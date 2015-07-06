package net.benmann.evald;

abstract class UnaryOperatorNode extends OperatorNode {
    UnaryOperatorNode(boolean isConstant) {
        super(isConstant);
    }
}