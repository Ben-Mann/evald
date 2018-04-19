package net.benmann.evald;

import net.benmann.evald.EvaldException.UndeclaredVariableEvaldException;
import net.benmann.evald.EvaldException.UnknownMethodEvaldException;

/**
 * Double Expression Parser
 */
public class Evald extends AbstractEvald {

    private Node root;

    /**
     * Construct with only basic arithmetic support ({@link LibArithmetic Library.CORE})
     */
    public Evald() {
        super();
    }

    /**
     * Construct with the specified set of libraries.
     */
    public Evald(Library... libraries) {
    	super(libraries);
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
        super.reset();

        root = super.parseNode(expression);
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
        return root.get();
    }

    /**
     * Construct a human readable representation of the expression tree
     * 
     * @return a multiline string containing the final optimised expression tree.
     */
    public String toTree() {
        return root.toTree("");
    }
}
