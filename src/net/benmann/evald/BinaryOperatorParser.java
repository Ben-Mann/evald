package net.benmann.evald;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Operator parser
abstract class BinaryOperatorParser extends OperatorParser {
    final Pattern tokenPattern;

    BinaryOperatorParser(String token) {
        super(token);
        this.tokenPattern = Pattern.compile("^(\\s*" + Pattern.quote(token) + "\\s*).*");
    }
    
    abstract protected BinaryOperatorNode create();

    BinaryOperatorNode parse(ExpressionString str) {
    	Matcher matches = tokenPattern.matcher(str.expression);
    	
    	if (!matches.find())
    		return null;
    	
    	String tokenFound = matches.group(1);
    	
    	int skip = tokenFound.length();
        
        //Remove token from expression
        str.update(skip);

        return create();
    }
}