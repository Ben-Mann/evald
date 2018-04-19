package net.benmann.evald;

import net.benmann.evald.EvaldException.UndeclaredVariableEvaldException;
import net.benmann.evald.EvaldException.UnknownMethodEvaldException;

import java.util.Arrays;

/**
 * Double expression parser with support for multiple intermediate variables and multiple final outputs
 */
public class MultiEvald extends AbstractEvald {

    private int intermediateCount = 0;
    private String[] intermediateNames = new String[0];
    private Node[] intermediateExpressions = new Node[0];
    private int[] intermediateInputIndexes = new int[0];

    private int outputCount = 0;
    private String[] outputNames = new String[0];
    private Node[] outputExpressions = new Node[0];
    private double[] outputs = new double[0];

    /**
     * Construct with only basic arithmetic support ({@link LibArithmetic Library.CORE})
     */
    public MultiEvald() {
        super();
    }

    /**
     * Construct with the specified set of libraries.
     */
    public MultiEvald(Library... libraries) {
    	super(libraries);
    }

    /**
     * Reset this instance before parsing a new expression.
     */
    public void resetExpression() {
        super.reset();

        intermediateCount = 0;
        Arrays.fill(intermediateNames, null);
        Arrays.fill(intermediateExpressions, null);
        Arrays.fill(intermediateInputIndexes, 0);

        outputCount = 0;
        Arrays.fill(outputNames, null);
        Arrays.fill(outputExpressions, null);
        Arrays.fill(outputs, 0);
    }

    /**
     * Parse the specified expression and store it to be used in later expressions.
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
    public void parseIntermediate(String name, String expression) {
        Node intermediate = super.parseNode(expression);
        int inputIndex = super.addVariable(name);

        int index = intermediateCount;
        intermediateCount += 1;

        if(index >= intermediateNames.length) {
            int newLength = intermediateNames.length + Evald.ARRAY_RESIZE_BUFFER;

            intermediateNames = Arrays.copyOf(intermediateNames, newLength);
            intermediateExpressions = Arrays.copyOf(intermediateExpressions, newLength);
            intermediateInputIndexes = Arrays.copyOf(intermediateInputIndexes, newLength);
        }

        intermediateNames[index] = name;
        intermediateExpressions[index] = intermediate;
        intermediateInputIndexes[index] = inputIndex;
    }

    /**
     * Parse the specified expression, returning an index to use with {@link #getOutput(int)}.
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
    public int parseOutput(String name, String expression) {
        Node output = super.parseNode(expression);

        int index = outputCount;
        outputCount += 1;

        if(index >= outputNames.length) {
            int newLength = outputNames.length + Evald.ARRAY_RESIZE_BUFFER;

            outputNames = Arrays.copyOf(outputNames, newLength);
            outputExpressions = Arrays.copyOf(outputExpressions, newLength);
            outputs = Arrays.copyOf(outputs, newLength);
        }

        outputNames[index] = name;
        outputExpressions[index] = output;

        return index;
    }

    /**
     * Get the calculated value for output {@param index} after {@link #evaluate()} is called.
     */
    public double getOutput(int index) {
        return outputs[index];
    }

    /**
     * Evaluate the expressions, and store all of the final output results for use by {@link #getOutput(int)}.
     *
     * @throws NullPointerException
     *             if an operation is attempted on a null variable.
     */
    public void evaluate() {
        for(int index = 0; index < intermediateCount; ++index) {
            Node expression = intermediateExpressions[index];
            int inputIndex = intermediateInputIndexes[index];

            super.setVariable(inputIndex, expression.get());
        }

        for(int index = 0; index < outputCount; ++index) {
            Node expression = outputExpressions[index];

            outputs[index] = expression.get();
        }
    }

    /**
     * Construct a human readable representation of the expression tree
     *
     * @return a multiline string containing the final optimised expression trees.
     */
    public String toTree() {
        StringBuilder builder = new StringBuilder();

        for(int index = 0; index < intermediateCount; ++index) {
            String name = intermediateNames[index];
            Node expression = intermediateExpressions[index];

            builder.append(name);
            builder.append(" =\n");
            builder.append(expression.toTree("  "));
            builder.append("\n");
        }

        for(int index = 0; index < outputCount; ++index) {
            String name = outputNames[index];
            Node expression = outputExpressions[index];

            builder.append(name);
            builder.append(" =\n");
            builder.append(expression.toTree("  "));
            builder.append("\n");
        }

        return builder.toString();
    }
}
