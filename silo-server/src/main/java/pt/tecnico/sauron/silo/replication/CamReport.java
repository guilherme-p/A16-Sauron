package pt.tecnico.sauron.silo.replication;

import java.time.Instant;
import java.util.List;

/**
 * Class to represent cam report update
 */
public class CamReport extends Update {

    private String name;
    private List<Long> peopleIds;
    private List<String> carPlates;
    // Instant when it was received by the first replica so that all replicas share the same instant
    private Instant instant;

    public CamReport(String name, List<Long> peopleIds, List<String> carPlates, Instant instant,
                     int replicaInstance, VectorTimestamp timestamp, VectorTimestamp prev) {
        super(replicaInstance, timestamp, prev);
        this.name = name;
        this.peopleIds = peopleIds;
        this.carPlates = carPlates;
        this.instant = instant;
    }

    public String getName() {
        return name;
    }

    public List<Long> getPeopleIds() {
        return peopleIds;
    }

    public List<String> getCarPlates() {
        return carPlates;
    }

    public Instant getInstant() {
        return instant;
    }

    @Override
    public void accept(UpdateVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "CamReport{" +
                "name='" + name + '\'' +
                ", info=" + super.toString() +
                '}';
    }
}
