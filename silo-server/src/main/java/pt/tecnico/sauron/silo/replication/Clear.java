package pt.tecnico.sauron.silo.replication;

/**
 * Class to represent clear update
 */
public class Clear  extends Update {

    public Clear(int replicaInstance, VectorTimestamp timestamp, VectorTimestamp prev) {
        super(replicaInstance, timestamp, prev);
    }

    @Override
    public void accept(UpdateVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Clear{" +
                "info=" + super.toString() +
                "}";
    }
}
