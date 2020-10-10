package pt.tecnico.sauron.silo.exceptions;

public class CarNotFoundException extends ObservationNotFoundException {

    private static final String MESSAGE = "Car not found: '%s'";

    public CarNotFoundException(String plate) {
        super(String.format(MESSAGE, plate));
    }

    public CarNotFoundException(String plate, Throwable cause) {
        super(String.format(MESSAGE, plate), cause);
    }
}