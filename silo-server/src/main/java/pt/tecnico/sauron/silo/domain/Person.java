package pt.tecnico.sauron.silo.domain;

import java.time.Instant;

/**
 * Domain class for a person observation
 */
public class Person extends SavedObservation<Long> {

    public Person(Long id, Instant timestamp, Camera camera) {
        super(id, Type.PERSON, timestamp, camera);
    }

    @Override
    public void accept(SavedObservationVisitor visitor) {
        visitor.visit(this);
    }
}
