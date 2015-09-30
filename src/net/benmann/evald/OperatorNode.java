package net.benmann.evald;

abstract class OperatorNode extends Node {
    Node b;
    final String token;

    abstract int getPrecedence();

    OperatorNode(String token, boolean isConstant) {
        super(isConstant);
        this.token = token;
    }

    @Override Node collapse() {
        b = b.collapse();

        if (!b.isConstant)
            return this;

        return new Constant(get());
    }
}
