package net.benmann.evald;

abstract class ThreeArgValueNode extends ValueNode {
    Node arg1, arg2, arg3;
    final String token;

    ThreeArgValueNode(String token, Node arg1, Node arg2, Node arg3) {
        super(false);
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.token = token;
    }

    @Override Node collapse() {
        arg1 = arg1.collapse();
        arg2 = arg2.collapse();
        arg3 = arg3.collapse();
        if (!arg1.isConstant || !arg2.isConstant || !arg3.isConstant)
            return this;

        return new Constant(get());
    }

    @Override String toTree(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(token).append("\n");
        sb.append(arg1.toTree(prefix + "  "));
        sb.append(arg2.toTree(prefix + "  "));
        sb.append(arg3.toTree(prefix + "  "));
        return sb.toString();
    }
}