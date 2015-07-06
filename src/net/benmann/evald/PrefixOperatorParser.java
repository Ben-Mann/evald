package net.benmann.evald;

import java.util.regex.Matcher;

abstract class PrefixOperatorParser extends UnaryOperatorParser {
    PrefixOperatorParser(String token) {
        super(token);
	}

    abstract PrefixOperatorNode create();

	PrefixOperatorNode parse(ExpressionString str) {
    	Matcher matches = tokenPattern.matcher(str.expression);
    	
    	if (!matches.find())
    		return null;
    	
    	String tokenFound = matches.group(1);
    	
    	int skip = tokenFound.length();
    	
    	str.update(skip);

        return create();
	}
}