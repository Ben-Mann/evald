package net.benmann.evald;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Library {
    public static final Library MATH = new LibMath();
    public static final Library LOGIC = new LibConditional();
    public static final Library BINARY = new LibBinary();
    public static final Library CORE = new LibArithmetic();

    public static final Library ALL = new Library() {
        @Override Parser[] getParsers() {
            List<Parser> parserList = new ArrayList<Parser>();
            parserList.addAll(Arrays.asList(CORE.getParsers()));
            parserList.addAll(Arrays.asList(BINARY.getParsers()));
            parserList.addAll(Arrays.asList(LOGIC.getParsers()));
            parserList.addAll(Arrays.asList(MATH.getParsers()));
            return parserList.toArray(new Parser[] {});
        }
    };

    abstract Parser[] getParsers();
}