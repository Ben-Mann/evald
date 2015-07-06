package net.benmann.evald;


abstract class Parser {
    final String token;

    Parser(String token) {
        this.token = token;
    }
}