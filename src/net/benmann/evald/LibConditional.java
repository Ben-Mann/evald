package net.benmann.evald;

import java.util.List;

/**
 * Contains operators and methods for boolean logic.
 * Note that boolean operators always evaluate false on any operation involving NaN
 */
class LibConditional extends Library {
    @Override Parser[] getParsers() {
        return new Parser[] { IF, AND, OR, TRUE, FALSE, EQUALS, NOTEQUALS, NOT, LT, LTE, GT, GTE, ISNAN, ISINF, ISNULL };
    }

    /**
     * return true if non zero and not nan.
     */
    private static boolean evaluate(Node node) {
        Double value = node.get();
        return value > 0 || value < 0;
    }

    public static final ConstantParser TRUE = new ConstantParser("true") {
        @Override protected Constant create() {
            return new Constant(1.0);
        }
    };

    public static final ConstantParser FALSE = new ConstantParser("false") {
        @Override protected Constant create() {
            return new Constant(0.0);
        }
    };

    public static final NArgParser IF = new NArgParser(3, new CreateNArgFunctionFn("if") {
        @Override protected ValueNode fn(List<Node> args) {
            return new ThreeArgValueNode(token, args.get(0), args.get(1), args.get(2)) {
                @Override protected Double get() {
                    if (evaluate(arg1))
                        return arg2.get();

                    return arg3.get();
                }
            };
        }
    });

    public static final NArgParser ISNAN = new NArgParser(1, new CreateNArgFunctionFn("isnan") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected Double get() {
                    return Double.isNaN(arg1.get()) ? 1.0 : 0.0;
                }
            };
        }
    });

    public static final NArgParser ISINF = new NArgParser(1, new CreateNArgFunctionFn("isinf") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected Double get() {
                    return Double.isInfinite(arg1.get()) ? 1.0 : 0.0;
                }
            };
        }
    });

    public static final NArgParser ISNULL = new NArgParser(1, new CreateNArgFunctionFn("isnull") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected Double get() {
                    return arg1.get() == null ? 1.0 : 0.0;
                }
            };
        }
    });

    public static final BinaryOperatorParser AND = new BinaryOperatorParser("&&") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.CONDITIONAL_AND) {
                @Override protected Double get() {
                    return (evaluate(a)) && (evaluate(b)) ? 1.0 : 0.0;
                }
            };
        }
    };

    public static final BinaryOperatorParser OR = new BinaryOperatorParser("||") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.CONDITIONAL_OR) {
                @Override protected Double get() {
                    return (evaluate(a)) || (evaluate(b)) ? 1.0 : 0.0;
                }
            };
        }
    };

    /**
     * NaN and 0 require some special handling;
     */
    public static final BinaryOperatorParser EQUALS = new BinaryOperatorParser("==") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.EQUALITY) {
                @Override protected Double get() {
                    return a.get().doubleValue() == b.get().doubleValue() ? 1.0 : 0.0;
                }
            };
        }
    };
    public static final BinaryOperatorParser LT = new BinaryOperatorParser("<") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.EQUALITY) {
                @Override protected Double get() {
                    return a.get() < b.get() ? 1.0 : 0.0;
                }
            };
        }
    };
    public static final BinaryOperatorParser LTE = new BinaryOperatorParser("<=") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.EQUALITY) {
                @Override protected Double get() {
                    return a.get() <= b.get() ? 1.0 : 0.0;
                }
            };
        }
    };

    public static final BinaryOperatorParser GT = new BinaryOperatorParser(">") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.EQUALITY) {
                @Override protected Double get() {
                    return a.get() > b.get() ? 1.0 : 0.0;
                }
            };
        }
    };
    public static final BinaryOperatorParser GTE = new BinaryOperatorParser(">=") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.EQUALITY) {
                @Override protected Double get() {
                    return a.get() >= b.get() ? 1.0 : 0.0;
                }
            };
        }
    };
    public static final BinaryOperatorParser NOTEQUALS = new BinaryOperatorParser("!=") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.EQUALITY) {
                @Override protected Double get() {
                    return a.get().doubleValue() != b.get().doubleValue() ? 1.0 : 0.0;
                }
            };
        }
    };

    public static final PrefixOperatorParser NOT = new PrefixOperatorParser("!") {
        @Override protected PrefixOperatorNode create() {
            return new PrefixOperatorNode(token) {
                @Override protected Double get() {
                    return evaluate(b) ? 0.0 : 1.0;
                }
            };
        }
    };
}
