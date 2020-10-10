package pt.tecnico.sauron.silo.exceptions;

public class InvalidCarPlateException extends InvalidArgumentsException {

    private static final String MESSAGE = "Invalid car plate: '%s'";

    public InvalidCarPlateException(String plate) {
        super(String.format(MESSAGE, plate));
    }

    public InvalidCarPlateException(String plate, Throwable cause) {
        super(String.format(MESSAGE, plate), cause);
    }
}
