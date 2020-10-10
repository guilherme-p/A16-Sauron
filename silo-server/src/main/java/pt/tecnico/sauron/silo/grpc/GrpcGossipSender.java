package pt.tecnico.sauron.silo.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.replication.*;
import pt.tecnico.sauron.silo.service.GossipSender;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to send gossip messages to other replicas
 */
public class GrpcGossipSender implements GossipSender {

    private final ZKNaming zkNaming;
    // Base path to search other replicas in zoo keeper
    private final String replicasBasePath;
    // This replica path in order not to send to itself
    private final String replicaPath;
    private final int replicaInstance;

    public GrpcGossipSender(String zooHost, String zooPort,
                            String replicasBasePath, int replicaInstance) {
        this.zkNaming = new ZKNaming(zooHost, zooPort);
        this.replicasBasePath = replicasBasePath;
        this.replicaInstance = replicaInstance;
        this.replicaPath = replicasBasePath + "/" + replicaInstance;
    }

    @Override
    public void send(List<Update> possibleUpdatesToSend, VectorTimestamp replicaTimestamp, VectorTimestamp[] tableTS) {
        String targetPath;
        try {
            for (ZKRecord record : zkNaming.listRecords(replicasBasePath)) {
                targetPath = record.getPath();
                // Check if sending to ourselves
                if (!replicaPath.equals(targetPath)) {
                    sendSingleReplica(possibleUpdatesToSend, replicaTimestamp, tableTS, targetPath, record.getURI());
                }
            }
        } catch (ZKNamingException exception) {
            System.out.println("SEVERE: Unable to connect to zooKeeper: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void sendSingleReplica(List<Update> possibleUpdatesToSend, VectorTimestamp replicaTimestamp, VectorTimestamp[] tableTS, String targetPath, String target) {
        // Build connection
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        GossipGrpc.GossipBlockingStub stub = GossipGrpc.newBlockingStub(channel);
        // Get target replica instance
        int targetReplicaInstance = Character.getNumericValue(target.charAt(target.length() - 1));
        // Get target replica timestamp
        VectorTimestamp targetTimestamp = tableTS[targetReplicaInstance - 1];
        try {
            // Build request
            VectorTS ts = GrpcMessageBuilder.buildVectorTimestamp(replicaTimestamp.toList());
            List<Operation> updatesToSend = possibleUpdatesToSend.stream()
                    // Only send update if it does not happen before target timestamp
                    // Don't send updates the target replica already has
                    .filter(update -> !update.getTimestamp().happensBefore(targetTimestamp))
                    .map(update -> GrpcMessageBuilder.buildOperation(update, replicaInstance))
                    .collect(Collectors.toList());
            System.out.println(String.format("INFO: Sending %d updates to replica %d ", updatesToSend.size(), targetReplicaInstance));
            GossipRequest request = GrpcMessageBuilder.buildGossipRequest(updatesToSend, ts, replicaInstance);
            // Execute request, no need to check response (empty)
            stub.gossip(request);
        } catch (StatusRuntimeException exception) {
            System.out.println(String.format("WARNING: Unable to send logs to '%s' at '%s'", targetPath, target));
        }
        channel.shutdown();
    }

}
