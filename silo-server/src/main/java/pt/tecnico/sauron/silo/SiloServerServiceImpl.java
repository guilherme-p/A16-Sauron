package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.grpc.GrpcController;
import pt.tecnico.sauron.silo.grpc.GrpcMessageBuilder;
import pt.tecnico.sauron.silo.exceptions.*;

import pt.tecnico.sauron.silo.grpc.*;
import pt.tecnico.sauron.silo.utils.CheckUtils;

import static io.grpc.Status.*;

public class SiloServerServiceImpl extends SiloGrpc.SiloImplBase {

    private final GrpcController grpcController;

    public SiloServerServiceImpl(GrpcController grpcController) {
        this.grpcController = grpcController;
    }

    @Override
    public void camJoin(CamJoinRequest request, StreamObserver<CamJoinResponse> responseObserver) {
        try {
            CamJoinResponse response = grpcController.handleCamJoin(request);
            synchronized (this) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (CameraAlreadyExistsException exception) {
            onCameraAlreadyExistsException(exception, responseObserver);
        } catch (InvalidArgumentsException exception) {
            onInvalidArgumentsException(exception, responseObserver);
        }
    }

    @Override
    public void camInfo(CamInfoRequest request, StreamObserver<CamInfoResponse> responseObserver) {
        try {
            CamInfoResponse response = grpcController.handleCamInfo(request);
            synchronized (this) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (ObjectNotFoundException exception) {
            onObjectNotFoundException(exception, responseObserver);
        } catch (InvalidArgumentsException exception) {
            onInvalidArgumentsException(exception, responseObserver);
        }
    }

    @Override
    public void camReport(CamReportRequest request, StreamObserver<CamReportResponse> responseObserver) {
        try {
            CamReportResponse response = grpcController.handleCamReport(request);
            synchronized (this) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (ObjectNotFoundException e) {
            onObjectNotFoundException(e, responseObserver);
        } catch (InvalidArgumentsException e) {
            onInvalidArgumentsException(e, responseObserver);
        }
    }

    @Override
    public void spotterTrack(SpotterTrackRequest request, StreamObserver<SpotterTrackResponse> responseObserver) {
        try {
            SpotterTrackResponse response = grpcController.handleSpotterTrack(request);
            synchronized (this) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (ObjectNotFoundException exception) {
            onObjectNotFoundException(exception, responseObserver);
        } catch (InvalidArgumentsException exception) {
            onInvalidArgumentsException(exception, responseObserver);
        }
    }

    @Override
    public void spotterTrackMatch(SpotterTrackMatchRequest request, StreamObserver<SpotterTrackMatchResponse> responseObserver) {
        try {
            SpotterTrackMatchResponse response = grpcController.handleSpotterTrackMatch(request);
            synchronized (this) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (InvalidArgumentsException exception) {
            onInvalidArgumentsException(exception, responseObserver);
        }
    }

    @Override
    public void spotterTrace(SpotterTraceRequest request, StreamObserver<SpotterTraceResponse> responseObserver) {
        try {
            SpotterTraceResponse response = grpcController.handleSpotterTrace(request);
            synchronized (this) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (InvalidArgumentsException exception) {
            onInvalidArgumentsException(exception, responseObserver);
        }
    }

    @Override
    public void ctrlPing(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        try {
            String message = CheckUtils.notNullOrBlank(request.getMessage(), "Message");
            PingResponse response =  GrpcMessageBuilder.buildPingResponse("Hello " + message + "!");
            synchronized (this) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (InvalidArgumentsException exception) {
            onInvalidArgumentsException(exception, responseObserver);
        }
    }

    @Override
    public void ctrlClear(ClearRequest request, StreamObserver<ClearResponse> responseObserver) {
        ClearResponse response = grpcController.handleClear(request);
        synchronized (this) {
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void ctrlInit(InitRequest request, StreamObserver<InitResponse> responseObserver) {
        try {
            InitResponse response = grpcController.handleInit(request);
            synchronized (this) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (CameraAlreadyExistsException exception) {
            onCameraAlreadyExistsException(exception, responseObserver);
        } catch (ObjectNotFoundException exception) {
            onObjectNotFoundException(exception, responseObserver);
        } catch (InvalidArgumentsException exception) {
            onInvalidArgumentsException(exception, responseObserver);
        }
    }

    /* Methods for general exception handling */

    private void onCameraAlreadyExistsException(CameraAlreadyExistsException exception,
                                                StreamObserver<?> responseObserver) {
        synchronized (this) {
            responseObserver.onError(ALREADY_EXISTS.withDescription(exception.getMessage()).asRuntimeException());
        }
    }

    private void onInvalidArgumentsException(InvalidArgumentsException exception,
                                             StreamObserver<?> responseObserver) {
        synchronized (this) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(exception.getMessage()).asRuntimeException());
        }
    }

    private void onObjectNotFoundException(ObjectNotFoundException exception,
                                           StreamObserver<?> responseObserver) {
        synchronized (this) {
            responseObserver.onError(NOT_FOUND.withDescription(exception.getMessage()).asRuntimeException());
        }
    }
}
