package net.benmann.evald;

import java.util.List;

public final class LibMath extends Library {
    @Override Parser[] getParsers() {
        return new Parser[] { SIGN, LOG2, RINT, MOD, SIN, POW, E, PI, ABS, ACOS, ASIN, ATAN, ATAN2, CBRT, CEIL, COS, COSH, EXP, FLOOR, HYPOT, LOG, LOG10, MAX, MIN, RANDOM, ROUND, SINH, SQRT, TAN, TANH, TODEGREES, TORADIANS, SECANT, COSECANT, COTAN, ASINH, ACOSH, ATANH };
	}

    public static final NArgParser SIGN = new NArgParser(1, new CreateNArgFunctionFn("sign") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    double d = arg1.get();
                    if (Double.isNaN(d))
                        return Double.NaN;
                    if (d < 0)
                        return -1;
                    if (d > 0)
                        return 1;
                    return 0;
                }
            };
        }
    });

    public static final NArgParser LOG2 = new NArgParser(1, new CreateNArgFunctionFn("log2") {
        final double log2value = Math.log(2);
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.log(arg1.get()) / log2value;
                }
            };
        }
    });

    public static final NArgParser RINT = new NArgParser(1, new CreateNArgFunctionFn("rint") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.rint(arg1.get());
                }
            };
        }
    });

    public static final NArgParser TAN = new NArgParser(1, new CreateNArgFunctionFn("tan") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.tan(arg1.get());
                }
            };
        }
    });
    public static final NArgParser TANH = new NArgParser(1, new CreateNArgFunctionFn("tanh") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.tanh(arg1.get());
                }
            };
        }
    });
    public static final NArgParser TODEGREES = new NArgParser(1, new CreateNArgFunctionFn("toDegrees") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.toDegrees(arg1.get());
                }
            };
        }
    });
    public static final NArgParser TORADIANS = new NArgParser(1, new CreateNArgFunctionFn("toRadians") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.toRadians(arg1.get());
                }
            };
        }
    });

    public static final NArgParser ROUND = new NArgParser(1, new CreateNArgFunctionFn("round") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return (double) Math.round(arg1.get());
                }
            };
        }
    });

    public static final NArgParser SQRT = new NArgParser(1, new CreateNArgFunctionFn("sqrt") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.sqrt(arg1.get());
                }
            };
        }
    });

    public static final NArgParser HYPOT = new NArgParser(2, new CreateNArgFunctionFn("hypot") {
        @Override protected ValueNode fn(List<Node> args) {
            return new TwoArgValueNode(token, args.get(0), args.get(1)) {
                @Override protected double get() {
                    return Math.hypot(arg1.get(), arg2.get());
                }
            };
        }
    });

    public static final NArgParser MOD = new NArgParser(2, new CreateNArgFunctionFn("mod") {
        @Override protected ValueNode fn(List<Node> args) {
            return new TwoArgValueNode(token, args.get(0), args.get(1)) {
                @Override protected double get() {
                    return arg1.get() % arg2.get();
                }
            };
        }
    });

    public static final NArgParser MAX = new NArgParser(2, new CreateNArgFunctionFn("max") {
        @Override protected ValueNode fn(List<Node> args) {
            return new TwoArgValueNode(token, args.get(0), args.get(1)) {
                @Override protected double get() {
                    return Math.max(arg1.get(), arg2.get());
                }
            };
        }
    });
    public static final NArgParser MIN = new NArgParser(2, new CreateNArgFunctionFn("min") {
        @Override protected ValueNode fn(List<Node> args) {
            return new TwoArgValueNode(token, args.get(0), args.get(1)) {
                @Override protected double get() {
                    return Math.min(arg1.get(), arg2.get());
                }
            };
        }
    });
    public static final ConstantParser E = new ConstantParser("e") {
        @Override protected Constant create() {
            return new Constant(Math.E);
        }
    };

    public static final ConstantParser PI = new ConstantParser("pi") {
        @Override protected Constant create() {
            return new Constant(Math.PI);
        }
    };

    public static final NArgParser RANDOM = new NArgParser(0, new CreateNArgFunctionFn("random") {
        @Override protected ValueNode fn(List<Node> args) {
            return new ZeroArgValueNode(token, false) {
                @Override protected double get() {
                    return Math.random();
                }
            };
        }
    });

    public static final NArgParser SINH = new NArgParser(1, new CreateNArgFunctionFn("sinh") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.sinh(arg1.get());
                }
            };
        }
    });

    public static final NArgParser LOG = new NArgParser(1, new CreateNArgFunctionFn("log") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.log(arg1.get());
                }
            };
        }
    });

    public static final NArgParser LOG10 = new NArgParser(1, new CreateNArgFunctionFn("log10") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.log10(arg1.get());
                }
            };
        }
    });

    public static final NArgParser EXP = new NArgParser(1, new CreateNArgFunctionFn("exp") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.exp(arg1.get());
                }
            };
        }
    });

    public static final NArgParser FLOOR = new NArgParser(1, new CreateNArgFunctionFn("floor") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.floor(arg1.get());
                }
            };
        }
    });

    public static final NArgParser CBRT = new NArgParser(1, new CreateNArgFunctionFn("cbrt") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.cbrt(arg1.get());
                }
            };
        }
    });

    public static final NArgParser CEIL = new NArgParser(1, new CreateNArgFunctionFn("ceil") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.ceil(arg1.get());
                }
            };
        }
    });

    public static final NArgParser COS = new NArgParser(1, new CreateNArgFunctionFn("cos") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.cos(arg1.get());
                }
            };
        }
    });

    public static final NArgParser COSH = new NArgParser(1, new CreateNArgFunctionFn("cosh") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.cosh(arg1.get());
                }
            };
        }
    });

    public static final NArgParser ABS = new NArgParser(1, new CreateNArgFunctionFn("abs") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.abs(arg1.get());
                }
            };
        }
    });

    public static final NArgParser ASIN = new NArgParser(1, new CreateNArgFunctionFn("asin") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.asin(arg1.get());
                }
            };
        }
    });

    public static final NArgParser ACOS = new NArgParser(1, new CreateNArgFunctionFn("acos") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.acos(arg1.get());
                }
            };
        }
    });

    public static final NArgParser ATAN = new NArgParser(1, new CreateNArgFunctionFn("atan") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.atan(arg1.get());
                }
            };
        }
    });

    public static final NArgParser ATAN2 = new NArgParser(2, new CreateNArgFunctionFn("atan2") {
        @Override protected ValueNode fn(List<Node> args) {
            return new TwoArgValueNode(token, args.get(0), args.get(1)) {
                @Override protected double get() {
                    return Math.atan2(arg1.get(), arg2.get());
                }
            };
        }
    });

    public static final NArgParser SIN = new NArgParser(1, new CreateNArgFunctionFn("sin") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return Math.sin(arg1.get());
                }
            };
        }
    });
    
    public static final NArgParser POW = new NArgParser(2, new CreateNArgFunctionFn("pow") {
        @Override protected ValueNode fn(List<Node> args) {
            return new TwoArgValueNode(token, args.get(0), args.get(1)) {
                @Override protected double get() {
                    return Math.pow(arg1.get(), arg2.get());
                }
            };
        }
    });

    public static final NArgParser SECANT = new NArgParser(1, new CreateNArgFunctionFn("sec") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return 1 / Math.cos(arg1.get());
                }
            };
        }
    });
    public static final NArgParser COSECANT = new NArgParser(1, new CreateNArgFunctionFn("cosec") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return 1 / Math.sin(arg1.get());
                }
            };
        }
    });
    public static final NArgParser COTAN = new NArgParser(1, new CreateNArgFunctionFn("cot") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    return 1 / Math.tan(arg1.get());
                }
            };
        }
    });
    public static final NArgParser ASINH = new NArgParser(1, new CreateNArgFunctionFn("asinh") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    double x = arg1.get();

                    return Math.log(x + Math.sqrt(x * x + 1.0));
                }
            };
        }
    });
    public static final NArgParser ACOSH = new NArgParser(1, new CreateNArgFunctionFn("acosh") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    double x = arg1.get();

                    return Math.log(x + Math.sqrt(x * x - 1.0));
                }
            };
        }
    });
    public static final NArgParser ATANH = new NArgParser(1, new CreateNArgFunctionFn("atanh") {
        @Override protected ValueNode fn(List<Node> args) {
            return new OneArgValueNode(token, args.get(0)) {
                @Override protected double get() {
                    double x = arg1.get();

                    return 0.5 * Math.log((1.0 + x) / (1.0 - x));
                }
            };
        }
    });
}