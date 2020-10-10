package pt.tecnico.sauron.silo.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class for Observations
 * @param <I> Id class for observation
 */
public abstract class SavedObservation<I extends Comparable<I>> implements Comparable<SavedObservation<I>> {

    public enum Type {
        PERSON, CAR
    }

    private final I id;

    private final Type type;

    private final Instant timestamp;

    private final Camera camera;

    public SavedObservation(I id, Type type, Instant timestamp, Camera camera) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.camera = camera;
    }

    public I getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Camera getCamera() {
        return camera;
    }

    public abstract void accept(SavedObservationVisitor visitor);

    @Override
    public String toString() {
        return "SavedObservation{" +
                "id=" + id +
                ", type=" + type +
                ", timestamp=" + timestamp +
                ", camera=" + camera +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedObservation<?> that = (SavedObservation<?>) o;
        return Objects.equals(id, that.id) &&
                type == that.type &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int compareTo(SavedObservation<I> o) {
        // sort by decreasing order of timestamps
        int p = o.timestamp.compareTo(timestamp);
        // if same timestamp, sort by increasing order of ids
        return (p != 0) ? p : id.compareTo(o.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, timestamp);
    }
}
