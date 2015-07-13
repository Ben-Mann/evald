package net.benmann.evald.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.benmann.evald.ArgFunction.NArgFunction;
import net.benmann.evald.ArgFunction.OneArgFunction;
import net.benmann.evald.ArgFunction.TwoArgFunction;
import net.benmann.evald.Evald;
import net.benmann.evald.EvaldException;
import net.benmann.evald.EvaldException.InvalidTokenEvaldException;
import net.benmann.evald.EvaldException.OperatorExpectedEvaldException;
import net.benmann.evald.EvaldException.UndeclaredVariableEvaldException;
import net.benmann.evald.EvaldException.UnknownMethodEvaldException;
import net.benmann.evald.Library;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class PublicAPITests {
    static final double DEFAULT_PRECISION = 0.00001;

    @Test public void testEmptyExpression() {
        Evald evald = new Evald();
        try {
            evald.parse("a#");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(OperatorExpectedEvaldException.class));
        }
    }

    @Test public void testBasic() {
        Evald evald = new Evald();
        evald.parse("a * b + c");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 3.0);
        evald.addVariable("c", 7.0);
        assertEquals(2 * 3 + 7.0, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testPrecedence() {
        Evald evald = new Evald();
        evald.parse("a * b + c");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 3.0);
        evald.addVariable("c", 7.0);
        assertEquals((2.0 * 3.0) + 7.0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a + b * c");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 3.0);
        evald.addVariable("c", 7.0);
        assertEquals(2.0 + (3.0 * 7.0), evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testSin() {
        Evald evald = new Evald();
        evald.addLibrary(Library.MATH);
        evald.parse("x * sin(y)");
        evald.addVariable("x", 5.0);
        evald.addVariable("y", 0.5);
        assertEquals(5 * Math.sin(0.5), evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testRemove() {
        Evald evald = new Evald();
        evald.addLibrary(Library.MATH);
        evald.parse("cos(x) * sin(y)");
        evald.addVariable("x", 0.74);
        evald.addVariable("y", 0.35);
        assertEquals(Math.cos(0.74) * Math.sin(0.35), evald.evaluate(), DEFAULT_PRECISION);
        evald.removeLibrary(Library.MATH);
        try {
            evald.parse("cos(x)");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(UnknownMethodEvaldException.class));
        }
        try {
            evald.parse("sin(x)");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(UnknownMethodEvaldException.class));
        }
    }

    @Test public void testPow() {
        Evald evald = new Evald();
        evald.addLibrary(Library.MATH);
        evald.parse("x * pow(y, z)");
        evald.addVariable("x", 0.3);
        evald.addVariable("y", 0.7);
        evald.addVariable("z", 1.8);
        assertEquals(0.3 * Math.pow(0.7, 1.8), evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("x * y ^ z");
        assertEquals(0.3 * Math.pow(0.7, 1.8), evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testOneArgFunction() {
        Evald evald = new Evald();
        evald.addUserFunction(new OneArgFunction("foo") {
            @Override public Double get(Double value) {
                return value + 2;
            }
        });
        evald.parse("x * foo(y)");
        evald.addVariable("x", 0.1);
        evald.addVariable("y", 0.7);
        assertEquals(0.1 * (0.7 + 2), evald.evaluate(), DEFAULT_PRECISION);
        try {
            evald.parse("x * foo(y,z)");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(EvaldException.class));
        }
    }

    @Test public void testCaseSensitivity() {
        Evald evald = new Evald();
        evald.addUserFunction(new OneArgFunction("foo") {
            @Override public Double get(Double value) {
                return value / 3;
            }
        });
        evald.addUserFunction(new OneArgFunction("Foo") {
            @Override public Double get(Double value) {
                return value * 11;
            }
        });
        evald.parse("FOO + Foo(1) + foo(2)");
        evald.addVariable("FOO", 0.1);
        assertEquals(0.1 + (1.0 * 11.0) + (2.0 / 3.0), evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testNArgFunction() {
        Evald evald = new Evald();
        evald.addUserFunction(new NArgFunction("sum") {
            @Override public Double get(Double... values) {
                Double sum = 0.0;
                for (Double value : values) {
                    sum += value;
                }
                return sum;
            }
        });
        evald.parse("x * sum(a,b,c,d,e)");
        evald.addVariable("x", 0.1);
        evald.addVariable("a", 0.7);
        evald.addVariable("b", 2.0);
        evald.addVariable("c", 3.0);
        evald.addVariable("d", 5.0);
        evald.addVariable("e", 7.0);
        assertEquals(0.1 * (0.7 + 2.0 + 3.0 + 5.0 + 7.0), evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testTwoArgFunction() {
        Evald evald = new Evald();
        evald.addUserFunction(new TwoArgFunction("bar") {
            @Override public Double get(Double arg1, Double arg2) {
                return arg1 / arg2;
            }
        });
        evald.parse("x * bar(y,z)");
        evald.addVariable("x", 0.1);
        evald.addVariable("y", 0.7);
        evald.addVariable("z", 0.2);
        assertEquals(0.1 * (0.7 / 0.2), evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testMissingLibrary() {
        Evald evald = new Evald();
        try {
            evald.parse("x * pow(y, z)");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(UnknownMethodEvaldException.class));
        }
    }

    @Test public void testNull() {
        Evald evald = new Evald();
        evald.parse("x");
        evald.addVariable("x", (Double) null);
        assertNull(evald.evaluate());
        evald.addLibrary(Library.LOGIC);
        evald.parse("isnull(x)");
        assertEquals(1.0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("x * y");
        evald.addVariable("y", 5);
        try {
            assertNull(evald.evaluate());
            fail("Expected a null pointer exception");
        } catch (Throwable t) {
            assertThat(t, instanceOf(NullPointerException.class));
        }
        evald.addVariable("x", 1);
        evald.parse("isnull(x)");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testListUndeclared() {
        Evald evald = new Evald();
        assertEquals(true, evald.getAllowUndeclared());
        evald.setAllowUndeclared(true);
        assertEquals(true, evald.getAllowUndeclared());
        evald.parse("abc * def + g / h");
        Set<String> vars = new HashSet<String>(Arrays.asList(evald.listUndeclared()));
        assertEquals(4, vars.size());
        assertTrue(vars.contains("abc"));
        assertTrue(vars.contains("def"));
        assertTrue(vars.contains("g"));
        assertTrue(vars.contains("h"));
    }

    @Test public void testMissingVariable() {
        Evald evald = new Evald();
        assertEquals(true, evald.getAllowUndeclared());
        evald.setAllowUndeclared(false);
        assertEquals(false, evald.getAllowUndeclared());
        evald.addVariable("xxxx", 0.3);
        try {
            evald.parse("xxxx * yyyy + zzzz");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(UndeclaredVariableEvaldException.class));
            assertThat(t.getMessage(), CoreMatchers.not(CoreMatchers.containsString("xxxx")));
            assertThat(t.getMessage(), CoreMatchers.containsString("yyyy"));
            assertThat(t.getMessage(), CoreMatchers.containsString("zzzz"));
        }
    }

    @Test public void testImplicitMultiplication() {
        Evald evald = new Evald();
        assertEquals(true, evald.getImplicitMultiplication());
        evald.setImplicitMultiplication(false);
        assertEquals(false, evald.getImplicitMultiplication());
        evald.addVariable("a", 2);
        evald.addVariable("b", 3);
        try {
            evald.parse("a b");
            fail("Expected an operator expected exception");
        } catch (Throwable t) {
            assertThat(t, instanceOf(OperatorExpectedEvaldException.class));
        }

        evald.setImplicitMultiplication(true);
        assertEquals(true, evald.getImplicitMultiplication());
        evald.parse("a b");
        assertEquals(2.0 * 3.0, evald.evaluate(), DEFAULT_PRECISION);

    }

    @Test public void testImplicitVariable() {
        Evald evald = new Evald();
        evald.setAllowUndeclared(true);
        evald.addVariable("x", 0.3);
        evald.addVariable("y", 0.7);
        evald.parse("x * y + z");
        assertEquals(0.3 * 0.7 + 0, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testNegative() {
        Evald evald = new Evald();
        evald.parse("-a * b + -c");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 3.0);
        evald.addVariable("c", 7.0);
        assertEquals(-2 * 3 + -7.0, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testInf() {
        Evald evald = new Evald(Library.CORE, Library.LOGIC);
        evald.parse("if(isinf(a/b),2,3)");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 0.0);
        assertEquals(2, evald.evaluate(), DEFAULT_PRECISION);
        evald.addVariable("b", 1.0);
        assertEquals(3, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("isinf(1/0)");
        assertEquals(1, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("isinf(1/1)");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testNaN() {
        Evald evald = new Evald(Library.ALL);
        evald.parse("if(isnan(sqrt(a)),2,3)");
        evald.addVariable("a", -1.0);
        assertEquals(2, evald.evaluate(), DEFAULT_PRECISION);
        evald.addVariable("a", 1.0);
        assertEquals(3, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("isnan(nan)");
        assertEquals(1, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("isnan(1)");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testNegativeBraces() {
        Evald evald = new Evald();
        evald.parse("-a * b + (-c)");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 3.0);
        evald.addVariable("c", 7.0);
        assertEquals(-2 * 3 + (-7.0), evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testBraces() {
        Evald evald = new Evald();
        evald.parse("2 * (a + (b - (c / (d + 4)) / 7) * (5 + (((6))))) / 3");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 3.0);
        evald.addVariable("c", 7.0);
        evald.addVariable("d", 23.0);
        double a = 2.0;
        double b = 3.0;
        double c = 7.0;
        double d = 23.0;
        assertEquals(2 * (a + (b - (c / (d + 4)) / 7) * (5 + (((6))))) / 3, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testPositive() {
        Evald evald = new Evald();
        evald.parse("+a * b + +c");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 3.0);
        evald.addVariable("c", 7.0);
        assertEquals(2 * 3 + +7.0, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testReplaceSin() {
        Evald evald = new Evald(Library.ALL);
        evald.parse("2*sin(a)");
        evald.addVariable("a", 45.0);
        assertEquals(2 * Math.sin(45), evald.evaluate(), DEFAULT_PRECISION);
        //replace sin()
        evald.addUserFunction(new OneArgFunction("sin") {
            @Override protected Double get(Double value) {
                return value + 1;
            }
        });
        //won't affect evaluation of existing expression
        assertEquals(2 * Math.sin(45), evald.evaluate(), DEFAULT_PRECISION);
        //will affect any subsequent parsing/expression
        evald.parse("2*sin(a)");
        assertEquals(2 * (45 + 1), evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testAddInvalidFunctionToken() {
        Evald evald = new Evald();
        try {
            evald.addUserFunction(new OneArgFunction("value+1") {
                @Override protected Double get(Double value) {
                    return value + 1;
                }
            });

            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(InvalidTokenEvaldException.class));
        }
        try {
            evald.addUserFunction(new OneArgFunction("#value1") {
                @Override protected Double get(Double value) {
                    return value + 1;
                }
            });

            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(InvalidTokenEvaldException.class));
        }
        //legal
        evald.addUserFunction(new OneArgFunction("_Value_1") {
            @Override protected Double get(Double value) {
                return value + 1;
            }
        });
        evald.addVariable("Value_2", 3);
        evald.addVariable("b", 2);
        evald.parse("b * _Value_1(Value_2)");
        assertEquals(2 * (3 + 1), evald.evaluate(), DEFAULT_PRECISION);
        //Same for variables
        try {
            evald.addVariable("value+1");
        } catch (Throwable t) {
            assertThat(t, instanceOf(InvalidTokenEvaldException.class));
        }
    }

    @Test public void testRemoveSin() {
        Evald evald = new Evald(Library.ALL);
        evald.parse("2*sin(a)");
        evald.addVariable("a", 45.0);
        assertEquals(2 * Math.sin(45), evald.evaluate(), DEFAULT_PRECISION);
        //remove sin()
        evald.removeFunction("sin");
        //won't affect evaluation of existing expression
        assertEquals(2 * Math.sin(45), evald.evaluate(), DEFAULT_PRECISION);
        //will effect parsing
        try {
            evald.parse("2*sin(a)");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(UnknownMethodEvaldException.class));
        }
    }

    @Test public void testIf() {
        Evald evald = new Evald();
        evald.addLibrary(Library.LOGIC);
        evald.parse("if(1,2,3)");
        assertEquals(2, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("if(a,b,c)");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 3.0);
        evald.addVariable("c", 7.0);
        assertEquals(3, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("if(a,b,c)");
        evald.addVariable("a", 0.0);
        assertEquals(7, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("if(a,b,c)");
        evald.addVariable("a", Double.NaN);
        assertEquals(7, evald.evaluate(), DEFAULT_PRECISION);
        evald.addVariable("a", Double.POSITIVE_INFINITY);
        assertEquals(3, evald.evaluate(), DEFAULT_PRECISION);
        evald.addVariable("a", Double.NEGATIVE_INFINITY);
        assertEquals(3, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("if(a,b,1+c*2)");
        evald.addVariable("a", 0.0);
        assertEquals(1 + 2 * 7, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testMod() {
        Evald evald = new Evald();
        evald.parse("a % b");
        evald.addVariable("a", 2.5);
        evald.addVariable("b", 0.2);
        assertEquals(2.5 % 0.2, evald.evaluate(), DEFAULT_PRECISION);
        evald.addLibrary(Library.MATH);
        evald.parse("mod(a,b)");
        assertEquals(2.5 % 0.2, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testConditionalLogic() {
        Evald evald = new Evald();
        evald.addLibrary(Library.LOGIC);
        evald.parse("1 && 2");
        assertEquals(1, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("1 && 0");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("0 && 0");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("(1/0) && 0");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("false");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("true");
        assertEquals(1, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("false && false");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("true && false");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("false && true");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("true && true");
        assertEquals(1, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("false || false");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("true || false");
        assertEquals(1, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("false || true");
        assertEquals(1, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("true || true");
        assertEquals(1, evald.evaluate(), DEFAULT_PRECISION);
    }

    private void testConditional(String expression, boolean expect) {
        Evald evald = new Evald();
        evald.setAllowUndeclared(false);
        evald.addLibrary(Library.LOGIC);
        evald.parse("if(" + expression + ",1,0)");
        assertEquals(expect ? 1 : 0, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testLibConditional() {
        Evald evald = new Evald();
        evald.addLibrary(Library.LOGIC);
        testConditional("1==1", true);
        testConditional("1==2", false);
        testConditional("1==nan", false);
        testConditional("nan==nan", false);
        testConditional("nan==0", false);

        testConditional("1!=0", true);
        testConditional("1!=1", false);
        testConditional("1!=nan", true);
        testConditional("nan!=nan", true);
        testConditional("nan!=0", true);

        testConditional("1<=1", true);
        testConditional("1<=2", true);
        testConditional("1<=0", false);
        testConditional("nan<=1", false);
        testConditional("1<=nan", false);
        testConditional("nan<=nan", false);

        testConditional("1<1", false);
        testConditional("1<2", true);
        testConditional("1<0", false);
        testConditional("nan<1", false);
        testConditional("1<nan", false);
        testConditional("nan<nan", false);

        testConditional("1>1", false);
        testConditional("1>2", false);
        testConditional("1>0", true);
        testConditional("nan>1", false);
        testConditional("1>nan", false);
        testConditional("nan>nan", false);

        testConditional("1>=1", true);
        testConditional("1>=2", false);
        testConditional("1>=0", true);
        testConditional("nan>=1", false);
        testConditional("1>=nan", false);
        testConditional("nan>=nan", false);

        testConditional("!true", false);
        testConditional("!false", true);
        testConditional("true", true);
        testConditional("false", false);
    }

    private void testBinary(String expression, int expect) {
        Evald evald = new Evald();
        evald.setAllowUndeclared(false);
        evald.addLibrary(Library.LOGIC, Library.BINARY);
        evald.parse(expression);
        assertEquals(expect, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testLibBinary() {
        testBinary("1 xor 1", 0);
        testBinary("1 xor 0", 1);
        testBinary("0 xor 1", 1);
        testBinary("0 xor 0", 0);
        testBinary("3 xor 9", 10);

        testBinary("~0", -1);
        testBinary("~9", -10);

        testBinary("3 | 9", 11);
        testBinary("3 & 9", 1);
    }

    private void checkMath(Evald evald, String expression, double expectedResult) {
        evald.parse(expression);
        assertEquals(expectedResult, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testMathLibrary() {
        Evald evald = new Evald(Library.MATH, Library.LOGIC, Library.CORE);
        //SIN, POW, E, PI, ABS, ACOS, ASIN, ATAN, ATAN2, CBRT,
        //CEIL, COS, COSH, EXP, FLOOR, HYPOT, LOG, LOG10, MAX, 
        //MIN, RANDOM, ROUND, SINH, SQRT, TAN, TANH, TODEGREES, TORADIANS };
        double a = 11.0;
        double b = -0.5;
        double c = 1.0;
        evald.addVariable("a", a);
        evald.addVariable("b", b);
        evald.addVariable("c", c);

        checkMath(evald, "b*sin(a)", b * Math.sin(a));
        checkMath(evald, "b*cos(a)", b * Math.cos(a));
        checkMath(evald, "b*e+a", b * Math.E + a);
        checkMath(evald, "b*pi+a", b * Math.PI + a);
        checkMath(evald, "abs(b*a)-c", Math.abs(b * a) - c);
        checkMath(evald, "b+acos(a)", b + Math.acos(a));
        checkMath(evald, "b/asin(a)", b / Math.asin(a));
        checkMath(evald, "b-atan(a)", b - Math.atan(a));
        checkMath(evald, "b-atan2(a,c)", b - Math.atan2(a, c));
        checkMath(evald, "c+cbrt(a)", c + Math.cbrt(a));
        checkMath(evald, "c-ceil(b)", c - Math.ceil(b));
        checkMath(evald, "b*cosh(a)", b * Math.cosh(a));
        checkMath(evald, "b+exp(a)", b + Math.exp(a));
        checkMath(evald, "floor(a+b)", Math.floor(a + b));
        checkMath(evald, "hypot(a,b+c)", Math.hypot(a, b + c));
        checkMath(evald, "log(a*b+c)", Math.log(a * b + c));
        checkMath(evald, "log10(a+b*c)", Math.log10(a + b * c));
        checkMath(evald, "max(b,c)", Math.max(b, c));
        checkMath(evald, "min(b-c, c-b)", Math.min(b - c, c - b));
        //random's a bit different...
        evald.parse("random()");
        double x = evald.evaluate();
        //FIXME There's a small chance this can fail. Is there a better way?
        assertNotEquals(x, evald.evaluate(), DEFAULT_PRECISION);

        checkMath(evald, "round(b*a)", ((Long) Math.round(b * a)).doubleValue());
        checkMath(evald, "sinh(a*b)", Math.sinh(a * b));
        checkMath(evald, "sqrt(a+c)*b", Math.sqrt(a + c) * b);
        checkMath(evald, "tan(c)", Math.tan(c));
        checkMath(evald, "tanh(a+c)", Math.tanh(a + c));
        checkMath(evald, "toDegrees(b)*a", Math.toDegrees(b) * a);
        checkMath(evald, "toRadians(a)*b", Math.toRadians(a) * b);
    }

    @Test public void testAddVariableTypes() {
        Evald evald = new Evald();
        evald.addVariable("a", 1);
        evald.addVariable("b", 2.0f);
        evald.addVariable("c", 3.0);
        evald.parse("a+b+c");
        assertEquals(1 * 2 * 3, evald.evaluate(), DEFAULT_PRECISION);
    }
}