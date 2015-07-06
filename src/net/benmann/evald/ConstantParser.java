package net.benmann.evald;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class ConstantParser extends ValueParser {
    final Pattern pattern;

    ConstantParser(String token) {
        super(token);
        this.pattern = Pattern.compile("^(\\s*" + Pattern.quote(token) + ")[^A-Za-z0-9_]");
    }

    abstract Constant create();

    @Override Node parse(ExpressionParser op, ExpressionString str) {
        Matcher matcher = pattern.matcher(str.expression + " "); //FIXME this is a hack for search order
        if (!matcher.find())
            return null;
        
        String tokenFound = matcher.group(1);
        int skip = tokenFound.length();
        str.update(skip);

        return create();
    }
}