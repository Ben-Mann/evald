package net.benmann.evald;

abstract class ZeroArgValueNode extends ValueNode {
    final String token;

    ZeroArgValueNode(String token, boolean isConstant) {
        super(isConstant);
        this.token = token;
    }

    @Override String toTree(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(token).append("\n");
        return sb.toString();
    }

    @Override protected Node collapse() {
        return this;
    }
}