package net.benmann.evald;

abstract class UnaryOperatorNode extends OperatorNode {
    UnaryOperatorNode(String token, boolean isConstant) {
        super(token, isConstant);
    }
}