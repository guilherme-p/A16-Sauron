package pt.tecnico.sauron.silo.replication;

/**
 * Class to store an update to be applied or that
 * is already applied but not received by all servers
 */
public abstract class Update implements Comparable<Update> {

    private int replicaInstance;
    private VectorTimestamp timestamp;
    private VectorTimestamp prev;

    public Update(int replicaInstance, VectorTimestamp timestamp, VectorTimestamp prev) {
        this.replicaInstance = replicaInstance;
        this.timestamp = VectorTimestamp.copyOf(timestamp);
        this.prev = VectorTimestamp.copyOf(prev);
    }

    public int getReplicaInstance() {
        return replicaInstance;
    }

    public VectorTimestamp getTimestamp() {
        return timestamp;
    }

    public VectorTimestamp getPrev() {
        return prev;
    }

    /**
     * Compares updates according to their {@link VectorTimestamp}.
     * Warning: This class has a natural ordering that is inconsistent with equals,
     * meaning if x.compareTo(y) == 0 doesnt mean x.equals(y), because timestamps
     * might be concurrent
     * @param other update to compare
     * @return the result of comparing the timestamps
     */
    @Override
    public int compareTo(Update other) {
        return timestamp.compareTo(other.timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Update update = (Update) o;
        return timestamp.equals(update.timestamp);
    }

    @Override
    public int hashCode() {
        return timestamp.hashCode();
    }

    /**
     * Visitor method to distinguish different operations
     * @param visitor visitor to process updates
     */
    public abstract void accept(UpdateVisitor visitor);

    @Override
    public String toString() {
        return "{" +
                "ts=" + timestamp +
                ", prev=" + prev +
                ", replica=" + replicaInstance +
                '}';
    }
}
