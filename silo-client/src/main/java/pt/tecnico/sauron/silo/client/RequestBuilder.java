package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RequestBuilder {

    private static final CamJoinRequest.Builder camJoinRequestBuilder
            = CamJoinRequest.newBuilder();

    private static final CamInfoRequest.Builder camInfoRequestBuilder
            = CamInfoRequest.newBuilder();

    private static final CamReportRequest.Builder camReportRequestBuilder
            = CamReportRequest.newBuilder();

    private static final SpotterTrackRequest.Builder spotterTrackRequestBuilder
            = SpotterTrackRequest.newBuilder();

    private static final SpotterTrackMatchRequest.Builder spotterTrackMatchRequestBuilder
            = SpotterTrackMatchRequest.newBuilder();

    private static final SpotterTraceRequest.Builder spotterTraceRequestBuilder
            = SpotterTraceRequest.newBuilder();

    private static final PingRequest.Builder pingRequestBuilder
            = PingRequest.newBuilder();

    private static final ClearRequest.Builder clearRequestBuilder
            = ClearRequest.newBuilder();

    private static final InitRequest.Builder initRequestBuilder
            = InitRequest.newBuilder();

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

    private RequestBuilder() {}

    /* Update Requests */

    public static CamJoinRequest buildCamJoinRequest(CamInfo info, VectorTS prev) {
        return camJoinRequestBuilder.clear().setInfo(info).setPrev(prev).build();
    }

    public static CamReportRequest buildCamReportRequest(String camName, List<ObjectInfo> observations, VectorTS prev) {
        return camReportRequestBuilder.clear().setCamName(camName).addAllObservations(observations).setPrev(prev).build();
    }

    public static ClearRequest buildClearRequest(VectorTS prev) {
        return clearRequestBuilder.clear().setPrev(prev).build();
    }

    public static InitRequest buildInitRequest(List<CamInfo> cameras, List<ObservationInfo> observations, VectorTS prev) {
        return initRequestBuilder.clear().addAllCameras(cameras).addAllObservations(observations).setPrev(prev).build();
    }

    /* Query Requests */

    public static CamInfoRequest buildCamInfoRequest(String name, VectorTS prev) {
        return camInfoRequestBuilder.clear().setName(name).setPrev(prev).build();
    }

    public static SpotterTrackRequest buildSpotterTrackRequest(ObjectInfo objectInfo, VectorTS prev) {
        return spotterTrackRequestBuilder.clear().setObjectInfo(objectInfo).setPrev(prev).build();
    }

    public static SpotterTrackMatchRequest buildSpotterTrackMatchRequest(ObjectType objectType, String regex, VectorTS prev) {
        return spotterTrackMatchRequestBuilder.clear().setObjectType(objectType).setRegex(regex).setPrev(prev).build();
    }

    public static SpotterTraceRequest buildSpotterTraceRequest(ObjectInfo info, VectorTS prev) {
        return spotterTraceRequestBuilder.clear().setObjectInfo(info).setPrev(prev).build();
    }

    /* Ping Request */

    public static PingRequest buildPingRequest(String message) {
        return pingRequestBuilder.clear().setMessage(message).build();
    }

    /* Build Requests sub messages */

    public static VectorTS buildVectorTimestamp(Long[] timestamps) {
        return vectorTimestampBuilder.clear()
                .addAllTimestamp(Arrays.stream(timestamps).collect(Collectors.toList()))
                .build();
    }

    public static CamInfo buildCamInfo(String name, Coordinates coordinates) {
        return camInfoBuilder.clear().setName(name).setCoordinates(coordinates).build();
    }

    public static Coordinates buildCoordinates(Double latitude, Double longitude) {
        return coordinatesBuilder.clear().setLatitude(latitude).setLongitude(longitude).build();
    }

    public static ObservationInfo buildObservationInfo(Observation observation, CamInfo camInfo) {
        return observationInfoBuilder.clear().setCamInfo(camInfo).setObservation(observation).build();
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

    public static ObjectType buildObjectTypePerson() {
        return ObjectType.PERSON;
    }

    public static ObjectType buildObjectTypeCar() {
        return ObjectType.CAR;
    }

    public static ObjectType buildObjectTypeUnknown() {
        return ObjectType.UNKNOWN_TYPE;
    }

    public static Timestamp buildTimeStamp(Instant instant) {
        return timestampBuilder.clear().setSeconds(instant.getEpochSecond()).build();
    }

}