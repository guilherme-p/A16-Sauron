package pt.tecnico.sauron.silo.exceptions;

public class InvalidPersonIdRegexException extends InvalidArgumentsException {

    private static final String MESSAGE = "Invalid person id regex: '%s'";

    public InvalidPersonIdRegexException(String regex) {
        super(String.format(MESSAGE, regex));
    }

    public InvalidPersonIdRegexException(String regex, Throwable cause) {
        super(String.format(MESSAGE, regex), cause);
    }
}
