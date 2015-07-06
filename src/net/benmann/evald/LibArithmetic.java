package net.benmann.evald;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Arithmetic library.
 * 
 * Adds support for:
 *  basic operators: + - * / ^ 
 *  braces: ( )
 *  constants: nan
 */
class LibArithmetic extends Library {
    @Override Parser[] getParsers() {
        return new Parser[] { CONSTANT, NaN, MOD, ADD, SUBTRACT, MULTIPLY, DIVIDE, POSITIVE, NEGATIVE, POW, BRACES };
    }

    public static final ConstantParser NaN = new ConstantParser("nan") {
        @Override protected Constant create() {
            return new Constant(Double.NaN);
        }
    };

    public static final BinaryOperatorParser ADD = new BinaryOperatorParser("+") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.ADDITIVE) {
                @Override protected Double get() {
                    return a.get() + b.get();
                }

                @Override Node collapse() {
                    a = a.collapse();
                    b = b.collapse();

                    if (!a.isConstant && !b.isConstant)
                        return this;

                    if (a.isConstant && b.isConstant)
                        return new Constant(get());

                    Node constantNode = a.isConstant ? a : b;
                    Node variableNode = a.isConstant ? b : a;

                    Double value = constantNode.get();
                    if (value.doubleValue() == 0)
                        return variableNode;

                    return this;
                }

            };
        }
    };

    public static final BinaryOperatorParser MOD = new BinaryOperatorParser("%") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.MULTIPLICATIVE) {
                @Override protected Double get() {
                    return a.get() % b.get();
                }

                @Override Node collapse() {
                    a = a.collapse();
                    b = b.collapse();

                    if (!a.isConstant && !b.isConstant)
                        return this;

                    if (a.isConstant && b.isConstant)
                        return new Constant(get());

                    if (a.isConstant && a.get().doubleValue() == 0)
                        return new Constant(0.0);

                    return this;
                }

            };
        }
    };

    public static final BinaryOperatorParser POW = new BinaryOperatorParser("^") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.POWER) {
                @Override protected Double get() {
                    return Math.pow(a.get(), b.get());
                }
                
                @Override Node collapse() {
                    a = a.collapse();
                    b = b.collapse();

                    if (!a.isConstant && !b.isConstant)
                        return this;

                    if (a.isConstant && b.isConstant)
                        return new Constant(get());

                    if (!b.isConstant)
                        return this;
                    
                    if (b.get().doubleValue() == 0)
                        return new Constant(1.0);
                    
                    if (b.get().doubleValue() == 1)
                        return a;

                    return this;
                }

            };
        }
    };

    public static final BinaryOperatorParser SUBTRACT = new BinaryOperatorParser("-") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.ADDITIVE) {
                @Override protected Double get() {
                    return a.get() - b.get();
                }

                @Override Node collapse() {
                    a = a.collapse();
                    b = b.collapse();

                    if (!a.isConstant && !b.isConstant)
                        return this;

                    if (a.isConstant && b.isConstant)
                        return new Constant(get());

                    Node constantNode = a.isConstant ? a : b;
                    Node variableNode = a.isConstant ? b : a;

                    Double value = constantNode.get();
                    if (value.doubleValue() == 0)
                        return variableNode;

                    return this;
                }

            };
        }
    };

    public static final BinaryOperatorParser MULTIPLY = new BinaryOperatorParser("*") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.MULTIPLICATIVE) {
                @Override protected Double get() {
                    return a.get() * b.get();
                }

                @Override Node collapse() {
                    a = a.collapse();
                    b = b.collapse();

                    if (!a.isConstant && !b.isConstant)
                        return this;

                    if (a.isConstant && b.isConstant)
                        return new Constant(get());

                    Node constantNode = a.isConstant ? a : b;
                    Node variableNode = a.isConstant ? b : a;

                    Double value = constantNode.get();
                    if (value.doubleValue() == 0)
                        return new Constant(0.0);
                    if (value.doubleValue() == 1)
                        return variableNode;

                    return this;
                }
            };
        }
    };

    public static final BinaryOperatorParser DIVIDE = new BinaryOperatorParser("/") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.MULTIPLICATIVE) {
                @Override protected Double get() {
                    return a.get() / b.get();
                }

                @Override Node collapse() {
                    a = a.collapse();
                    b = b.collapse();

                    if (!a.isConstant && !b.isConstant)
                        return this;

                    if (a.isConstant && b.isConstant)
                        return new Constant(get());

                    if (a.isConstant && a.get().doubleValue() == 0)
                        return new Constant(0.0);

                    if (b.isConstant && b.get().doubleValue() == 1)
                        return a;

                    return this;
                }

            };
        }
    };

    public static final PrefixOperatorParser POSITIVE = new PrefixOperatorParser("+") {
        @Override protected PrefixOperatorNode create() {
            return new PrefixOperatorNode(token) {
                @Override protected Double get() {
                    return b.get();
                }

                @Override Node collapse() {
                    return b;
                }
            };
        }
    };

    public static final PrefixOperatorParser NEGATIVE = new PrefixOperatorParser("-") {
        @Override protected PrefixOperatorNode create() {
            return new PrefixOperatorNode(token) {
                @Override protected Double get() {
                    return -b.get();
                }
            };
        }
    };
    
    public static final ValueParser BRACES = new ValueParser(null) {
        @Override Node parse(ExpressionParser operationParser, ExpressionString str) {
            Character ch = str.expression.charAt(0);
            if (ch != '(')
                return null;

            //Lookahead to matching closing brace
            int brace = 0;
            String content = str.expression;
            int closingBraceIndex = 1;
            while (closingBraceIndex < content.length()) {
                char bch = content.charAt(closingBraceIndex);

                if (bch == '(') {
                    brace++;
                } else if (bch == ')') {
                    brace--;
                }

                if (brace == -1)
                    break;

                closingBraceIndex++;
            }

            if (brace != -1)
                throw new EvaldException("Mismatched braces from " + content);

            if (closingBraceIndex == 1) {
                throw new EvaldException("Empty braces - a value was expected.");
            }

            //We need to return an operation for all of content.
            //nest parsing. it's what ExpressionString is for.
            content = str.expression.substring(1, closingBraceIndex);

            str.update(content.length() + 2);
            return operationParser.parse(content);
        }
    };

    //FIXME support exponents, ie 1.5E+35, or else don't, explicitly
    private static final Pattern constantPattern = Pattern.compile("[0-9]*\\.?[0-9]+");
    public static final ValueParser CONSTANT = new ValueParser(null) {
        @Override Node parse(ExpressionParser operationParser, ExpressionString str) {
            Character ch = str.expression.charAt(0);
            if (!Character.isDigit(ch))
                return null;

            //Find a value
            Matcher matcher = constantPattern.matcher(str.expression);
            if (!matcher.find())
                return null;

            final String content = matcher.group();

            str.update(content.length());
            return new Constant(Double.parseDouble(content));
        }
    };
}