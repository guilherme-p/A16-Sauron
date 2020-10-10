package pt.tecnico.sauron.silo;


import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.exceptions.InvalidArgumentsException;
import pt.tecnico.sauron.silo.grpc.*;

import static io.grpc.Status.INVALID_ARGUMENT;

public class SiloServerReplicationImpl extends GossipGrpc.GossipImplBase {

    private final GrpcController grpcController;

    public SiloServerReplicationImpl(GrpcController grpcController) {
        this.grpcController = grpcController;
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        try {
            GossipResponse response = grpcController.handleGossip(request);
            synchronized (this) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (InvalidArgumentsException exception) {
            onInvalidArgumentsException(exception, responseObserver);
        }
    }

    private void onInvalidArgumentsException(InvalidArgumentsException exception,
                                             StreamObserver<?> responseObserver) {
        synchronized (this) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(exception.getMessage()).asRuntimeException());
        }
    }
}
