package net.benmann.evald;

import java.util.Collection;

@SuppressWarnings("serial")
 public class EvaldException extends RuntimeException {
    EvaldException() {
		super();
	}
	
    EvaldException(String arg) {
		super(arg);
	}
	
    /**
     * Thrown when an undeclared variable was found in an expression, and undeclared variables
     * were not allowed (with Expression.setAllowUndeclared).
     */
    static public class UndeclaredVariableEvaldException extends EvaldException {
		static private String toList(Collection<String> strings) {
			StringBuilder sb = new StringBuilder();
			for(String string : strings) {
				sb.append("\n").append(string);
			}
			return sb.toString();
		}
		UndeclaredVariableEvaldException(Collection<String> undeclared) {
			super("There are " + undeclared.size() + " undeclared variables." + toList(undeclared));
		}
	}
	
    /**
     * Thrown when a method being parsed was not found.
     */
    static public class UnknownMethodEvaldException extends EvaldException {
        UnknownMethodEvaldException(String str) {
            super("Syntax error - unknown method at " + str);
		}
	}

    /**
     * Thrown when the expression being parsed was unexpectedly empty.
     */
    static public class EmptyExpressionEvaldException extends EvaldException {
		EmptyExpressionEvaldException(ExpressionString expression) {
			super("Unsupported empty expression " + expression.expression);
		}
	}

    /**
     * Thrown when the parser expected an operator at the current position in the expression
     */
    static public class OperatorExpectedEvaldException extends EvaldException {
        OperatorExpectedEvaldException(ExpressionString expression) {
            super("An operator was expected at " + expression.expression);
        }
    }

    /**
     * Thrown when a specified token does not conform to the required naming conventions.
     * Tokens must contain only alphanumeric characters or underscores, and cannot begin with a digit.
     */
    static public class InvalidTokenEvaldException extends EvaldException {
        InvalidTokenEvaldException(String token) {
            super("The token " + token + " is invalid. Tokens must start with a letter or underscore, and may only contain underscores or alphanumeric characters.");
        }
    }
}