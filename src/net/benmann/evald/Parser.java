package net.benmann.evald;


public abstract class Parser {
    public static final int NO_MAX = -1;
    final String token;

    Parser(String token) {
        this.token = token;
    }
}