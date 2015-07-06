package net.benmann.evald;

abstract class ValueParser extends Parser {
    ValueParser(String token) {
        super(token);
    }

    abstract Node parse(ExpressionParser operationParser, ExpressionString str);
}