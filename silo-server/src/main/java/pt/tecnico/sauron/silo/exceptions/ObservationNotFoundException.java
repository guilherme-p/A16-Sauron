package pt.tecnico.sauron.silo.exceptions;

public class ObservationNotFoundException extends ObjectNotFoundException {

    public ObservationNotFoundException(String message) {
        super(message);
    }

    public ObservationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
