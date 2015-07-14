package net.benmann.evald;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class NArgParser extends ValueParser {
	final Pattern pattern;
	final int minArguments;
	final int maxArguments;
	final CreateNArgFunctionFn createFn;
	
    NArgParser(int numArguments, CreateNArgFunctionFn createFn) {
        this(numArguments, numArguments, createFn);
    }

    NArgParser(int minArguments, int maxArguments, CreateNArgFunctionFn createFn) {
        super(createFn.token);
        assert (maxArguments == NO_MAX || minArguments < maxArguments);
		assert(maxArguments > 0);
        pattern = Pattern.compile("^(\\s*" + Pattern.quote(token) + "\\()");
		this.minArguments = minArguments;
		this.maxArguments = maxArguments;
		this.createFn = createFn;
	}

	@Override
    Node parse(ExpressionParser operationParser, ExpressionString str) {
		Matcher matcher = pattern.matcher(str.expression);
		if (!matcher.find())
            return null;
		
		String tokenFound = matcher.group(1);
		int skip = tokenFound.length();
		str.update(skip);
		
		//find matching ) and count , at the base level.
		int level = 0;
		int closingBraceIndex = 0;
		List<Node> args = new ArrayList<Node>();
		//Read until we get to a ) or ,, then consume that value.
		while (closingBraceIndex < str.expression.length()) {
			char bch = str.expression.charAt(closingBraceIndex);
			if (bch == '(') {
				level++;
			} else if (bch == ')') {
				if (level == 0) {
                    //FIXME these repeated calls should be consolidated.
                    if (args.size() != 0 || closingBraceIndex != 0) {
                        args.add(operationParser.parse(str.expression.substring(0, closingBraceIndex)));
                    }
					str.update(closingBraceIndex+1); //consume the )
				}
				level--;
			} else if (bch == ',' && level == 0) {
                args.add(operationParser.parse(str.expression.substring(0, closingBraceIndex)));
				str.update(closingBraceIndex+1); //consume the )
				closingBraceIndex = 0;
				continue;
			}
			
			if (level == -1)
				break;
			
			closingBraceIndex++;
		}
		
		if (args.size() < minArguments) {
			throw new EvaldException("Insufficient arguments for function " + token);
		}
		
        if (maxArguments > 0 && args.size() > maxArguments) {
			throw new EvaldException("Too many arguments for function " + token);
		}
		
		if (level != -1) {
			throw new EvaldException("Missing closing brace for function " + token); //FIXME for function where?
		}

		while (args.size() < maxArguments) {
			//pad with null
			args.add(null);
		}
		
        return createFn.fn(args);
	}
}