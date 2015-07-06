package net.benmann.evald;

abstract class ValueNode extends Node {

    ValueNode(boolean isConstant) {
        super(isConstant);
    }
}