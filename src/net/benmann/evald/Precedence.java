package net.benmann.evald;

enum Precedence {
    ASSIGNMENT,
    CONDITIONAL,
    CONDITIONAL_OR,
    CONDITIONAL_AND,
    BITWISE_OR,
    BITWISE_XOR,
    BITWISE_AND,
    EQUALITY,
    RELATIONAL,
    SHIFT,
    ADDITIVE,
    MULTIPLICATIVE,
    POWER,
	PREFIX,
	POSTFIX;
}
