package net.benmann.evald;

abstract class PostfixOperatorNode extends UnaryOperatorNode {
    final String token;

    PostfixOperatorNode(String token) {
        super(false);
        this.token = token;
    }

	@Override int getPrecedence() {
        return Precedence.POSTFIX.ordinal();
	}

    @Override String toTree(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("Postfix ").append(token).append("\n");
        sb.append(b.toTree(prefix + "  "));
        return sb.toString();
    }

}