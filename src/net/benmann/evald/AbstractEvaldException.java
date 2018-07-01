package net.benmann.evald;

import java.util.Collection;

@SuppressWarnings("serial")
public abstract class AbstractEvaldException extends RuntimeException {
    AbstractEvaldException() {
		super();
	}
	
    AbstractEvaldException(String message) {
		super(message);
	}
    
    static public class EvaldException extends AbstractEvaldException {
        EvaldException(String message) {
            super(message);
        }

        EvaldException withContext(String context) {
            return new EvaldException(getMessage() + " Syntax error in subexpression \"" + context + "\"");
        }
    }

    abstract <T extends AbstractEvaldException> T withContext(String context);

    /**
     * Thrown when an undeclared variable was found in an expression, and undeclared variables
     * were not allowed (with Expression.setAllowUndeclared).
     */
    static public class UndeclaredVariableEvaldException extends AbstractEvaldException {
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

        UndeclaredVariableEvaldException(String token) {
            super("The specified token " + token + " was not declared in this expression.");
        }

        UndeclaredVariableEvaldException withContext(String context) {
            return new UndeclaredVariableEvaldException(getMessage() + " in " + context);
        }
	}
	
    /**
     * Thrown when a method being parsed was not found.
     */
    static public class UnknownMethodEvaldException extends AbstractEvaldException {
        UnknownMethodEvaldException(String str) {
            super("Syntax error - unknown method at " + str);
		}

        UnknownMethodEvaldException withContext(String context) {
            return new UnknownMethodEvaldException(getMessage() + " in " + context);
        }
	}

    /**
     * Thrown when the expression being parsed was unexpectedly empty.
     */
    static public class EmptyExpressionEvaldException extends AbstractEvaldException {
		EmptyExpressionEvaldException(ExpressionString expression) {
			super("Unsupported empty expression " + expression.expression);
		}

        private EmptyExpressionEvaldException(String message) {
            super(message);
        }

        EmptyExpressionEvaldException withContext(String context) {
            return new EmptyExpressionEvaldException(getMessage() + " in " + context);
        }
	}

    /**
     * Thrown when the parser expected an operator at the current position in the expression
     */
    static public class OperatorExpectedEvaldException extends AbstractEvaldException {
        OperatorExpectedEvaldException(ExpressionString expression) {
            super("An operator was expected at " + expression.expression);
        }

        private OperatorExpectedEvaldException(String message) {
            super(message);
        }

        OperatorExpectedEvaldException withContext(String context) {
            return new OperatorExpectedEvaldException(getMessage() + " in " + context);
        }
    }

    /**
     * Thrown when a specified token does not conform to the required naming conventions.
     * Tokens must contain only alphanumeric characters or underscores, and cannot begin with a digit.
     */
    static public class InvalidTokenEvaldException extends AbstractEvaldException {
        InvalidTokenEvaldException(String token) {
            super("The token " + token + " is invalid. Tokens must start with a letter or underscore, and may only contain underscores or alphanumeric characters.");
        }

        InvalidTokenEvaldException withContext(String context) {
            return new InvalidTokenEvaldException(getMessage() + " in " + context);
        }
    }

    /**
     * Thrown when a component is uninitialised
     */
    static public class UninitialisedEvaldException extends AbstractEvaldException {
        UninitialisedEvaldException(String message) {
            super(message);
        }

        UninitialisedEvaldException withContext(String context) {
            return new UninitialisedEvaldException(getMessage() + " in " + context);
        }
    }
}