package net.benmann.evald;

abstract class OperatorNode extends Node {
    Node b;

    abstract int getPrecedence();

    OperatorNode(boolean isConstant) {
        super(isConstant);
    }

    @Override Node collapse() {
        b = b.collapse();

        if (!b.isConstant)
            return this;

        return new Constant(get());
    }
}
