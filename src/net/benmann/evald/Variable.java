package net.benmann.evald;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Variable extends ValueNode {
    final int index;
    final List<Double> values;	//FIXME for slightly better performance we could use a double[] array directly.

    Variable(Evald evald, int index) {
        super(false);
        values = evald.valueList;
        this.index = index;
    }

    @Override protected Double get() {
        return values.get(index);
    }

    @Override protected Node collapse() {
        return this;
    }

    @Override String toTree(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("Variable[").append(index).append("]\n");
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