package pt.tecnico.sauron.silo.exceptions;

import pt.tecnico.sauron.silo.grpc.ObjectType;

public class UnknownObjectTypeException extends InvalidArgumentsException {

    private static final String MESSAGE = "Unknown object type: '%s'";

    public UnknownObjectTypeException(ObjectType objectType) {
        super(String.format(MESSAGE, objectType.toString()));
    }

    public UnknownObjectTypeException(ObjectType objectType, Throwable cause) {
        super(String.format(MESSAGE, objectType.toString()), cause);
    }
}
