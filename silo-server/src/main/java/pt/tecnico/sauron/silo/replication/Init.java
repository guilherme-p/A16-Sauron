package pt.tecnico.sauron.silo.replication;

import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.Car;
import pt.tecnico.sauron.silo.domain.Person;

import java.util.List;

/**
 * Class to represent init update
 */
public class Init extends Update {

    private List<Camera> cameras;
    private List<Person> people;
    private List<Car> cars;

    public Init(List<Camera> cameras, List<Person> people, List<Car> cars,
                int replicaInstance, VectorTimestamp timestamp, VectorTimestamp prev) {
        super(replicaInstance, timestamp, prev);
        this.cameras = cameras;
        this.people = people;
        this.cars = cars;
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public List<Person> getPeople() {
        return people;
    }

    public List<Car> getCars() {
        return cars;
    }

    @Override
    public void accept(UpdateVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Init{" +
                "cameras=" + cameras +
                ", people=" + people +
                ", cars=" + cars +
                ", info=" + super.toString() +
                '}';
    }
}
