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
public final class LibArithmetic extends Library {
    @Override Parser[] getParsers() {
        return new Parser[] { CONSTANT, CONSTANTSN, NaN, MOD, ADD, SUBTRACT, MULTIPLY, DIVIDE, POSITIVE, NEGATIVE, POW, BRACES };
    }

    public static final ConstantParser NaN = new ConstantParser("nan") {
        @Override protected Constant create() {
            return new Constant(Double.NaN);
        }
    };

    public static final BinaryOperatorParser ADD = new BinaryOperatorParser("+") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.ADDITIVE) {
                @Override protected double get() {
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

                    double value = constantNode.get();
                    if (value == 0)
                        return variableNode;

                    return this;
                }

            };
        }
    };

    public static final BinaryOperatorParser MOD = new BinaryOperatorParser("%") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.MULTIPLICATIVE) {
                @Override protected double get() {
                    return a.get() % b.get();
                }

                @Override Node collapse() {
                    a = a.collapse();
                    b = b.collapse();

                    if (!a.isConstant && !b.isConstant)
                        return this;

                    if (a.isConstant && b.isConstant)
                        return new Constant(get());

                    if (a.isConstant && a.get() == 0)
                        return new Constant(0.0);

                    return this;
                }

            };
        }
    };

    public static final BinaryOperatorParser POW = new BinaryOperatorParser("^") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.POWER) {
                @Override protected double get() {
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
                    
                    if (b.get() == 0)
                        return new Constant(1.0);
                    
                    if (b.get() == 1)
                        return a;

                    return this;
                }

            };
        }
    };

    public static final BinaryOperatorParser SUBTRACT = new BinaryOperatorParser("-") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.ADDITIVE) {
                @Override protected double get() {
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

                    double value = constantNode.get();
                    if (value == 0)
                        return variableNode;

                    return this;
                }

            };
        }
    };

    public static final BinaryOperatorParser MULTIPLY = new BinaryOperatorParser("*") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.MULTIPLICATIVE) {
                @Override protected double get() {
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

                    double value = constantNode.get();
                    if (value == 0)
                        return new Constant(0.0);
                    if (value == 1)
                        return variableNode;

                    return this;
                }
            };
        }
    };

    public static final BinaryOperatorParser DIVIDE = new BinaryOperatorParser("/") {
        @Override protected BinaryOperatorNode create() {
            return new BinaryOperatorNode(token, Precedence.MULTIPLICATIVE) {
                @Override protected double get() {
                    return a.get() / b.get();
                }

                @Override Node collapse() {
                    a = a.collapse();
                    b = b.collapse();

                    if (!a.isConstant && !b.isConstant)
                        return this;

                    if (a.isConstant && b.isConstant)
                        return new Constant(get());

                    if (a.isConstant && a.get() == 0)
                        return new Constant(0.0);

                    if (b.isConstant && b.get() == 1)
                        return a;

                    return this;
                }

            };
        }
    };

    public static final PrefixOperatorParser POSITIVE = new PrefixOperatorParser("+") {
        @Override protected PrefixOperatorNode create() {
            return new PrefixOperatorNode(token) {
                @Override protected double get() {
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
                @Override protected double get() {
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
    public static final ValueParser CONSTANT = new ValueParser(constantPattern.toString()) {
        @Override Node parse(ExpressionParser operationParser, ExpressionString str) {
            Character ch = str.expression.charAt(0);
            if (!Character.isDigit(ch) && !ch.equals('.'))
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

    //Scientific Notation Version
    private static final Pattern constantSNPattern = Pattern.compile("[0-9]*\\.?[0-9]+[eE][\\+\\-][0-9]*\\.?[0-9]+");
    public static final ValueParser CONSTANTSN = new ValueParser(constantSNPattern.toString()) {
        @Override Node parse(ExpressionParser operationParser, ExpressionString str) {
            Character ch = str.expression.charAt(0);
            if (!Character.isDigit(ch) && !ch.equals('.'))
                return null;

            //Find a value
            Matcher matcher = constantSNPattern.matcher(str.expression);
            if (!matcher.find())
                return null;

            final String content = matcher.group();
            //split at e
            String[] parts = content.split("[eE]");

            double m = Double.parseDouble(parts[0]);
            double exp = Double.parseDouble(parts[1]);
            double value = m * Math.pow(10, exp);

            str.update(content.length());
            return new Constant(value);
        }
    };
}