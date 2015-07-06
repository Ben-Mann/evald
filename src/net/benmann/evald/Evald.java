package net.benmann.evald;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.benmann.evald.EvaldException.InvalidTokenEvaldException;
import net.benmann.evald.EvaldException.UndeclaredVariableEvaldException;
import net.benmann.evald.EvaldException.UnknownMethodEvaldException;

/**
 * Double Expression Parser
 */
public class Evald {
    private Node root;
    private boolean allowUndeclared = true;
    private boolean implicitMultiplication = true;
    private boolean allowMultiplePostfixOperators = true;
    private static final Pattern validTokenPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");

    final List<Double> valueList = new ArrayList<Double>();
    final Map<String, Integer> keyIndexMap = new HashMap<String, Integer>();
    final Set<String> undeclaredKeyMap = new HashSet<String>();

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
            removeParsers(library.getParsers());
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
        root = new ExpressionParser(this, new ExpressionString(expression)).parse();
        if (allowUndeclared || undeclaredKeyMap.isEmpty())
        	return;
        
        throw new UndeclaredVariableEvaldException(undeclaredKeyMap);
    }

    /**
     * Evaluate the expression, and return a double value.
     * 
     * @return the result of evaluating the expression using the current variable values.
     * 
     * @throws NullPointerException
     *             if an operation is attempted on a null variable.
     */
    public Double evaluate() {
        return root.get();
    }

    /**
     * Declare a variable to be used by the expression parser. The returned integer can be used as an index in calls to {@link #setVariable()} for faster updates.
     * 
     * @param token
     *            Name of the variable to be added. Names are case sensitive.
     * @return an index that can be used to update this variable with {@link #setVariable}. The same index will be returned if the variable was previously added using addVariable.
     */
    public int addVariable(String token) {
        return addVariable(token, (Double) null);
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
    public int addVariable(String token, Float value) {
        return addVariable(token, value.doubleValue());
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
    public int addVariable(String token, Integer value) {
        return addVariable(token, value.doubleValue());
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
    public int addVariable(String token, Double value) {
        if (!validToken(token))
            throw new InvalidTokenEvaldException(token);

        Integer result = keyIndexMap.get(token);
        if (result == null) {
            result = valueList.size();
            keyIndexMap.put(token, result);
            valueList.add(value);
        } else {
        	valueList.set(result, value);
        }
        return result;
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
     */
    public void setVariable(int index, Double value) {
        valueList.set(index, value);
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
     */
    public void setVariable(int index, Float value) {
        valueList.set(index, value.doubleValue());
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
     */
    public void setVariable(int index, Integer value) {
        valueList.set(index, value.doubleValue());
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
                function.set(args);
    			return function;
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

    String toTree() {
        return root.toTree("");
    }

    private void removeParsers(Parser... parsers) {
        for (Parser parser : parsers) {
            removeParser(parser);
        }
    }

    private boolean validToken(String token) {
        return validTokenPattern.matcher(token).find();
    }
}
