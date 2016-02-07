package net.benmann.evald;

import java.util.List;

/**
 * Abstract base class for simplified user added functions. The ArgFunction subclasses eliminate any need to reference the expression's tree Nodes,
 * and provides automatic optimisation (elimination of functions with all constant inputs).
 * 
 * Use one of the public subclasses when adding a function to an Evald instance. These subclasses
 * ({@link OneArgFunction}, {@link TwoArgFunction}, {@link ThreeArgFunction} and {@link NArgFunction})
 * also provide a simplified interface for implementing custom functions, as there is no need to call {@link Node#get()}
 */
public abstract class ArgFunction {
    final int minArgs;
    final int maxArgs;
    final String token;
    final boolean isPure;

    protected ArgFunction(String token, int minArgs, int maxArgs, boolean isPure) {
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.token = token;
        this.isPure = isPure;
    }

    abstract protected double get(Node[] inputs, double[] values);

    ValueNode createNode(final List<Node> args) {
        if (isPure)
            return new PureFunctionValueNode(args);
        return new ImpureFunctionValueNode(args);
    }

    private class ImpureFunctionValueNode extends PureFunctionValueNode {
        public ImpureFunctionValueNode(final List<Node> args) {
            super(args);
        }

        @Override Node collapse() {
            return this;
        }
    }

    private class PureFunctionValueNode extends ValueNode {
        protected Node[] inputs;
        protected double[] values;

        public PureFunctionValueNode(final List<Node> args) {
            super(false);
            inputs = args.toArray(new Node[] {});
            values = new double[args.size()];
        }

        @Override Node collapse() {
            boolean allConstant = true;
            for (int i = 0; i < inputs.length; i++) {
                inputs[i] = inputs[i].collapse();
                if (!inputs[i].isConstant)
                    allConstant = false;
            }
            if (!allConstant)
                return this;
            return new Constant(get());
        }

        @Override String toTree(String prefix) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix).append("UserFn ").append(token).append("\n");
            for (Node input : inputs) {
                sb.append(input.toTree(prefix + "  "));
            }
            return sb.toString();
        }

        @Override protected double get() {
            return ArgFunction.this.get(inputs, values);
        }
    }

    /**
     * An {@link ArgFunction} which allows for vararg functions.
     * 
     * Override the {@link #get(double...)} method to implement desired custom functionality.
     * If a limited number of arguments are required, it's possible to throw an exception from {@link #get(double...)},
     * which will detect the argument mismatch on a call to {@link Evald#evaluate()}.
     * A better option is to use {@link OneArgFunction}, {@link TwoArgFunction} or {@link ThreeArgFunction},
     * which will raise a parse exception on the earlier call to {@link Evald#parse(String)}.
     */
    static abstract public class NArgFunction extends ArgFunction {
        /**
         * Construct a function evaluator that takes multiple arguments
         * 
         * @param token
         *            the token to be used. Must be a valid token (start with [a-zA-Z_], contain only [a-zA-Z0-9_])
         */
        public NArgFunction(String token) {
            this(token, 0, NArgParser.NO_MAX);
        }

        /**
         * Construct a function evaluator that takes multiple arguments
         * 
         * @param token
         *            the token to be used. Must be a valid token (start with [a-zA-Z_], contain only [a-zA-Z0-9_])
         * @param minArgs
         *            minimum legal number of arguments.
         * @param maxArgs
         *            maximum legal number of arguments, or {@link Parser#NO_MAX} if the maximum is unlimited.
         */
        public NArgFunction(String token, int minArgs, int maxArgs) {
            super(token, minArgs, maxArgs, true);
        }

        @Override protected double get(Node[] inputs, double[] values) {
            for (int i = 0; i < inputs.length; i++) {
                values[i] = inputs[i].get();
            }
            return get(values);
        }

        /**
         * Return the result of a custom function given the supplied arguments.
         * 
         * @param args
         *            A variable number of arguments, as supplied in the expression. Note that values may also be Inf, NaN or null.
         * @return the result of the custom function.
         */
        abstract protected double get(double... args);
    }

    /**
     * An {@link ArgFunction} which allows for impure (non-deterministic) vararg functions.
     * 
     * Override the {@link #get(double...)} method to implement desired custom functionality.
     * If a limited number of arguments are required, it's possible to throw an exception from {@link #get(double...)},
     * which will detect the argument mismatch on a call to {@link Evald#evaluate()}.
     * A better option is to use {@link OneArgFunction}, {@link TwoArgFunction} or {@link ThreeArgFunction},
     * which will raise a parse exception on the earlier call to {@link Evald#parse(String)}.
     * 
     * Note that compared to {@link NArgFunction}, this version will not benefit from up-front
     * expression optimisation, and is useful when the implemented function may provide different
     * output given the same input. An example is a customised method using an external
     * data source or implementing a random number generator.
     */
    static abstract public class ImpureNArgFunction extends ArgFunction {
        /**
         * Construct a function evaluator that takes multiple arguments
         * 
         * @param token
         *            the token to be used. Must be a valid token (start with [a-zA-Z_], contain only [a-zA-Z0-9_])
         */
        public ImpureNArgFunction(String token) {
            this(token, 0, NArgParser.NO_MAX);
        }

        /**
         * Construct a function evaluator that takes multiple arguments
         * 
         * @param token
         *            the token to be used. Must be a valid token (start with [a-zA-Z_], contain only [a-zA-Z0-9_])
         * @param minArgs
         *            minimum legal number of arguments.
         * @param maxArgs
         *            maximum legal number of arguments, or {@link Parser#NO_MAX} if the maximum is unlimited.
         */
        public ImpureNArgFunction(String token, int minArgs, int maxArgs) {
            super(token, minArgs, maxArgs, false);
        }

        @Override protected double get(Node[] inputs, double[] values) {
            for (int i = 0; i < inputs.length; i++) {
                values[i] = inputs[i].get();
            }
            return get(values);
        }

        /**
         * Return the result of a custom function given the supplied arguments.
         * 
         * @param args
         *            A variable number of arguments, as supplied in the expression. Note that values may also be Inf, NaN or null.
         * @return the result of the custom function.
         */
        abstract protected double get(double... args);
    }

    /**
     * An {@link ArgFunction} which allows for single-argument functions.
     * 
     * Override the {@link #get(double)} method to implement desired custom functionality.
     * Allows for parsing checks for the correct number of arguments.
     */
    static abstract public class OneArgFunction extends ArgFunction {
        /**
         * Construct a function evaluator that takes one argument
         * 
         * @param token
         *            the token to be used. Must be a valid token (start with [a-zA-Z_], contain only [a-zA-Z0-9_])
         */
        public OneArgFunction(String token) {
            super(token, 1, 1, true);
        }

        @Override protected double get(Node[] inputs, double[] values) {
            return get(inputs[0].get());
        }

        /**
         * Return the result of a custom function given the supplied argument.
         * 
         * @param value
         *            The single argument supplied for this evaluation. Note that values may also be Inf, NaN or null.
         * @return the result of the custom function.
         */
        abstract protected double get(double value);
    }

    /**
     * An {@link ArgFunction} which allows for two-argument functions.
     * 
     * Override the {@link #get(double, double)} method to implement desired custom functionality.
     * Allows for parsing checks for the correct number of arguments.
     */
    static abstract public class TwoArgFunction extends ArgFunction {
        /**
         * Construct a function evaluator that takes two arguments
         * 
         * @param token
         *            the token to be used. Must be a valid token (start with [a-zA-Z_], contain only [a-zA-Z0-9_])
         */
        public TwoArgFunction(String token) {
            super(token, 2, 2, true);
        }

        @Override protected double get(Node[] inputs, double[] values) {
            return get(inputs[0].get(), inputs[1].get());
        }

        /**
         * Return the result of a custom function given the supplied arguments.
         * 
         * @param arg1
         *            The first argument supplied for this evaluation. Note that values may also be Inf, NaN or null.
         * @param arg2
         *            The second argument supplied for this evaluation. Note that values may also be Inf, NaN or null.
         * @return the result of the custom function.
         */
        abstract protected double get(double arg1, double arg2);
    }

    /**
     * An {@link ArgFunction} which allows for three-argument functions.
     * 
     * Override the {@link #get(double, double, double)} method to implement desired custom functionality.
     * Allows for parsing checks for the correct number of arguments.
     */
    static abstract public class ThreeArgFunction extends ArgFunction {
        /**
         * Construct a function evaluator that takes three arguments
         * 
         * @param token
         *            the token to be used. Must be a valid token (start with [a-zA-Z_], contain only [a-zA-Z0-9_])
         */
        public ThreeArgFunction(String token) {
            super(token, 3, 3, true);
        }

        @Override protected double get(Node[] inputs, double[] values) {
            return get(inputs[0].get(), inputs[1].get(), inputs[2].get());
        }

        /**
         * Return the result of a custom function given the supplied arguments.
         * 
         * @param arg1
         *            The first argument supplied for this evaluation. Note that values may also be Inf, NaN or null.
         * @param arg2
         *            The second argument supplied for this evaluation. Note that values may also be Inf, NaN or null.
         * @param arg3
         *            The third argument supplied for this evaluation. Note that values may also be Inf, NaN or null.
         * @return the result of the custom function.
         */
        abstract protected double get(double arg1, double arg2, double arg3);
    }
}