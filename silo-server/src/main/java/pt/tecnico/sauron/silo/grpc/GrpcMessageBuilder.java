package pt.tecnico.sauron.silo.grpc;

import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.SavedObservation;
import pt.tecnico.sauron.silo.domain.SavedObservationVisitor;
import pt.tecnico.sauron.silo.domain.Car;
import pt.tecnico.sauron.silo.domain.Person;
import pt.tecnico.sauron.silo.replication.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builder for grpc messages
 * Static methods only to be used whenever a message needs to be constructed
 */
public class GrpcMessageBuilder {

    /* Builders for Gossip Service */

    private static final GossipRequest.Builder gossipRequestBuilder
            = GossipRequest.newBuilder();

    private static final GossipResponse.Builder gossipResponseBuilder
            = GossipResponse.newBuilder();

    private static final Operation.Builder operationBuilder
            = Operation.newBuilder();

    /* Builders for Silo Service */

    private static final CamJoinData.Builder camJoinDataBuilder
            = CamJoinData.newBuilder();

    private static final CamJoinResponse.Builder camJoinResponseBuilder
            = CamJoinResponse.newBuilder();

    private static final CamInfoResponse.Builder camInfoResponseBuilder
            = CamInfoResponse.newBuilder();

    private static final CamReportData.Builder camReportDataBuilder
            = CamReportData.newBuilder();

    private static final CamReportResponse.Builder camReportResponseBuilder
            = CamReportResponse.newBuilder();

    private static final SpotterTrackResponse.Builder spotterTrackResponseBuilder
            = SpotterTrackResponse.newBuilder();

    private static final SpotterTrackMatchResponse.Builder spotterTrackMatchResponseBuilder
            = SpotterTrackMatchResponse.newBuilder();

    private static final SpotterTraceResponse.Builder spotterTraceResponseBuilder
            = SpotterTraceResponse.newBuilder();

    private static final PingResponse.Builder pingResponseBuilder
            = PingResponse.newBuilder();

    private static final ClearResponse.Builder clearResponseBuilder
            = ClearResponse.newBuilder();

    private static final InitData.Builder initDataBuilder
            = InitData.newBuilder();

    private static final InitResponse.Builder initResponseBuilder
            = InitResponse.newBuilder();

    private static final CamInfo.Builder camInfoBuilder
            = CamInfo.newBuilder();

    private static final Coordinates.Builder coordinatesBuilder
            = Coordinates.newBuilder();

    private static final ObservationInfo.Builder observationInfoBuilder
            = ObservationInfo.newBuilder();

    private static final Observation.Builder observationBuilder
            = Observation.newBuilder();

    private static final ObjectInfo.Builder objectInfoBuilder
            = ObjectInfo.newBuilder();

    private static final Timestamp.Builder timestampBuilder
            = Timestamp.newBuilder();

    private static final VectorTS.Builder vectorTimestampBuilder
            = VectorTS.newBuilder();

    /* Aux classes */

    private static final AuxObservationInfoBuilder auxObservationInfoBuilder
            = new AuxObservationInfoBuilder();

    private static final AuxOperationBuilder auxOperationBuilder
            = new AuxOperationBuilder();

    private GrpcMessageBuilder() {}

    /* Gossip Responses */

    public static GossipRequest buildGossipRequest(List<Operation> log, VectorTS ts, int replicaInstance) {
        return gossipRequestBuilder.clear().addAllLog(log).setTs(ts).setReplicaInstance(replicaInstance).build();
    }

    public static GossipResponse buildGossipResponse() {
        return gossipResponseBuilder.clear().build();
    }

    /* Update Responses */

    public static CamJoinResponse buildCamJoinResponse(VectorTS updateId) {
        return camJoinResponseBuilder.clear().setUpdateID(updateId).build();
    }

    public static CamReportResponse buildCamReportResponse(VectorTS updateId) {
        return camReportResponseBuilder.clear().setUpdateID(updateId).build();
    }

    public static ClearResponse buildClearResponse(VectorTS updateId) {
        return clearResponseBuilder.clear().setUpdateID(updateId).build();
    }

    public static InitResponse buildInitResponse(VectorTS updateId) {
        return initResponseBuilder.clear().setUpdateID(updateId).build();
    }

    /* Query Responses */

    public static CamInfoResponse buildCamInfoResponse(Coordinates coordinates, VectorTS newTS) {
        return camInfoResponseBuilder.clear().setCoordinates(coordinates).setNew(newTS).build();
    }

    public static SpotterTrackResponse buildSpotterTrackResponse(ObservationInfo observationInfo, VectorTS newTS) {
        return spotterTrackResponseBuilder.clear().setObservationInfo(observationInfo).setNew(newTS).build();
    }

    public static SpotterTrackMatchResponse buildSpotterTrackMatchResponse(List<ObservationInfo> observationInfos, VectorTS newTS) {
        return spotterTrackMatchResponseBuilder.clear().addAllObservationInfos(observationInfos).setNew(newTS).build();
    }

    public static SpotterTraceResponse buildSpotterTraceResponse(List<ObservationInfo> observationInfos, VectorTS newTS) {
        return spotterTraceResponseBuilder.clear().addAllObservationInfos(observationInfos).setNew(newTS).build();
    }

    /* Ping Response */

    public static PingResponse buildPingResponse(String message) {
        return pingResponseBuilder.clear().setMessage(message).build();
    }

    /* Build Sub messages */

    public static Operation buildOperation(Op operation, CamJoinData camInfoData,
                                           VectorTS prev, VectorTS ts, int replicaInstance) {
        return operationBuilder.clear().setOperation(operation).setCamJoinData(camInfoData)
                .setPrev(prev).setTs(ts).setReplicaInstance(replicaInstance).build();
    }

    public static Operation buildOperation(Op operation, CamReportData camReportData,
                                           VectorTS prev, VectorTS ts, int replicaInstance) {
        return operationBuilder.clear().setOperation(operation).setCamReportData(camReportData)
                .setPrev(prev).setTs(ts).setReplicaInstance(replicaInstance).build();
    }

    public static Operation buildOperation(Op operation, VectorTS prev, VectorTS ts, int replicaInstance) {
        return operationBuilder.clear().setOperation(operation)
                .setPrev(prev).setTs(ts).setReplicaInstance(replicaInstance).build();
    }

    public static Operation buildOperation(Op operation, InitData initData,
                                           VectorTS prev, VectorTS ts, int replicaInstance) {
        return operationBuilder.clear().setOperation(operation).setInitData(initData)
                .setPrev(prev).setTs(ts).setReplicaInstance(replicaInstance).build();
    }

    public static Operation buildOperation(Update update, int replicaInstance) {
        return auxOperationBuilder.buildOperationFromUpdate(update, replicaInstance);
    }

    public static CamJoinData buildCamJoinData(String name, Coordinates coordinates) {
        return camJoinDataBuilder.clear().setName(name).setCoordinates(coordinates).build();
    }

    public static CamReportData buildCamReportData(String camName, List<Observation> observations) {
        return camReportDataBuilder.clear().setName(camName).addAllObservations(observations).build();
    }

    public static InitData buildInitData(List<CamInfo> cameras, List<ObservationInfo> observations) {
        return initDataBuilder.clear().addAllCameras(cameras).addAllObservationInfos(observations).build();
    }

    public static VectorTS buildVectorTimestamp(List<Long> timestamps) {
        return vectorTimestampBuilder.clear()
                .addAllTimestamp(timestamps)
                .build();
    }

    public static CamInfo buildCamInfo(String name, Coordinates coordinates) {
        return camInfoBuilder.clear().setName(name).setCoordinates(coordinates).build();
    }

    public static Coordinates buildCoordinates(Double latitude, Double longitude) {
        return coordinatesBuilder.clear().setLatitude(latitude).setLongitude(longitude).build();
    }

    public static ObservationInfo buildObservationInfo(Observation observation, CamInfo camInfo) {
        return observationInfoBuilder.clear().setObservation(observation).setCamInfo(camInfo).build();
    }

    public static Observation buildObservation(ObjectInfo objectInfo, Timestamp timestamp) {
        return observationBuilder.clear().setObjectInfo(objectInfo).setTimestamp(timestamp).build();
    }

    public static ObjectInfo buildObjectInfo(ObjectType objectType, Long numericId) {
        return objectInfoBuilder.clear().setType(objectType).setNumericId(numericId).build();
    }

    public static ObjectInfo buildObjectInfo(ObjectType objectType, String alphanumericId) {
        return objectInfoBuilder.clear().setType(objectType).setAlphanumericId(alphanumericId).build();
    }

    public static ObjectType buildObjectType(SavedObservation.Type type) {
        switch (type) {
            case PERSON:
                return ObjectType.PERSON;
            case CAR:
                return ObjectType.CAR;
            default:
                return ObjectType.UNKNOWN_TYPE;
        }
    }

    public static Timestamp buildTimeStamp(Instant instant) {
        return timestampBuilder.clear().setSeconds(instant.getEpochSecond()).build();
    }

    public static ObservationInfo buildObservationInfo(SavedObservation<?> savedObservation) {
        return auxObservationInfoBuilder.buildObservationInfoFromSavedObservation(savedObservation);
    }

    private static class AuxObservationInfoBuilder implements SavedObservationVisitor {

        private ObjectType lastObjectType;
        private ObjectInfo lastObjectInfo;

        private <T extends Comparable<T>> ObservationInfo buildObservationInfoFromSavedObservation(SavedObservation<T> savedObservation) {
            Camera camera = savedObservation.getCamera();
            Coordinates coordinates = buildCoordinates(camera.getLatitude(), camera.getLongitude());
            CamInfo camInfo = buildCamInfo(camera.getName(), coordinates);
            lastObjectType = buildObjectType(savedObservation.getType());
            /* Generate last object info */
            savedObservation.accept(this);
            Timestamp timestamp = buildTimeStamp(savedObservation.getTimestamp());
            Observation observation = buildObservation(lastObjectInfo, timestamp);
            return buildObservationInfo(observation, camInfo);
        }

        @Override
        public void visit(Person person) {
            lastObjectInfo = buildObjectInfo(lastObjectType, person.getId());
        }

        @Override
        public void visit(Car car) {
            lastObjectInfo = buildObjectInfo(lastObjectType, car.getId());
        }
    }

    private static class AuxOperationBuilder implements UpdateVisitor {

        private Operation lastOperation;
        private int replicaInstance;

        private Operation buildOperationFromUpdate(Update update, int replicaInstance) {
            this.replicaInstance = replicaInstance;
            update.accept(this);
            return lastOperation;
        }

        @Override
        public void visit(CamJoin camJoin) {
            Coordinates coordinates = buildCoordinates(camJoin.getLatitude(), camJoin.getLongitude());
            CamJoinData data = buildCamJoinData(camJoin.getName(), coordinates);
            VectorTS prev = buildVectorTimestamp(camJoin.getPrev().toList());
            VectorTS ts = buildVectorTimestamp(camJoin.getTimestamp().toList());
            lastOperation = buildOperation(Op.CAM_JOIN, data, prev, ts, replicaInstance);
        }

        @Override
        public void visit(CamReport camReport) {
            List<Observation> observations = buildObservations(camReport);
            CamReportData data = buildCamReportData(camReport.getName(), observations);
            VectorTS prev = buildVectorTimestamp(camReport.getPrev().toList());
            VectorTS ts = buildVectorTimestamp(camReport.getTimestamp().toList());
            lastOperation = buildOperation(Op.CAM_REPORT, data, prev, ts, replicaInstance);
        }

        private List<Observation> buildObservations(CamReport camReport) {
            Timestamp ts = buildTimeStamp(camReport.getInstant());
            Stream<Observation> peopleObservations = camReport.getPeopleIds().stream()
                    .map(id -> buildObservation(buildObjectInfo(ObjectType.PERSON, id), ts));
            Stream<Observation> carObservations = camReport.getCarPlates().stream()
                    .map(plate -> buildObservation(buildObjectInfo(ObjectType.CAR, plate), ts));
            return Stream.concat(peopleObservations, carObservations)
                    .collect(Collectors.toList());
        }

        @Override
        public void visit(Clear clear) {
            VectorTS prev = buildVectorTimestamp(clear.getPrev().toList());
            VectorTS ts = buildVectorTimestamp(clear.getTimestamp().toList());
            lastOperation = buildOperation(Op.CLEAR, prev, ts, replicaInstance);
        }

        @Override
        public void visit(Init init) {
            List<CamInfo> camInfos = buildCamInfos(init.getCameras());
            List<ObservationInfo> observations = buildObservationInfos(init.getPeople(), init.getCars());
            InitData data = buildInitData(camInfos, observations);
            VectorTS prev = buildVectorTimestamp(init.getPrev().toList());
            VectorTS ts = buildVectorTimestamp(init.getTimestamp().toList());
            lastOperation = buildOperation(Op.INIT, data, prev, ts, replicaInstance);
        }

        private List<CamInfo> buildCamInfos(List<Camera> cameras) {
            return cameras.stream()
                    .map(camera -> buildCamInfo(camera.getName(),
                            buildCoordinates(camera.getLatitude(), camera.getLongitude())))
                    .collect(Collectors.toList());
        }

        private List<ObservationInfo> buildObservationInfos(List<Person> people, List<Car> cars) {
            return Stream.concat(people.stream(), cars.stream())
                    .map(GrpcMessageBuilder::buildObservationInfo)
                    .collect(Collectors.toList());
        }
    }
}
