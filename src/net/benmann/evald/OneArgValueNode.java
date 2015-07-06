package net.benmann.evald;

abstract class OneArgValueNode extends ValueNode {
    Node arg1;
    final String token;

    OneArgValueNode(String token, Node arg1) {
        super(false);
		this.arg1 = arg1;
        this.token = token;
	}

    @Override Node collapse() {
        arg1 = arg1.collapse();
        if (!arg1.isConstant)
            return this;

        return new Constant(get());
    }
    
    @Override String toTree(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(token).append("\n");
        sb.append(arg1.toTree(prefix + "  "));
        return sb.toString();
    }
}