package pt.tecnico.sauron.silo.domain;

import java.time.Instant;

/**
 * Domain class for a car observation
 */
public class Car extends SavedObservation<String> {

    public Car(String plate, Instant timestamp, Camera camera) {
        super(plate, Type.CAR, timestamp, camera);
    }

    @Override
    public void accept(SavedObservationVisitor visitor) {
        visitor.visit(this);
    }
}
