package net.benmann.evald;

import java.util.regex.Matcher;

abstract class PostfixOperatorParser extends UnaryOperatorParser {
    PostfixOperatorParser(String token) {
        super(token);
	}

    abstract PostfixOperatorNode create();

    PostfixOperatorNode parse(ExpressionString str) {
    	Matcher matches = tokenPattern.matcher(str.expression);
    	
    	if (!matches.find())
    		return null;
    	
    	String tokenFound = matches.group(1);
    	
    	int skip = tokenFound.length();
    	
    	str.update(skip);
    	
        return create();
	}
}