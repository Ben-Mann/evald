package net.benmann.evald;

abstract class TwoArgValueNode extends ValueNode {
    Node arg1, arg2;
    final String token;

    TwoArgValueNode(String token, Node arg1, Node arg2) {
        super(false);
		this.arg1 = arg1;
		this.arg2 = arg2;
        this.token = token;
	}

    @Override Node collapse() {
        arg1 = arg1.collapse();
        arg2 = arg2.collapse();
        if (!arg1.isConstant || !arg2.isConstant)
            return this;

        return new Constant(get());
    }

    @Override String toTree(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(token).append("\n");
        sb.append(arg1.toTree(prefix + "  "));
        sb.append(arg2.toTree(prefix + "  "));
        return sb.toString();
    }
}