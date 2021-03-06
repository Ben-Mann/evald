package net.benmann.evald.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.benmann.evald.AbstractEvaldException.EmptyExpressionEvaldException;
import net.benmann.evald.AbstractEvaldException.EvaldException;
import net.benmann.evald.AbstractEvaldException.InvalidTokenEvaldException;
import net.benmann.evald.AbstractEvaldException.OperatorExpectedEvaldException;
import net.benmann.evald.AbstractEvaldException.UndeclaredVariableEvaldException;
import net.benmann.evald.AbstractEvaldException.UninitialisedEvaldException;
import net.benmann.evald.AbstractEvaldException.UnknownMethodEvaldException;
import net.benmann.evald.ArgFunction.ImpureNArgFunction;
import net.benmann.evald.ArgFunction.NArgFunction;
import net.benmann.evald.ArgFunction.OneArgFunction;
import net.benmann.evald.ArgFunction.TwoArgFunction;
import net.benmann.evald.Evald;
import net.benmann.evald.Library;

public class PublicAPITests {
    static final double DEFAULT_PRECISION = 0.00001;

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test public void testEmptyExpression() {
        Evald evald = new Evald();
        try {
            evald.parse("a#");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(EvaldException.class));
        }
        try {
            evald.parse("");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(EmptyExpressionEvaldException.class));
        }
    }

    @Test public void testBasic() {
        Evald evald = new Evald();
        evald.parse("a * b + c");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 3.0);
        evald.addVariable("c", 7.0);
        assertEquals(2 * 3 + 7.0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a*.75");
        assertEquals(2 * 0.75, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a*0.750");
        assertEquals(2 * 0.75, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a*0.750000000000000001");
        assertEquals(2 * 0.75, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a*7.5e-1");
        assertEquals(2 * 0.75, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a*.0075e+2");
        assertEquals(2 * 0.75, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a*1.510e+3");
        assertEquals(2 * 1510, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a*1.510e+23");
        assertEquals(2 * 1.51e+23, evald.evaluate(), DEFAULT_PRECISION * Math.pow(10, 23));
        evald.parse("a*1.45767979e+3");
        assertEquals(2 * 1.45767979e+3, evald.evaluate(), DEFAULT_PRECISION * Math.pow(10, 23));
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
        evald.parse("-a^2");
        assertEquals(-Math.pow(2, 2), evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testPrefix() {
        Evald evald = new Evald();
        evald.parse("a+b");
        evald.addVariable("a", -1);
        evald.addVariable("b", -2);
        assertEquals(-3, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("-1+a+b+-4");
        assertEquals(-8, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("(a+b*10)/2");
        assertEquals((-1.0 + (-2.0 * 10.0)) / 2.0, evald.evaluate(), DEFAULT_PRECISION);
        evald.addVariable("a", 11.039);
        evald.addVariable("b", -248.597);
        evald.parse("(-100000-a+b*333.3)/1000");
        assertEquals((-100000 - 11.039 + (-248.597) * 333.3) / 1000, evald.evaluate(), DEFAULT_PRECISION);
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
        evald.setImplicitMultiplication(false);
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
        evald.setImplicitMultiplication(true);
        evald.setAllowUndeclared(false);
        try {
            evald.parse("cos(x)");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(UndeclaredVariableEvaldException.class));
            assertTrue(t.getMessage().contains("cos"));
        }
    }

    @Test public void testPrecedence2() {
        Evald evald = new Evald(Library.CORE, Library.LOGIC, Library.MATH);
        evald.addVariable("v", 5);
        evald.parse("-v^2");
        assertEquals(-Math.pow(5, 2), evald.evaluate(), DEFAULT_PRECISION);

    }

    static public class Rint extends OneArgFunction {
        public Rint() {
            super("rint");
        }

        @Override public double get(double a) {
            return (double) Math.rint(a);
        }
    }
    @Test public void testExtra2() {
        Evald evald = new Evald(Library.CORE, Library.LOGIC, Library.MATH);
        evald.addUserFunction(new Rint());
        double offset = -287.72079288140884;
        double swdep = 486.2106094079397;
        evald.addVariable("offset", offset);
        evald.addVariable("swdep", swdep);
        evald.parse("rint(offset)+4");
        System.err.println(evald.toTree());
        System.err.println(evald.evaluate());
        evald.parse("rint(offset)+4");
        System.err.println(evald.toTree());
        System.err.println(evald.evaluate());
        evald.parse("rint((1000.0/2500)*offset)");
        System.err.println(evald.evaluate());
        evald.parse("rint((1000.0/2500)*offset)");
        System.err.println(evald.evaluate());
        evald.parse("rint((1000.0/2500)*offset)+4");
        System.err.println(evald.evaluate());
        evald.parse("rint((1000.0/2500)*offset)+4");
        System.err.println(evald.evaluate());
        evald.parse("rint((1000.0/2500)*offset)+4+15");
        System.err.println(evald.evaluate());
        evald.parse("rint((1000.0/2500)*offset)+4+15");
        System.err.println(evald.evaluate());
        evald.parse("rint((1000.0/2500)*offset)+rint(1400.0*(swdep/100.0)/1535.0)+15");
        System.err.println(evald.evaluate());
        assertEquals(Math.rint((1000.0 / 2500) * offset) + Math.rint(1400.0 * (swdep / 100.0) / 1535.0) + 15, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testExtra() {
        Evald evald = new Evald(Library.CORE, Library.LOGIC, Library.MATH);
        double v1 = Double.POSITIVE_INFINITY;
        evald.addVariable("v1", v1);
        evald.addVariable("v2", 1);
        double twt_ms = 461.5381100190259;
        double h1 = -198.20700211614127;
        double h2 = 1.7976931348623157E308;
        evald.addVariable("h1", h1);
        evald.addVariable("twt_ms", twt_ms);
        evald.addVariable("h2", h2);
        evald.parse("v1 + v1 * 0.75 * exp(-(twt_ms-((h1+h2)/2))^2/(2*75^2))");
        System.err.println(" = " + evald.evaluate());
        System.err.println("(-(twt_ms-((h1+h2)/2))^2/(2*75^2)) = " + (v1 + v1 * 0.75 * Math.exp(-Math.pow((twt_ms - ((h1 + h2) / 2)), 2) / (Math.pow(2 * 75, 2)))));
    }
    
    @Test public void testSpaceAfterFunctionName() {
        Evald evald = new Evald(Library.CORE, Library.LOGIC, Library.MATH);
        evald.parse("if ( isnan ( v1 ) , v2 , nan )");
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
            @Override public double get(double value) {
                return value + 2;
            }
        });
        evald.parse("x * foo(y) + 4");
        evald.addVariable("x", 0.1);
        evald.addVariable("y", 0.7);
        assertEquals(0.1 * (0.7 + 2) + 4, evald.evaluate(), DEFAULT_PRECISION);
        //fail if user functions are persisting state.
        assertEquals(0.1 * (0.7 + 2) + 4, evald.evaluate(), DEFAULT_PRECISION);
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
            @Override public double get(double value) {
                return value / 3;
            }
        });
        evald.addUserFunction(new OneArgFunction("Foo") {
            @Override public double get(double value) {
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
            @Override public double get(double... values) {
                double sum = 0.0;
                for (double value : values) {
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

    @Test public void testImpureNArgFunction() {
        Evald evald = new Evald();
        evald.addUserFunction(new ImpureNArgFunction("randfn", 0, 0) {
            @Override public double get(double... values) {
                return Math.random();
            }
        });
        evald.parse("2 * randfn()");
        assertNotEquals(evald.evaluate(), evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testTwoArgFunction() {
        Evald evald = new Evald();
        evald.addUserFunction(new TwoArgFunction("bar") {
            @Override public double get(double arg1, double arg2) {
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
        evald.setImplicitMultiplication(false);
        try {
            evald.parse("x * pow(y, z)");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(UnknownMethodEvaldException.class)); //Expected a method "pow"
        }
        evald.setImplicitMultiplication(true);
        try {
            evald.parse("x * pow(y, z)");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(EvaldException.class)); //Expected a variable "pow"
        }
    }

    @Test public void testListUndeclared() {
        Evald evald = new Evald();
        assertEquals(true, evald.getAllowUndeclared());
        evald.setAllowUndeclared(true);
        assertEquals(true, evald.getAllowUndeclared());
        evald.addVariable("abc");
        evald.addVariable("q");
        evald.parse("abc * def + g / h");
        Set<String> vars = new HashSet<String>(Arrays.asList(evald.listUndeclared()));
        assertEquals(3, vars.size());
        assertFalse(vars.contains("abc"));
        assertFalse(vars.contains("q"));
        assertTrue(vars.contains("def"));
        assertTrue(vars.contains("g"));
        assertTrue(vars.contains("h"));
    }

    @Test public void addVariableAfterParsing() {
        Evald evald = new Evald();
        int xi = evald.addVariable("xxxx");
        evald.parse("xxxx * yyyy");
        int yi = evald.addVariable("yyyy");
        int zi = evald.addVariable("zzzz");
        evald.setVariable(xi, 0.5);
        evald.setVariable(yi, 2.0);
        evald.setVariable(zi, 5.0);
        double result = evald.evaluate();
        assertEquals(2.0 * 0.5, result, DEFAULT_PRECISION);
    }

    @Test public void testParseInvalid() {
        Evald evald = new Evald();
        try {
            evald.parse("c *");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(EvaldException.class));
            assertEquals("Expected a value after *", t.getMessage());
        }

        evald = new Evald();
        try {
            evald.parse("* b");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(EvaldException.class));
            assertEquals("Expected a value or expression at * b", t.getMessage());
        }
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
        evald.parse(".75a");
        assertEquals(2 * 0.75, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a (b+1)");
        assertEquals(2 * (3 + 1), evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a(b+1)");
        assertEquals(2 * (3 + 1), evald.evaluate(), DEFAULT_PRECISION);

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

    private void testCollapse(double inv) {
        Evald evald = new Evald(Library.CORE, Library.LOGIC);
        evald.addVariable("a", inv);
        evald.parse("a + 0");
        assertEquals(inv + 0, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a * a");
        assertEquals(inv * inv, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("0 % a");
        assertEquals(0 % inv, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("0 / a");
        assertEquals(0 / inv, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a / 1");
        assertEquals(inv / 1, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a * 1");
        assertEquals(inv * 1, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a ^ 0");
        assertEquals(Math.pow(inv, 0), evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a ^ 1");
        assertEquals(Math.pow(inv, 1), evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("a * 0");
        assertEquals(inv * 0, evald.evaluate(), DEFAULT_PRECISION);

    }

    @Test public void testCollapse() {
        testCollapse(Double.POSITIVE_INFINITY);
        testCollapse(Double.NaN);
        testCollapse(Double.MAX_VALUE);
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
            @Override protected double get(double value) {
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
                @Override protected double get(double value) {
                    return value + 1;
                }
            });

            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(InvalidTokenEvaldException.class));
        }
        try {
            evald.addUserFunction(new OneArgFunction("#value1") {
                @Override protected double get(double value) {
                    return value + 1;
                }
            });

            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(InvalidTokenEvaldException.class));
        }
        //legal
        evald.addUserFunction(new OneArgFunction("_Value_1") {
            @Override protected double get(double value) {
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
        evald.setAllowUndeclared(false);
        try {
            evald.parse("2*sin(a)");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(UndeclaredVariableEvaldException.class));
            assertTrue(t.getMessage().contains("sin"));
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

    @Test public void testAssignment() {
        Evald evald = new Evald();
        evald.addVariable("a", 2);
        evald.addVariable("b", 5);
        try {
            evald.parse("a == b");
        } catch (Throwable t) {
            assertThat(t, instanceOf(EvaldException.class));
        }
        evald.addLibrary(Library.LOGIC);
        evald.parse("a == b");
        assertEquals(0, evald.evaluate(), DEFAULT_PRECISION);
        try {
            //Assignment is not yet supported.
            evald.parse("a = b");
        } catch (Throwable t) {
            assertThat(t, instanceOf(EvaldException.class));
        }
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
        checkMath(evald, "log(a*c+b)", Math.log(a * c + b));
        checkMath(evald, "log10(a+b*c)", Math.log10(a + b * c));
        checkMath(evald, "max(b,c)", Math.max(b, c));
        checkMath(evald, "min(b-c, c-b)", Math.min(b - c, c - b));
        //random's a bit different...
        evald.parse("2*random()");
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

    @Test public void testExtraMath() {
        Evald evald = new Evald(Library.MATH, Library.LOGIC, Library.CORE);
        double a = 11.0;
        double b = -0.5;
        double c = 1.0;
        evald.addVariable("a", a);
        evald.addVariable("b", b);
        evald.addVariable("c", c);
        checkMath(evald, "atanh(-0.9)", 0.5 * Math.log(0.1 / 1.9));
        checkMath(evald, "acosh(2.5)", Math.log(2.5 + Math.pow(Math.pow(2.5, 2) - 1, 0.5)));
        checkMath(evald, "asinh(-3.2)", Math.log(-3.2 + Math.pow(Math.pow(-3.2, 2) + 1, 0.5)));
        checkMath(evald, "cot(2.0)", 1 / Math.tan(2.0));
        checkMath(evald, "cosec(2.0)", 1 / Math.sin(2.0));
        checkMath(evald, "sec(2.0)", 1 / Math.cos(2.0));
    }

    @Test public void testAddVariableTypes() {
        Evald evald = new Evald();
        evald.addVariable("a", 1);
        evald.addVariable("b", 2.0f);
        evald.addVariable("c", 3.0);
        evald.parse("a+b+c");
        assertEquals(1 * 2 * 3, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testListAllVariables() {
        Evald evald = new Evald();
        evald.addVariable("a", 1);
        evald.addVariable("b", 2);
        evald.parse("a + b + c");
        evald.addVariable("d", 3);
        Set<String> vars = new HashSet<String>(Arrays.asList(evald.listAllVariables()));
        assertTrue(vars.contains("a"));
        assertTrue(vars.contains("b"));
        assertTrue(vars.contains("c"));
        assertTrue(vars.contains("d"));
    }

    @Test public void testListAllFunctions() {
        Evald evald = new Evald(Library.MATH, Library.LOGIC, Library.CORE);
        evald.parse("a % b");
        assertArrayEquals(evald.listActiveFunctions());
        evald.parse("a + sin(b)");
        assertArrayEquals(evald.listActiveFunctions(), "sin");
        evald.parse("cos(a) + sin(isnan(b))");
        assertArrayEquals(evald.listActiveFunctions(), "sin", "cos", "isnan");
    }

    /** list must contain exactly the listed strings, in any order */
    private void assertArrayEquals(String[] actualArray, String... expectedArray) {
        Set<String> actualSet = new HashSet(Arrays.asList(actualArray));
        Set<String> expectedSet = new HashSet(Arrays.asList(expectedArray));
        assertTrue("Expected {" + Arrays.toString(expectedArray) + "} but got {" + Arrays.toString(actualArray) + "}",
                   actualSet.containsAll(expectedSet) && expectedSet.containsAll(actualSet));
    }

    @Test public void testListActiveVariables() {
        Evald evald = new Evald();
        assertEquals(0, evald.listActiveVariables().length);
        evald.addVariable("a", 1);
        evald.addVariable("b", 2);
        evald.parse("a + b + c");
        evald.addVariable("d", 3);
        Set<String> vars = new HashSet<String>(Arrays.asList(evald.listActiveVariables()));
        assertTrue(vars.contains("a"));
        assertTrue(vars.contains("b"));
        assertTrue(vars.contains("c"));
        assertFalse(vars.contains("d"));
    }

    @Test public void testSubtraction() {
        Evald evald = new Evald();
        evald.addVariable("a", 2);
        evald.parse("-a");
        assertEquals(-2, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("1-a");
        assertEquals(1 - 2, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("-1-a");
        assertEquals(-1 - 2, evald.evaluate(), DEFAULT_PRECISION);
        evald.parse("0-a");
        assertEquals(0 - 2, evald.evaluate(), DEFAULT_PRECISION);
    }
    
    @Test public void testNullVariable() {
    	Evald evald = new Evald();
    	int ia = evald.addVariable("a");
    	int ib = evald.addVariable("b");
        evald.setVariable(ia, (int) 1);
        evald.parse("a*2");
        assertEquals(2, evald.evaluate(), DEFAULT_PRECISION);
        evald.setVariable(ia, (int) 3);
    	evald.setVariable(ib, 2.0f);
    	evald.parse("a*b");
        assertEquals(6, evald.evaluate(), DEFAULT_PRECISION);
    }

    @Test public void testRemoveConstant() {
        Evald evald = new Evald();
        evald.addLibrary(Library.MATH);
        evald.parse("nan");
        assertTrue(Double.isNaN(evald.evaluate()));
        evald.addVariable("nan", 1);
        evald.parse("nan");
        assertNotEquals(1, evald.evaluate(), DEFAULT_PRECISION);
        evald.removeConstant(null); //Okay, but meaningless
        evald.removeConstant(""); //Okay, but meaningless
        evald.removeConstant("irrelevant");
        evald.removeConstant("nan");
        evald.parse("nan");
        assertEquals(1, evald.evaluate(), DEFAULT_PRECISION);
    }

    private void assertContainsAll(String[] expect, String[] actual) {
        assertNotNull(expect);
        assertNotNull(actual);
        assertTrue("Expected arrays to have the same length, but\n" + Arrays.toString(actual) + "\n doesn't match\n" + Arrays.toString(expect), expect.length == actual.length);
        Set<String> expectSet = new HashSet<String>(Arrays.asList(expect));
        expectSet.removeAll(Arrays.asList(actual));
        assertTrue("The actual output didn't contain " + expectSet, expectSet.isEmpty());
    }

    @Test public void testUninitialised() {
        thrown.expect(UninitialisedEvaldException.class);
        Evald evald = new Evald();
        evald.evaluate();
    }

    @Test public void testMultipleExpression() {
        Evald evald = new Evald();
        evald.parse("c = a ^ b;\nd = a * c");
        assertContainsAll(new String[] { "a", "b", "c", "d" }, evald.listAllVariables());
        assertContainsAll(new String[] { "a", "b" }, evald.listAllInputs());
        assertContainsAll(new String[] { "c", "d" }, evald.listAllOutputOrIntermediateVariables());
        int cIndex = evald.getVariableIndex("c");
        double a = 7;
        double b = 3;
        evald.addVariable("a", a);
        evald.addVariable("b", b);
        double defaultEvaluateResult = evald.evaluate();
        double dValue = evald.getVariableValue("d");
        double cValue = evald.getVariableValue(cIndex);
        assertEquals(cValue, Math.pow(a, b), DEFAULT_PRECISION);
        assertEquals(dValue, Math.pow(a, b) * a, DEFAULT_PRECISION);
        assertEquals(dValue, defaultEvaluateResult, DEFAULT_PRECISION);        
    }

    @Test public void testSingleExpressionDefaultToken() {
        Evald evald = new Evald();
        evald.parse("a * 2");
        evald.addVariable("a", 2);
        double result = evald.evaluate();
        assertEquals(4, (int) result);
        assertEquals(4, (int) evald.getVariableValue(evald.getDefaultResultToken()));
        assertEquals("result", evald.getDefaultResultToken());
        evald.setDefaultResultToken("d");
        assertEquals("d", evald.getDefaultResultToken());
        //Won't be usable immediately - expression must be re-parsed if we changed the default token.
        try {
            evald.getVariableValue("d");
            fail("Expected this to fail.");
        } catch (UndeclaredVariableEvaldException e) {
            //Move on...
        }
        evald.parse("a * 2");
        evald.evaluate();
        assertEquals(4, (int) evald.getVariableValue("d"));
    }

    @Test public void testMultiExpressionExceptions() {
        Evald evald = new Evald();
        try {
            evald.parse("a * 2; b=a");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(EvaldException.class));
            assertTrue(t.getMessage().contains("Expected an assignment expression"));
        }
        evald.parse("b = a * 2;;c = b*b");
        try {
            evald.enableOutputs("q");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(UndeclaredVariableEvaldException.class));
        }
        evald.enableOutputs("b");
        evald.addVariable("a", 3);
        assertEquals(6, evald.evaluate(), DEFAULT_PRECISION);
        assertEquals(6, evald.getVariableValue("b"), DEFAULT_PRECISION);
    }

    @Test public void testAddManyVariables() {
        Evald evald = new Evald();
        evald.parse("aa+bb");
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                String token = String.format("%c%c", 'a' + i, 'a' + j);
                evald.addVariable(token, i * 10 + j);
                System.err.println("Added " + token + " = " + (i * 10 + j));
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertEquals(i * 10 + j, evald.getVariableValue(String.format("%c%c", 'a' + i, 'a' + j)), DEFAULT_PRECISION);
            }
        }
    }

    @Test public void testMultiExpressionExceptionAmbiguity() {
        Evald evald = new Evald();
        try {
            evald.parse("x = a + b;\ny = a * b;\nlen = sqrt(a^2+b^2);\nq = abc()");
        } catch (EvaldException e) {
            assertTrue(e.getMessage().contains("Syntax error in subexpression"));
            assertTrue(e.getMessage().contains("abc"));
        }
    }
}
