package net.benmann.evald;

class Constant extends ValueNode {
    final double value;

    Constant(double value) {
        super(true);
        this.value = value;
    }

    @Override protected double get() {
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