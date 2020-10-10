package pt.tecnico.sauron.silo.exceptions;

public class InvalidCarPlateRegexException extends InvalidArgumentsException {

    private static final String MESSAGE = "Invalid car plate regex: '%s'";

    public InvalidCarPlateRegexException(String regex) {
        super(String.format(MESSAGE, regex));
    }

    public InvalidCarPlateRegexException(String regex, Throwable cause) {
        super(String.format(MESSAGE, regex), cause);
    }
}
