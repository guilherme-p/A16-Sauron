package pt.tecnico.sauron.silo.service;

import pt.tecnico.sauron.silo.replication.Update;
import pt.tecnico.sauron.silo.replication.VectorTimestamp;

import java.util.List;

/**
 * Interface to send gossip messages to other replicas
 */
public interface GossipSender {

    /**
     * Sends updates to the necessary replicas
     * @param possibleUpdatesToSend list of updates that any replica might not know. Can be filtered
     *                              for specific replicas according to their timestamp in tableTS
     * @param replicaTimestamp timestamp of the replicas
     * @param tableTS with the last received timestamps from other replicas
     */
    void send(List<Update> possibleUpdatesToSend, VectorTimestamp replicaTimestamp, VectorTimestamp[] tableTS);
}
