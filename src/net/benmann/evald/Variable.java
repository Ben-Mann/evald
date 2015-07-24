package net.benmann.evald;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Variable extends ValueNode {
    final int index;
    double[] values;

    //how about: when we do parse(), we allocate the evald valuelist, because we know which variables
    //are used in THIS expression, and can pre-allocate them. Then, after the allocation
    Variable(Evald evald, int index) {
        super(false);
        evald.addValueArrayCallback(new SetValueArrayCallback() {
            @Override void setValueArray(double[] valueArray) {
                values = valueArray;
            }
        });
        evald.addUsedIndex(index);
        this.index = index;
    }

    @Override protected double get() {
        return values[index];
    }

    @Override protected Node collapse() {
        return this;
    }

    @Override String toTree(String prefix) {
        StringBuilder sb = new StringBuilder();
        String value = "undefined";
        if (values != null && values.length > index) {
            value = Double.toString(get());
        }
        sb.append(prefix).append("Variable[").append(index).append("] (").append(value).append(")\n");
        return sb.toString();
    }

    static Pattern pattern = Pattern.compile("[a-zA-Z_][a-z_A-Z0-9]*");
    static ValueParser parser = new ValueParser(null) {
        @Override ValueNode parse(ExpressionParser operationParser, ExpressionString str) {
            //Find a value
            Matcher matcher = Variable.pattern.matcher(str.expression);
            if (!matcher.find())
                return null;

            String content = matcher.group();

            Integer index = operationParser.evald.keyIndexMap.get(content);
            if (index == null) {
                operationParser.evald.undeclaredKeyMap.add(content);
                index = operationParser.evald.addVariable(content, 0.0);
            }

            str.update(content.length());
            return new Variable(operationParser.evald, index);
        }
    };
}