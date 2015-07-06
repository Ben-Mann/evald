package net.benmann.evald;

abstract class BinaryOperatorNode extends OperatorNode {
    Node a;
    final Precedence precedence;
    final String token;

    BinaryOperatorNode(String token, Precedence precedence) {
        super(false);
        this.precedence = precedence;
        this.token = token;
    }

    @Override int getPrecedence() {
        return precedence.ordinal();
    }

    @Override Node collapse() {
        a = a.collapse();
        b = b.collapse();

        if (a.isConstant && b.isConstant)
            return new Constant(get());

        return this;
    }

    @Override String toTree(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("Binary ").append(token).append("\n");
        sb.append(a.toTree(prefix + "  "));
        sb.append(b.toTree(prefix + "  "));
        return sb.toString();
    }

}