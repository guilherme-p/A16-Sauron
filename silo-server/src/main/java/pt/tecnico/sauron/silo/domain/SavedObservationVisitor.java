package pt.tecnico.sauron.silo.domain;

public interface SavedObservationVisitor {

    void visit(Person person);

    void visit(Car car);
}
