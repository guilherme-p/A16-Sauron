package pt.tecnico.sauron.silo.exceptions;

public class PersonNotFoundException extends ObservationNotFoundException {

    private static final String MESSAGE = "Person not found: '%d'";

    public PersonNotFoundException(Long id) {
        super(String.format(MESSAGE, id));
    }

    public PersonNotFoundException(Long id, Throwable cause) {
        super(String.format(MESSAGE, id), cause);
    }
}
