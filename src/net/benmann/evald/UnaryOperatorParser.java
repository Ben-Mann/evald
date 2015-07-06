package net.benmann.evald;

import java.util.regex.Pattern;

abstract class UnaryOperatorParser extends OperatorParser {
    final Pattern tokenPattern;
    
    UnaryOperatorParser(String token) {
        super(token);
        this.tokenPattern = Pattern.compile("^(\\s*" + Pattern.quote(token) + "\\s*).*");
	}
}