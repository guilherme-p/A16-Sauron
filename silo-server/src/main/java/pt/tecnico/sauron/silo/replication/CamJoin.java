package pt.tecnico.sauron.silo.replication;

/**
 * Class to represent cam join update
 */
public class CamJoin extends Update {

    private final String name;
    private final Double latitude;
    private final Double longitude;

    public CamJoin(String name, Double latitude, Double longitude,
                   int replicaInstance, VectorTimestamp timestamp, VectorTimestamp prev) {
        super(replicaInstance, timestamp, prev);
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Override
    public void accept(UpdateVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "CamJoin{" +
                "name='" + name + '\'' +
                ", info=" + super.toString() +
                '}';
    }
}
