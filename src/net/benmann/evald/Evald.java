package net.benmann.evald;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.benmann.evald.AbstractEvaldException.EmptyExpressionEvaldException;
import net.benmann.evald.AbstractEvaldException.EvaldException;
import net.benmann.evald.AbstractEvaldException.InvalidTokenEvaldException;
import net.benmann.evald.AbstractEvaldException.UndeclaredVariableEvaldException;
import net.benmann.evald.AbstractEvaldException.UninitialisedEvaldException;
import net.benmann.evald.AbstractEvaldException.UnknownMethodEvaldException;

/**
 * Double Expression Parser
 */
public class Evald {
    private boolean allowUndeclared = true;
    private boolean implicitMultiplication = true;
    private boolean allowMultiplePostfixOperators = true;
    private static final Pattern validTokenPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
    private static final Pattern subExpressionPattern = Pattern.compile("^\\s*([a-zA-Z_][a-z_A-Z0-9]*)\\s*=([^=]+.*)");
    private static final int ARRAY_RESIZE_BUFFER = 16;
    private static final String DEFAULT_RESULT_VARIABLE = "result";
    private String resultVariable = DEFAULT_RESULT_VARIABLE;

    private double[] valueArray;
    private String[] variableToken;
    private int valueArraySize;
    private Set<Integer> inputSet = new HashSet<>();

    /**
     * Track subexpression properties, primarily linking a variable to an expression.
     * Also used to manage dependencies - if an output variable is to be ignored, then
     * we can determine if it need not be evaluated at all.
     */
    private static class SubExpression {
        int outputVariableIndex;
        Node expressionRoot;
        boolean enabled;
        final Set<Integer> usedVariables;

        SubExpression(int outputVariableIndex, Node expressionRoot, Set<Integer> usedVariables) {
            this.outputVariableIndex = outputVariableIndex;
            this.expressionRoot = expressionRoot;
            this.usedVariables = usedVariables;
            enabled = true;
        }
    }

    final List<SubExpression> expressions = new ArrayList<>();

    final Map<String, Integer> keyIndexMap = new HashMap<String, Integer>();
    final Set<String> undeclaredKeyMap = new HashSet<String>();
    final Set<String> outputKeyMap = new HashSet<String>();
    private final List<SetValueArrayCallback> valueArrayCallbacks = new ArrayList<SetValueArrayCallback>();
    private final Set<Integer> usedIndices = new HashSet<Integer>();
    private final Set<String> usedFunctions = new HashSet<String>();

    ParserList<ValueParser> valueParsers = new ParserList<ValueParser>();
    ParserList<BinaryOperatorParser> binaryOperatorParsers = new ParserList<BinaryOperatorParser>();
    ParserList<PrefixOperatorParser> prefixOperatorParsers = new ParserList<PrefixOperatorParser>();
    ParserList<PostfixOperatorParser> postfixOperatorParsers = new ParserList<PostfixOperatorParser>();

    /**
     * Construct with only basic arithmetic support ({@link LibArithmetic Library.CORE})
     */
    public Evald() {
        this(new LibArithmetic());
    }
    
    /**
     * Add one or more predefined {@link Library}
     * 
     * @param libraries
     *            The library or libraries to be added to the current Evald instance.
     */
    public void addLibrary(Library... libraries) {
        for (Library library : libraries) {
            assignParsers(library.getParsers());
        }
    }

    /**
     * Remove all methods from one or more libraries. Note that this matches the library specific
     * implementation; if a library function has been overridden with {@link #addUserFunction(String, ArgFunction)}
     * the overridden function (as it is not part of the library) will remain.
     * 
     * @param libraries
     */
    public void removeLibrary(Library... libraries) {
        for (Library library : libraries) {
            remove(library.getParsers());
        }
    }

    /**
     * Construct with the specified set of libraries.
     */
    public Evald(Library... libraries) {
    	for(Library library : libraries) {
    		assignParsers(library.getParsers());
    	}
    }
    
    /**
     * Parse the specified expression.
     * 
     * @param expression
     *            an expression to parse
     * 
     * @throws UndeclaredVariableEvaldException
     *             if any variable in the expression has not yet been declared, and setAllowUndeclared was not set. The exception lists the variable names in its message.
     * @throws UnknownMethodEvaldException
     *             if any method in the expression has not been declared. Set the method using {@link #addUserFunction}.
     * @throws EvaldException
     *             on any other syntax error.
     */
    public void parse(String expression) {
        valueArrayCallbacks.clear();
        usedIndices.clear();
        usedFunctions.clear();
        inputSet.clear();
        
        parseSubExpressions(expression);

        for (SetValueArrayCallback callback : valueArrayCallbacks) {
            callback.setValueArray(valueArray);
        }

        if (allowUndeclared || undeclaredKeyMap.isEmpty())
        	return;
        
        throw new UndeclaredVariableEvaldException(undeclaredKeyMap);
    }

    private void parseSubExpressions(String expression) {
        expressions.clear();
        String[] statements = expression.split(";");
        if (statements.length == 1) {
            if (!subExpressionPattern.matcher(statements[0]).find()) {
                parseSubExpression(resultVariable + " = " + statements[0], false);
                return;
            }
        }
        for (String statement : statements) {
            statement = statement.trim();
            if (statement.isEmpty()) {
                continue;
            }
            parseSubExpression(statement, true);
        }
    }

    private void parseSubExpression(String subExpression, boolean includeContext) {
        try {
            Matcher matcher = subExpressionPattern.matcher(subExpression);
            if (!matcher.find() || matcher.groupCount() != 2) {
                throw new EvaldException("Expected an assignment expression.");
            }

            String variable = matcher.group(1);
            String expression = matcher.group(2);

            ExpressionString string = new ExpressionString(expression);
            if (string.expression.isEmpty())
                throw new EmptyExpressionEvaldException(string);

            int index = addVariable(variable);
            usedIndices.add(index);
            ExpressionParser parser = new ExpressionParser(this, string);
            Node root = parser.parse();
            Set<Integer> newInputs = new HashSet<Integer>(parser.usedIndices);
            newInputs.removeAll(usedIndices);
            usedIndices.addAll(parser.usedIndices);
            inputSet.addAll(newInputs);
            expressions.add(new SubExpression(index, root, parser.usedIndices));
        } catch (AbstractEvaldException e) {
            if (!includeContext) {
                throw e;
            }
            throw e.withContext(subExpression);
        }
    }

    /**
     * Evaluate the expression, and return a double value.
     * 
     * @return the result of evaluating the expression using the current variable values.
     * 
     * @throws NullPointerException
     *             if an operation is attempted on a null variable.
     */
    public double evaluate() {
        if (expressions.isEmpty()) {
            throw new UninitialisedEvaldException("Parser not initialised");
        }
        double result = 0;
        for (SubExpression expression : expressions) {
            if (!expression.enabled) {
                continue;
            }
            result = expression.expressionRoot.get();
            valueArray[expression.outputVariableIndex] = result;
        }
        return result;
    }

    public int getVariableIndex(String token) {
        Integer result = keyIndexMap.get(token);
        if (result == null) {
            throw new UndeclaredVariableEvaldException(token);
        }
        return result;
    }

    public double getVariableValue(int index) {
        return valueArray[index];
    }

    public double getVariableValue(String token) {
        return valueArray[getVariableIndex(token)];
    }

    /**
     * Declare a variable to be used by the expression parser. The returned integer can be used as an index in calls to {@link #setVariable()} for faster updates.
     * 
     * @param token
     *            Name of the variable to be added. Names are case sensitive.
     * @return an index that can be used to update this variable with {@link #setVariable}. The same index will be returned if the variable was previously added using addVariable.
     */
    public int addVariable(String token) {
        return addVariableToList(token, 0.0);
    }

    /**
     * Declare a variable to be used by the expression parser. The returned integer can be used as an index in calls to {@link #setVariable()} for faster updates.
     * 
     * Note that internally the variable will be converted to a double.
     * 
     * @param token
     *            Name of the variable to be added. Names are case sensitive.
     * @param value
     *            an initial value for the variable.
     * @return an index that can be used to update this variable with {@link #setVariable}. The same index will be returned if the variable was previously added using addVariable.
     */
    public int addVariable(String token, float value) {
        return addVariableToList(token, (double) value);
    }

    /**
     * Declare a variable to be used by the expression parser. The returned integer can be used as an index in calls to {@link #setVariable()} for faster updates.
     * 
     * Note that internally the variable will be converted to a double.
     * 
     * @param token
     *            Name of the variable to be added. Names are case sensitive.
     * @param value
     *            an initial value for the variable.
     * @return an index that can be used to update this variable with {@link #setVariable}. The same index will be returned if the variable was previously added using addVariable.
     */
    public int addVariable(String token, int value) {
        return addVariableToList(token, (double) value);
    }

    /**
     * Declare a variable to be used by the expression parser. The returned integer can be used as an index in calls to {@link #setVariable()} for faster updates.
     * 
     * @param token
     *            Name of the variable to be added. Names are case sensitive.
     * @param value
     *            an initial value for the variable.
     * @return an index that can be used to update this variable with {@link #setVariable}. The same index will be returned if the variable was previously added using addVariable.
     */
    public int addVariable(String token, double value) {
        return addVariableToList(token, value);
    }

    /**
     * Add a variable. If the variable has an index, add it to valueArray AND valueList, otherwise add it only to valueList.
     */
    private int addVariableToList(String token, Double value) {
        if (!validToken(token))
            throw new InvalidTokenEvaldException(token);

        Integer result = keyIndexMap.get(token);
        if (result == null) {
            result = valueArraySize++;
            keyIndexMap.put(token, result);
            validateValueArrayIndex(result);
            variableToken[result] = token;
        }
        valueArray[result] = value;
        return result;
    }

    private void validateValueArrayIndex(int index) {
        if (valueArray != null && index < valueArray.length) {
            return;
        }
        if (valueArray == null) {
            valueArray = new double[ARRAY_RESIZE_BUFFER];
            variableToken = new String[ARRAY_RESIZE_BUFFER];
        } else if (valueArray.length <= index) {
            double[] oldValues = valueArray;
            String[] oldTokens = variableToken;
            int newSize = index + ARRAY_RESIZE_BUFFER;
            valueArray = new double[newSize];
            variableToken = new String[newSize];
            System.arraycopy(oldValues, 0, valueArray, 0, oldValues.length);
            System.arraycopy(oldTokens, 0, variableToken, 0, oldTokens.length);
        }
        for (SetValueArrayCallback callback : valueArrayCallbacks) {
            callback.setValueArray(valueArray);
        }
    }

    /**
     * Updates the variable at the specified index with a new value. This is the preferred method for high performance evaluation, since no hash lookup based on the variable name is required.
     * 
     * @param index
     *            index created using {@link #addVariable}
     * @param value
     *            the value to which the variable should be set.
     * @throws IndexOutOfBoundsException
     *             if the index was not obtained from {@link #addVariable}
     * @throws NullPointerException
     *             if the parser has not yet been run, and the indexed value cache index is therefore unavailable.             
     */
    public void setVariable(int index, double value) {
        valueArray[index] = value;
    }

    /**
     * Updates the variable at the specified index with a new value. This is the preferred method for high performance evaluation, since no hash lookup based on the variable name is required.
     * 
     * Note that internally the variable will be converted to a double.
     * 
     * @param index
     *            index created using {@link #addVariable}.
     * @param value
     *            the value to which the variable should be set.
     * @throws IndexOutOfBoundsException
     *             if the index was not obtained from {@link #addVariable}
     * @throws NullPointerException
     *             if the parser has not yet been run, and the indexed value cache index is therefore unavailable.             
     */
    public void setVariable(int index, float value) {
        valueArray[index] = (double) value;
    }

    /**
     * Updates the variable at the specified index with a new value. This is the preferred method for high performance evaluation, since no hash lookup based on the variable name is required.
     * 
     * Note that internally the variable will be converted to a double.
     * 
     * @param index
     *            index created using {@link #addVariable}.
     * @param value
     *            the value to which the variable should be set.
     * @throws IndexOutOfBoundsException
     *             if the index was not obtained from {@link #addVariable}
     * @throws NullPointerException
     *             if the parser has not yet been run, and the indexed value cache index is therefore unavailable.             
     */
    public void setVariable(int index, int value) {
        valueArray[index] = (double) value;
    }

    /**
     * Enable or disable implicit multiplication.
     * 
     * Implicit multiplication allows an expression such as 'a b' to be interpreted as a*b; spaces
     * between variable names are an implicit * operator.
     * 
     * @param true to enable implicit multiplication (the default), false to disable it.
     */
    public void setImplicitMultiplication(boolean enabled) {
        implicitMultiplication = enabled;
    }

    /**
     * Control how the parser should handle undeclared variables. If allowed, the variables can
     * be added later. If disallowed, the variables must have been predefined, and missing
     * variables will cause the parser to throw a {@link UndeclaredVariableEvaldException}. The default
     * value is true.
     * 
     * @param enabled
     *            false to raise an exception when encountering undeclared variables during parsing, true to ignore undeclared variables during parsing.
     */
    public void setAllowUndeclared(boolean enabled) {
        allowUndeclared = enabled;
    }
    
    /**
     * If setAllowUndeclared is set to true, undeclared variables can be read post-parse
     * using this function, to ensure they are added before later running evaluate.
     * 
     * @return an array of undeclared variable names found in the string
     */
    public String[] listUndeclared() {
        return undeclaredKeyMap.toArray(new String[] {});
    }

    /**
     * List all variables currently defined in this Evald instance
     */
    public String[] listAllVariables() {
        String[] result = new String[valueArraySize];
        System.arraycopy(variableToken, 0, result, 0, valueArraySize);
        return result;
    }

    private String[] tokensFromIndices(Collection<Integer> indices) {
        List<String> tokens = new ArrayList<>(indices.size());
        for (Integer index : indices) {
            tokens.add(variableToken[index]);
        }
        return tokens.toArray(new String[] {});
    }

    /**
     * List all variables used by this expression which are not defined by the expression itself
     */
    public String[] listAllInputs() {
        return tokensFromIndices(inputSet);
    }

    /**
     * List all variables set by this expression, which are not also a required input
     */
    public String[] listAllOutputOrIntermediateVariables() {
        Set<Integer> indices = new HashSet<>(usedIndices);
        indices.removeAll(inputSet);
        return tokensFromIndices(indices);
    }

    /**
     * Sets which output variables (from {@link #listAllOutputOrIntermediateVariables()}) are actually needed by the caller.
     * Causes the evaluator to disable any output or intermediate variable which is not required for these outputs.
     * 
     * @param outputs
     *            an array of variable tokens specifying the outputs to enable. The array must not be empty.
     */
    public void enableOutputs(String... outputToken) {
        int[] outputIndex = new int[outputToken.length];
        for (int i = 0; i < outputToken.length; i++) {
            Integer index = keyIndexMap.get(outputToken[i]);
            if (index == null) {
                throw new UndeclaredVariableEvaldException(outputToken[i]);
            }
            outputIndex[i] = index;
        }
        enableOutputs(outputIndex);
    }

    /**
     * Sets which output variables (from {@link #listAllOutputOrIntermediateVariables()}) are actually needed by the caller.
     * Causes the evaluator to disable any output or intermediate variable which is not required for these outputs.
     * 
     * @param outputs
     *            an array of variable indices specifying the outputs to enable. The array must not be empty.
     */
    public void enableOutputs(int... outputIndex) {
        int len = expressions.size() - 1;
        Set<Integer> required = new HashSet<>();
        for (int index : outputIndex) {
            required.add(index);
        }
        for(int i = len; i >= 0; i--) {
            SubExpression expression = expressions.get(i);
            expression.enabled = required.contains(expression.outputVariableIndex);
            if (expression.enabled) {
                required.addAll(expression.usedVariables);
            }
        }
    }

    public void enableAllOutputs() {
        for (SubExpression expression : expressions) {
            expression.enabled = true;
        }
    }

    /**
     * List all functions currently in use by this instance's expression.
     */
    public String[] listActiveFunctions() {
        return usedFunctions.toArray(new String[] {});
    }

    /** 
     * List all variables currently in use by this instance's expression (including undefined variables, if permitted by {@link #setAllowUndeclared(boolean)}).
     */
    public String[] listActiveVariables() {
        if (expressions.isEmpty() || usedIndices.isEmpty())
            return new String[] {};
        
        return tokensFromIndices(usedIndices);
    }

    /**
     * Determine whether the parser will allow undeclared variables or throw an exception.
     * 
     * @return the value previously set by setAllowUndeclared, or the default of true if unset.
     */
    public boolean getAllowUndeclared() {
    	return allowUndeclared;
    }
    
    /**
     * Determine whether implicit multiplication is enabled.
     * 
     * Implicit multiplication allows an expression such as 'a b' to be interpreted as a*b; spaces
     * between variable names are an implicit * operator.
     * 
     * @return the result set by a previous call to {@link #setImplicitMultiplication}, or the default (true)
     */
    public boolean getImplicitMultiplication() {
    	return implicitMultiplication;
    }
    

    /**
     * Indicates whether postfix operators can be legally concatenated without explicit braces.
     * 
     * For example, if the postfix factorial operator ! were added, then "a!!" will succeed
     * only with this enabled; otherwise "(a!)!" will be required to explicitly declare the additional postfix.
     * 
     * @return true if multiple postfix operators are permitted (the default)
     */
    public boolean getAllowMultiplePostfixOperators() {
    	return allowMultiplePostfixOperators;
    }

    /**
     * Allow postfix operators to be legally concatenated without explicit braces.
     * 
     * For example, if the postfix factorial operator ! were added, then "a!!" will succeed
     * only with this enabled; otherwise "(a!)!" will be required to explicitly declare the additional postfix.
     * 
     * @param allow
     *            true to allow multiple postfix operators, false to force braces use.
     */
    public void setAllowMultiplePostfixOperators(boolean allow) {
        allowMultiplePostfixOperators = allow;
    }

    /**
     * Remove a loaded function that uses the specified token. This will
     * only affect new parsing - existing evaluation will still use the
     * previously loaded function with that name.
     * 
     * @param token
     *            The name of the function to be removed.
     */
    public void removeFunction(String token) {
        valueParsers.remove(token);
    }

    /**
     * Add a new function to be used in subsequent {@link #parse()} calls. If the
     * token is already in use by another function, it will be replaced.
     * Tokens by the same name that are already in the parse tree will
     * continue to be used by the evaluator until the next {@link #parse()} call.
     * 
     * @param function
     *            An {@link ArgFunction} to be called when this token is encountered in the expression.
     * @throws InvalidTokenEvaldException
     *             if the specified token is not a valid token name.
     */
    public void addUserFunction(final ArgFunction function) {
        if (!validToken(function.token))
            throw new InvalidTokenEvaldException(function.token);

        removeFunction(function.token);

        valueParsers.add(new NArgParser(function.minArgs, function.maxArgs, new CreateNArgFunctionFn(function.token) {
    		@Override public ValueNode fn(List<Node> args) {
                return function.createNode(args);
    		}
    	}));
	}

    void addParser(Parser parser) {
        if (parser instanceof ValueParser) {
            valueParsers.add((ValueParser) parser);
        } else if (parser instanceof PrefixOperatorParser) {
            prefixOperatorParsers.add((PrefixOperatorParser) parser);
        } else if (parser instanceof PostfixOperatorParser) {
            postfixOperatorParsers.add((PostfixOperatorParser) parser);
        } else if (parser instanceof BinaryOperatorParser) {
            binaryOperatorParsers.add((BinaryOperatorParser) parser);
        }
    }

    private void assignParsers(Parser... parsers) {
        for (Parser parser : parsers) {
            addParser(parser);
        }
    }

    void removeParser(Parser parser) {
        if (parser instanceof ValueParser) {
            valueParsers.remove((ValueParser) parser);
        } else if (parser instanceof PrefixOperatorParser) {
            prefixOperatorParsers.remove((PrefixOperatorParser) parser);
        } else if (parser instanceof PostfixOperatorParser) {
            postfixOperatorParsers.remove((PostfixOperatorParser) parser);
        } else if (parser instanceof BinaryOperatorParser) {
            binaryOperatorParsers.remove((BinaryOperatorParser) parser);
        }
    }
    
    /**
     * Remove any constant using the specified token. Allows you to create variables a,b,c,d,e, for example,
     * if the constant e is not required.
     * 
     * @param token the constant to be removed.
     */
    public void removeConstant(String token) {
        if (token == null || token.isEmpty())
            return;
        
        List<ValueParser> toRemove = new ArrayList<ValueParser>();
        for (ValueParser parser : valueParsers) {
            if (token.equals(parser.token) && parser instanceof ConstantParser) {
                toRemove.add(parser);
            }
        }

        for (ValueParser parser : toRemove) {
            valueParsers.remove(parser);
        }
    }

    /**
     * Construct a human readable representation of the expression tree
     * 
     * @return a multiline string containing the final optimised expression tree.
     */
    public String toTree() {
        StringBuilder result = new StringBuilder();
        for(SubExpression expression : expressions) {
            if (!expression.enabled) {
                continue;
            }
            result.append(variableToken[expression.outputVariableIndex]);
            result.append(" = ");
            result.append(expression.expressionRoot.toTree(""));
        }
        return result.toString();
    }

    /** For tests only */
    String executionSequence() {
        StringBuilder result = new StringBuilder();
        for (SubExpression expression : expressions) {
            if (!expression.enabled) {
                continue;
            }
            result.append(variableToken[expression.outputVariableIndex]);
        }
        return result.toString();
    }

    public void remove(Parser... parsers) {
        for (Parser parser : parsers) {
            removeParser(parser);
        }
    }

    private boolean validToken(String token) {
        return validTokenPattern.matcher(token).find();
    }

    void addValueArrayCallback(SetValueArrayCallback valueArrayCallback) {
        valueArrayCallbacks.add(valueArrayCallback);
    }

    void addUsedIndex(int variableIndex) {
        if (usedIndices.contains(variableIndex)) {
            return;
        }
        inputSet.add(variableIndex);
        usedIndices.add(variableIndex);
    }

    void addUsedFunction(String functionName) {
        usedFunctions.add(functionName);
    }

    public String getDefaultResultToken() {
        return resultVariable;
    }

    public void setDefaultResultToken(String defaultToken) {
        if (!validToken(defaultToken)) {
            throw new InvalidTokenEvaldException(defaultToken);
        }
        resultVariable = defaultToken;
    }
}
