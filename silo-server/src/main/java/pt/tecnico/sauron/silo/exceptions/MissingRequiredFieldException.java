package pt.tecnico.sauron.silo.exceptions;

public class MissingRequiredFieldException extends InvalidArgumentsException {

    private static final String MESSAGE = "Missing required field: '%s'";

    public MissingRequiredFieldException(String fieldName) {
        super(String.format(MESSAGE, fieldName));
    }

    public MissingRequiredFieldException(String fieldName, Throwable cause) {
        super(String.format(MESSAGE, fieldName), cause);
    }
}
