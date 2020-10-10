package pt.tecnico.sauron.silo.exceptions;

public class InvalidPersonIdException extends InvalidArgumentsException {

    private static final String MESSAGE = "Invalid person id: '%d'";

    public InvalidPersonIdException(Long id) {
        super(String.format(MESSAGE, id));
    }

    public InvalidPersonIdException(Long id, Throwable cause) {
        super(String.format(MESSAGE, id), cause);
    }
}
