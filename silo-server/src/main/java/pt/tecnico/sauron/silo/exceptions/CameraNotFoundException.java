package pt.tecnico.sauron.silo.exceptions;

public class CameraNotFoundException extends ObjectNotFoundException {

    private static final String MESSAGE = "Camera not found";
    private static final String MESSAGE_ID = "Camera not found: '%s'";

    public CameraNotFoundException() {
        super(MESSAGE);
    }

    public CameraNotFoundException(String id) {
        super(String.format(MESSAGE_ID, id));
    }

    public CameraNotFoundException(Throwable cause) {
        super(MESSAGE, cause);
    }

    public CameraNotFoundException(String id, Throwable cause) {
        super(String.format(MESSAGE_ID, cause));
    }
}
