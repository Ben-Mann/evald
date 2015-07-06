package net.benmann.evald;

class ExpressionString {
    String expression;

    ExpressionString(String expression) {
        this.expression = expression.trim();
    }

    boolean update(int charactersConsumed) {
        if (charactersConsumed == expression.length()) {
            expression = "";
        } else {
            expression = expression.substring(charactersConsumed);
        }
        return charactersConsumed != 0;
    }
}