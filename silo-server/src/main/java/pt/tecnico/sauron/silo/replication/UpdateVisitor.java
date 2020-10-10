package pt.tecnico.sauron.silo.replication;

/**
 * Interface to visit update operations
 */
public interface UpdateVisitor {

    void visit(CamJoin camJoin);
    void visit(CamReport camReport);
    void visit(Clear clear);
    void visit(Init init);
}
