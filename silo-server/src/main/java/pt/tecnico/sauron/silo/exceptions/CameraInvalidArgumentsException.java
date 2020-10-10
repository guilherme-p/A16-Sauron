package pt.tecnico.sauron.silo.exceptions;

public class CameraInvalidArgumentsException extends InvalidArgumentsException {

    public static final String INVALID_CAMERA_NAME = "Invalid camera name";
    public static final String INVALID_LATITUDE = "Invalid value for latitude";
    public static final String INVALID_LONGITUDE = "Invalid value for longitude";

    public CameraInvalidArgumentsException(String message) {
        super(message);
    }

    public CameraInvalidArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }
}
