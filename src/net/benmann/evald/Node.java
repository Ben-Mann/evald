package net.benmann.evald;

abstract class Node {
	OperatorNode parent;
    final boolean isConstant;

    /**
     * Get the current value for this tree node.
     * 
     * @return the result of evaluating this tree node.
     */
    protected abstract Double get();

    Node(boolean isConstant) {
        this.isConstant = isConstant;
    }

    /**
     * Used to optimise constant operations out of a query.
     * 
     * If all inputs are constant, the output is likely to be constant (depending on the function), and the get() processing can be eliminated replaced instead by a constant value.
     * 
     * @return a constant representation of this node, if one is available, or else this node.
     */
    abstract Node collapse();

    abstract String toTree(String prefix);
}