package net.benmann.evald;

class Constant extends ValueNode {
    final Double value;

    Constant(Double value) {
        super(true);
        this.value = value;
    }

    @Override protected Double get() {
        return value;
    }

    @Override protected Node collapse() {
        return this;
    }

    @Override String toTree(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("Constant = ").append(value).append("\n");
        return sb.toString();
    }
}