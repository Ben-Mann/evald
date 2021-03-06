package net.benmann.evald;

/**
 * Binary operators. Note that these all treat the double value as a long integer
 */
public final class LibBinary extends Library {
    @Override Parser[] getParsers() {
        return new Parser[] { XOR, COMPLEMENT, OR, AND };
    }

    private static long longValue(Node n) {
        return (long) n.get();
    }

    private static double toDouble(long l) {
        return new Long(l).doubleValue();
    }

    //^ conflicts with our arithmetic power operator; 
    public static final BinaryOperatorParser XOR = new BinaryOperatorParser("xor") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.BITWISE_XOR) {
                @Override protected double get() {
                    return toDouble(longValue(a) ^ longValue(b));
                }
            };
        }
    };

    public static final PrefixOperatorParser COMPLEMENT = new PrefixOperatorParser("~") {
        @Override protected PrefixOperatorNode create() {
            return new PrefixOperatorNode(token) {
                @Override protected double get() {
                    return toDouble(~longValue(b));
                }
            };
        }
    };

    public static final BinaryOperatorParser OR = new BinaryOperatorParser("|") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.BITWISE_OR) {
                @Override protected double get() {
                    return toDouble(longValue(a) | longValue(b));
                }
            };
        }
    };

    public static final BinaryOperatorParser AND = new BinaryOperatorParser("&") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.BITWISE_AND) {
                @Override protected double get() {
                    return toDouble(longValue(a) & longValue(b));
                }
            };
        }
    };
}