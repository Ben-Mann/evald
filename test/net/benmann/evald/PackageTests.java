package net.benmann.evald;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import net.benmann.evald.AbstractEvaldException.EvaldException;
import net.benmann.evald.ArgFunction.OneArgFunction;

public class PackageTests {
    static final double DEFAULT_PRECISION = 0.00001;


    static double getFactorial(double v) {
        if (v < 1)
            return Double.NaN;
        if (v == 1)
            return 1.0;

        double result = 1;
        for (int i = 2; i <= v; i++) {
            result *= i;
        }
        return result;
    }

    private void testOptimisation(String unoptimised, String optimised, int expectedSteps) {
        Evald eOpt = new Evald(Library.ALL);
        eOpt.parse(optimised);
        String treeOpt = eOpt.toTree();
        Evald eUnopt = new Evald(Library.ALL);
        eUnopt.parse(unoptimised);
        String treeUnopt = eUnopt.toTree();
        assertEquals(treeOpt, treeUnopt);
        assertEquals(expectedSteps, treeOpt.split("[\n]").length);
    }

    @Test public void testCollapse() {
        testOptimisation("v * 1", "v", 1);
        testOptimisation("v * 0", "v * 0", 3); //0*inf = nan, so we don't optimise this. 
        testOptimisation("1 * v", "v", 1);
        testOptimisation("0 * v", "0 * v", 3);
        testOptimisation("v * 2", "v * 2", 3);
        testOptimisation("v * 1 + 1", "v + 1", 3);

        testOptimisation("0 / v", "0 / v", 3);
        testOptimisation("v / 1", "v", 1);
        testOptimisation("v / 0", "v / 0", 3);

        testOptimisation("v + 0", "v", 1);
        testOptimisation("v + 1", "v + 1", 3);

        testOptimisation("v - 0", "v", 1);
        testOptimisation("v - 1", "v - 1", 3);

        testOptimisation("v * (2 - 1) + (1 + 0) + (b + 0)", "v + 1 + b", 5);

        testOptimisation("+v", "v", 1);
        testOptimisation("2 * +v", "2 * v", 3);

        testOptimisation("v ^ 2", "v ^ 2", 3);
        testOptimisation("v ^ 1", "v", 1);
        testOptimisation("v ^ 0", "1", 1);

        testOptimisation("2 % v", "2 % v", 3);
        testOptimisation("0 % v", "0 % v", 3);

        testOptimisation("v * (sin(toRadians(90))^2) + 1", "v + 1", 3);
    }

    @Test public void testAddPostfixOperator() {
        PostfixOperatorParser parser = new PostfixOperatorParser("!") {
            @Override public PostfixOperatorNode create() {
                return new PostfixOperatorNode(token) {
                    @Override protected double get() {
                        return getFactorial(b.get());
                    }
                };
            }
        };

        Evald evald = new Evald();
        evald.addParser(parser);
        evald.parse("a! * b + c!");
        evald.addVariable("a", 2.0);
        evald.addVariable("b", 3.0);
        evald.addVariable("c", 7.0);
        assertEquals(getFactorial(2) * 3 + getFactorial(7.0), evald.evaluate(), 0.001);

        assertEquals(true, evald.getAllowMultiplePostfixOperators());
        evald.setAllowMultiplePostfixOperators(false);
        assertEquals(false, evald.getAllowMultiplePostfixOperators());
        evald.parse("(b!)!");
        assertEquals(getFactorial(getFactorial(3)), evald.evaluate(), 0.001);
        try {
            evald.parse("b!!");
            fail();
        } catch (Throwable t) {
            assertThat(t, instanceOf(EvaldException.class));
        }
        evald.setAllowMultiplePostfixOperators(true);
        assertEquals(true, evald.getAllowMultiplePostfixOperators());
        evald.parse("b!!");
        assertEquals(getFactorial(getFactorial(3)), evald.evaluate(), 0.001);
    }

    @Test public void testParserList() {
        Evald evald = new Evald();
        evald.addUserFunction(new OneArgFunction("foobar") {
            @Override protected double get(double value) {
                return value * 2;
            }
        });
        evald.addParser(new BinaryOperatorParser("ping") {
            @Override public BinaryOperatorNode create() {
                return new BinaryOperatorNode(token, Precedence.ADDITIVE) {
                    @Override protected double get() {
                        return 100 * (a.get() + b.get());
                    }
                };
            }
        });
        evald.addParser(new BinaryOperatorParser("pingpong") {
            @Override public BinaryOperatorNode create() {
                return new BinaryOperatorNode(token, Precedence.MULTIPLICATIVE) {
                    @Override protected double get() {
                        return -(a.get() + b.get());
                    }
                };
            }
        });

        evald.parse("foo * foobar(3) * 5 ping 6 pingpong 7");
        evald.addVariable("foo", 100.0);
        assertEquals(100.0 * ((100 * 2 * 3 * 5) - (6 + 7)), evald.evaluate(), DEFAULT_PRECISION);
    }
    
    
    @Test public void testMultipleExpressionDependencies() {
        String expression = 
                  "x = (a + b + a * b) / (a + 2 * b) / b;\n"
                + "y = (a + 2 * b) / (a + b + a * b) / a;"
                + "out1 = 2 * x * y;"
                + "out2 = 2 / x / y;";
        Evald evald = new Evald();
        evald.parse(expression);
        assertEquals("xyout1out2", evald.executionSequence());
        evald.enableAllOutputs();
        assertEquals("xyout1out2", evald.executionSequence());
        evald.enableOutputs("x");
        assertEquals("x", evald.executionSequence());
        evald.enableOutputs("y");
        assertEquals("y", evald.executionSequence());
        evald.enableOutputs("out1");
        assertEquals("xyout1", evald.executionSequence());
        evald.enableOutputs("out2");
        assertEquals("xyout2", evald.executionSequence());
        evald.enableAllOutputs();
        assertEquals("xyout1out2", evald.executionSequence());
    }
}
