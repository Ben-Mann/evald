package net.benmann.evald;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.benmann.evald.EvaldException.EmptyExpressionEvaldException;
import net.benmann.evald.EvaldException.OperatorExpectedEvaldException;
import net.benmann.evald.EvaldException.UnknownMethodEvaldException;

class ExpressionParser {
    private ExpressionString expression;
    final Evald evald;

    ExpressionParser(Evald evald, ExpressionString expression) {
        this.expression = expression;
        this.evald = evald;
    }


    Node parse(String expression) {
        return new ExpressionParser(evald, new ExpressionString(expression)).parse();
    }
    
    Node root;		//root node of the tree.
    Node lastValue;	//The last value we parsed.
    OperatorNode lastOperator;
    
    boolean readPrefix() {
    	PrefixOperatorNode node = null;
    	
        for (PrefixOperatorParser parser : evald.prefixOperatorParsers) {
        	node = parser.parse(expression);
        	if (node != null)
        		break;
        }
        
        if (node == null)
        	return false;
        
        if (lastOperator != null) {
            assert (lastOperator.b == null);
            lastOperator.b = node;
            node.parent = lastOperator;
        } else {
            assert (root == null); //expected this to be caught by the loop
            root = node;
        }
        lastOperator = node;
        
        return true;
    }
    
    boolean readValue() {
        Node node = null;
        String preParse = expression.expression;

        for (ValueParser parser : evald.valueParsers) {
            node = parser.parse(this, expression);
            if (node != null)
                break;
        }

        if (node == null) {
            node = Variable.parser.parse(this, expression);
            if (node == null)
                return false;
            //If expression starts with ( and node is a Variable, then we've read an undefined method.
            if (!expression.expression.isEmpty() && expression.expression.charAt(0) == '(')
                throw new UnknownMethodEvaldException(preParse);
        }

        if (lastOperator != null) {
            assert (lastOperator.b == null);
            lastOperator.b = node;
            node.parent = lastOperator;
            lastOperator = null;
        } else if (root == null) {
            root = node;
        }

        lastValue = node;
        return true;
    }
    
    boolean readPostfix() {
        PostfixOperatorNode node = null;
        PostfixOperatorParser parser = null;
		for(PostfixOperatorParser postfixParser : evald.postfixOperatorParsers) {
			parser = postfixParser;
            node = postfixParser.parse(expression);
            if (node != null)
                break;
		}

        if (node == null)
            return false;

        Node b;
        
        if (lastValue != null) {
            b = lastValue;
        } else {
                throw new EvaldException("The postfix operator "+parser.token+" requires an lvalue.");
        }
            
        if (root == null) {
            assert (false); //should have been caught by the lastValue/lastOperator above.
        }
        
        node.b = b;
        node.parent = b.parent;
        b.parent = node;
        if (node.parent != null) {
            node.parent.b = node;
        } else {
            root = node;
        }

        lastOperator = null;
        lastValue = node;
        
        return true;
    }

    final Pattern implictMultiplyPattern = Pattern.compile("^(\\s*).*");
    
    boolean readOperator() {
    	String msg = expression.expression;
    	
    	BinaryOperatorNode node = null;
    	BinaryOperatorParser parser = null;
    	
        for (BinaryOperatorParser operatorParser : evald.binaryOperatorParsers) {
            node = operatorParser.parse(expression);
            if (node != null) {
            	parser = operatorParser;
            	break;
            }
        }
        
        //Special case: No operator can imply multiplication
        if (node == null && evald.getImplicitMultiplication()) {
            Matcher matches = implictMultiplyPattern.matcher(expression.expression);
            if (matches.find()) {
                String tokenFound = matches.group(1);
                expression.update(tokenFound.length());
                parser = LibArithmetic.MULTIPLY;
                node = parser.create();
            }
        }

        if (node == null)
            throw new OperatorExpectedEvaldException(expression);
        
        if (lastValue == null)
        	throw new EvaldException("The operator "+parser.token+" at " + msg + " requires an lvalue");
        
        Node lvalue = lastValue;
        lastValue = null;
        
        while (lvalue.parent != null && lvalue.parent.getPrecedence() >= node.getPrecedence()) {
        	lvalue = lvalue.parent;
        }
        
        node.a = lvalue;
        node.parent = lvalue.parent;
        lvalue.parent = node;
        if (node.parent == null) {
            root = node;
        } else {
            node.parent.b = node;
        }

        lastOperator = node;
        
        return true;
    }

    static enum State {
    	PREFIX,
    	VALUE,
    	POSTFIX,
    	OPERATOR
    }
    
    Node parse() {
    	if (expression.expression.isEmpty())
            return LibArithmetic.NaN.parse(this, new ExpressionString("nan")); //TODO is this a good default?
    	
        State state = State.PREFIX;
        
        while(!expression.expression.isEmpty()) {
        	if (state == State.PREFIX) {
            	if (readPrefix())
            		continue;
            	state = State.VALUE;
        	} else if (state == State.VALUE) {
        		if (!readValue())
                    throw new EvaldException("Expected a value or expression at " + expression.expression);
        		state = State.POSTFIX;
        	} else if (state == State.POSTFIX) {
                if (readPostfix() && evald.getAllowMultiplePostfixOperators())
        			continue;
        		state = State.OPERATOR;
        	} else if (state == State.OPERATOR) {
        		if (!readOperator())
        			break;
        		state = State.PREFIX;
        	}
        }

        if (!expression.expression.isEmpty())
            throw new EmptyExpressionEvaldException(expression);

        assert (root != null);
        root = collapse(root);
        return root;
    }

    /**
     * Return this node, with constant expressions collapsed to constant values.
     */
    Node collapse(Node node) {
        return node.collapse();
    }

}