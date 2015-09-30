package net.benmann.evald;

abstract class PrefixOperatorNode extends UnaryOperatorNode {
    PrefixOperatorNode(String token) {
        super(token, false);
    }

    @Override int getPrecedence() {
        return Precedence.PREFIX.ordinal();
	}

    @Override String toTree(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("Prefix ").append(token).append("\n");
        sb.append(b.toTree(prefix + "  "));
        return sb.toString();
    }

}
