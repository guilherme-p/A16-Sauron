package pt.tecnico.sauron.silo.replication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class VectorTimestamp implements Comparable<VectorTimestamp> {

    private final Long[] timestamp;

    /**
     * Constructs VectorTimestamp with all entries to 0
     * @param size of the vector
     */
    public VectorTimestamp(int size) {
        this.timestamp = new Long[size];
        Arrays.fill(timestamp, 0L);
    }

    /**
     * Constructs VectorTimestamp with the given timestamps
     * @param timestamp to use
     */
    public VectorTimestamp(List<Long> timestamp) {
        this.timestamp = timestamp.toArray(new Long[0]);
    }

    private VectorTimestamp(VectorTimestamp other) {
        this.timestamp = Arrays.copyOf(other.timestamp, other.timestamp.length);
    }

    public List<Long> toList() {
        synchronized (this.timestamp) {
            return new ArrayList<>(Arrays.asList(this.timestamp));
        }
    }

    /**
     * Makes a deep copy of the given VectorTimestamp
     * @param other timestamp to copy
     * @return a new VectorTimestamp
     */
    public static VectorTimestamp copyOf(VectorTimestamp other) {
        return new VectorTimestamp(other);
    }

    /**
     * Increments the entry representing the replica instance
     * @param replicaInstance replica instance number
     */
    public void incrementReplicaInstanceValue(int replicaInstance) {
        synchronized (this.timestamp) {
            this.timestamp[replicaInstance - 1]++;
        }
    }

    /**
     * Sets the entry representing the replica instance to the given value
     * @param replicaInstance replica instance number
     * @param value value to set the given entry
     */
    public void setReplicaInstanceValue(int replicaInstance, Long value) {
        synchronized (this.timestamp) {
            this.timestamp[replicaInstance - 1] = value;
        }
    }

    /**
     * @param replicaInstance replica instance number
     * @return the value of the entry with the given replica instance number
     */
    public Long getReplicaInstanceValue(int replicaInstance) {
        synchronized (this.timestamp) {
            return this.timestamp[replicaInstance - 1];
        }
    }

    /**
     * @param other VectorTimestamp to compare
     * @return true if this happens before other and
     * false otherwise
     */
    public boolean happensBefore(VectorTimestamp other) {
        synchronized (this.timestamp) {
            checkSize(other);
            return IntStream.range(0, this.timestamp.length)
                    .allMatch(index -> this.timestamp[index] <= other.timestamp[index]);
        }
    }

    /**
     * @param other VectorTimestamp to compare
     * @return true if this happens after other and
     * false otherwise
     */
    public synchronized boolean happensAfter(VectorTimestamp other) {
        synchronized (this.timestamp) {
            checkSize(other);
            return IntStream.range(0, this.timestamp.length)
                    .allMatch(index -> this.timestamp[index] >= other.timestamp[index]);
        }
    }

    /**
     * Update this timestamp to the result of merging
     * this and other timestamps
     * @param other timestamp to merge
     */
    public synchronized void merge(VectorTimestamp other) {
        synchronized (this.timestamp) {
            checkSize(other);
            Arrays.setAll(this.timestamp, index -> Math.max(this.timestamp[index], other.timestamp[index]));
        }
    }

    /**
     * CompareTo for VectorTimestamps
     * Warning: This class has a natural ordering that is inconsistent with equals,
     * meaning if x.compareTo(y) == 0 doesnt mean x.equals(y)
     * @param other timestamp
     * @return -1 if other happens before this, 0 if the events
     * are concurrent or equal, 1 if other happens after this
     */
    @Override
    public int compareTo(VectorTimestamp other) {
        checkSize(other);
        // Assume both are true
        boolean happensBefore = true;
        boolean happensAfter = true;
        synchronized (this.timestamp) {
            for (int i = 0; i < this.timestamp.length; i++) {
                // If entry in this.timestamp < entry in other.timestamp
                // this.timestamp cant happen after other.timestamp
                if (this.timestamp[i] < other.timestamp[i]) {
                    happensAfter = false;
                }
                // If entry in this.timestamp > entry in other.timestamp
                // this.timestamp cant happen before other.timestamp
                else if (this.timestamp[i] > other.timestamp[i]) {
                    happensBefore = false;
                }
            }
        }
        // If this.timestamp doesnt happen before or after other.timestamp
        // the ts events are concurrent
        if (happensBefore && happensAfter) return 0;
        if (happensBefore) return -1;
        if (happensAfter) return 1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VectorTimestamp that = (VectorTimestamp) o;
        synchronized (this.timestamp) {
            return Arrays.equals(timestamp, that.timestamp);
        }
    }

    @Override
    public int hashCode() {
        synchronized (this.timestamp) {
            return Arrays.hashCode(timestamp);
        }
    }

    private void checkSize(VectorTimestamp other) {
        if (this.timestamp.length != other.timestamp.length) {
            throw new IllegalArgumentException("VectorTimestamps must be of same size");
        }
    }

    @Override
    public String toString() {
        synchronized (this.timestamp) {
            return Arrays.toString(timestamp);
        }
    }
}