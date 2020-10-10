package pt.tecnico.sauron.silo.grpc;

import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.domain.*;
import pt.tecnico.sauron.silo.exceptions.*;
import pt.tecnico.sauron.silo.replication.*;
import pt.tecnico.sauron.silo.service.ReplicaService;
import pt.tecnico.sauron.silo.utils.CheckUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pt.tecnico.sauron.silo.grpc.GrpcFields.*;

/**
 * Class responsible for translation between gRPC objects
 * and the domain entities used by the services
 */
public class GrpcController {

    private ReplicaService replicaService;

    public GrpcController(ReplicaService replicaService) {
        this.replicaService = replicaService;
    }

    /* Query operations */

    /**
     * Process cam info request
     * @param request to process
     * @return response to send if no error has occurred
     * @throws CameraNotFoundException if no camera with the given name exists
     * @throws CameraInvalidArgumentsException if camera has invalid arguments
     * @throws MissingRequiredFieldException if a required field is missing
     */
    public CamInfoResponse handleCamInfo(CamInfoRequest request)
            throws CameraNotFoundException, CameraInvalidArgumentsException, MissingRequiredFieldException {
        // Parse request
        String name = CheckUtils.notNull(request.getName(), CAM_NAME);
        // Execute request
        QueryResponse<CameraCoordinates> response = replicaService.camInfo(name);
        // Build response
        CameraCoordinates cameraCoordinates = response.getData();
        Coordinates coordinates = GrpcMessageBuilder.buildCoordinates(cameraCoordinates.getLatitude(), cameraCoordinates.getLongitude());
        VectorTS ts = GrpcMessageBuilder.buildVectorTimestamp(response.getNewTS().toList());
        return GrpcMessageBuilder.buildCamInfoResponse(coordinates, ts);
    }

    /**
     * Processes spotter track request
     * @param request to process
     * @return the response to send if no error occurred
     * @throws ObservationNotFoundException if no observation was found
     * @throws InvalidPersonIdException if the given person id is invalid
     * @throws InvalidCarPlateException if the given car plate is invalid
     * @throws UnknownObjectTypeException if the object type is unknown
     * @throws MissingRequiredFieldException if a field is missing
     */
    public SpotterTrackResponse handleSpotterTrack(SpotterTrackRequest request)
            throws ObservationNotFoundException, InvalidPersonIdException, InvalidCarPlateException,
            UnknownObjectTypeException, MissingRequiredFieldException {
        // Parse request
        ObjectInfo objectInfo = CheckUtils.notNull(request.getObjectInfo(), OBJECT_INFO);
        ObjectType objectType = CheckUtils.notNull(objectInfo.getType(), OBJECT_TYPE);
        // Execute request
        QueryResponse<? extends SavedObservation<?>> response;
        switch (objectType) {
            case PERSON:
                response = replicaService.trackPerson(objectInfo.getNumericId());
                break;
            case CAR:
                response = replicaService.trackCar(objectInfo.getAlphanumericId());
                break;
            case UNKNOWN_TYPE:
            default:
                throw new UnknownObjectTypeException(objectType);
        }
        // Build response
        SavedObservation<?> savedObservation = response.getData();
        ObservationInfo observationInfo = GrpcMessageBuilder.buildObservationInfo(savedObservation);
        VectorTS ts = GrpcMessageBuilder.buildVectorTimestamp(response.getNewTS().toList());
        return GrpcMessageBuilder.buildSpotterTrackResponse(observationInfo, ts);
    }

    /**
     * Process spotter track match request
     * @param request to process
     * @return the response to send if no error occurred
     * @throws InvalidPersonIdRegexException if the given person id regex is invalid
     * @throws InvalidCarPlateRegexException if the given car plate regex is invalid
     * @throws UnknownObjectTypeException if the object type is unknown
     * @throws MissingRequiredFieldException if a field is missing
     */
    public SpotterTrackMatchResponse handleSpotterTrackMatch(SpotterTrackMatchRequest request)
            throws InvalidPersonIdRegexException, InvalidCarPlateRegexException,
            UnknownObjectTypeException, MissingRequiredFieldException {
        // Parse request
        ObjectType objectType = CheckUtils.notNull(request.getObjectType(), OBJECT_TYPE);
        // Execute request and build response params
        List<? extends SavedObservation<?>> observations;
        VectorTS ts;
        switch (objectType) {
            case PERSON:
                QueryResponse<List<Person>> responsePerson = replicaService.trackPersonMatch(request.getRegex());
                observations = responsePerson.getData();
                ts = GrpcMessageBuilder.buildVectorTimestamp(responsePerson.getNewTS().toList());
                break;
            case CAR:
                QueryResponse<List<Car>> responseCar = replicaService.trackCarMatch(request.getRegex());
                observations = responseCar.getData();
                ts = GrpcMessageBuilder.buildVectorTimestamp(responseCar.getNewTS().toList());
                break;
            case UNKNOWN_TYPE:
            default:
                throw new UnknownObjectTypeException(objectType);
        }
        // Build response
        List<ObservationInfo> observationInfos = observations.stream()
                .map(GrpcMessageBuilder::buildObservationInfo)
                .collect(Collectors.toList());
        return GrpcMessageBuilder.buildSpotterTrackMatchResponse(observationInfos, ts);
    }

    /**
     * Processes spotter trace request
     * @param request to process
     * @return the response to send if no error occurred
     * @throws InvalidPersonIdException if the given person id is invalid
     * @throws InvalidCarPlateException if the given car plate is invalid
     * @throws UnknownObjectTypeException if the object type is unknown
     * @throws MissingRequiredFieldException if a field is missing
     */
    public SpotterTraceResponse handleSpotterTrace(SpotterTraceRequest request)
            throws InvalidPersonIdException, InvalidCarPlateException,
            UnknownObjectTypeException, MissingRequiredFieldException {
        // Parse request
        ObjectInfo objectInfo = CheckUtils.notNull(request.getObjectInfo(), OBJECT_INFO);
        ObjectType objectType = CheckUtils.notNull(objectInfo.getType(), OBJECT_TYPE);
        List<? extends SavedObservation<?>> observations;
        VectorTS ts;
        // Execute request and build response params
        switch (objectType) {
            case PERSON:
                QueryResponse<List<Person>> responsePerson = replicaService.tracePerson(objectInfo.getNumericId());
                observations = responsePerson.getData();
                ts = GrpcMessageBuilder.buildVectorTimestamp(responsePerson.getNewTS().toList());
                break;
            case CAR:
                QueryResponse<List<Car>> responseCar = replicaService.traceCar(objectInfo.getAlphanumericId());
                observations = responseCar.getData();
                ts = GrpcMessageBuilder.buildVectorTimestamp(responseCar.getNewTS().toList());
                break;
            case UNKNOWN_TYPE:
            default:
                throw new UnknownObjectTypeException(objectType);
        }
        // Build response
        List<ObservationInfo> observationInfos = observations.stream()
                .map(GrpcMessageBuilder::buildObservationInfo)
                .collect(Collectors.toList());
        return GrpcMessageBuilder.buildSpotterTraceResponse(observationInfos, ts);
    }

    /* Update Operations */

    /**
     * Process cam join request
     * @param request to process
     * @return response to send if no error occurred
     * @throws CameraAlreadyExistsException if the camera name is duplicate
     * @throws CameraInvalidArgumentsException if camera has invalid arguments
     * @throws MissingRequiredFieldException if a required field is missing
     */
    public CamJoinResponse handleCamJoin(CamJoinRequest request)
            throws CameraAlreadyExistsException, CameraInvalidArgumentsException, MissingRequiredFieldException {
        // Parse request
        VectorTimestamp prev = new VectorTimestamp(request.getPrev().getTimestampList());
        CamInfo camInfo = CheckUtils.notNull(request.getInfo(), CAM_INFO);
        String name = camInfo.getName();
        Coordinates coordinates = camInfo.getCoordinates();
        // Execute request
        VectorTimestamp updateID = replicaService.camJoin(name, coordinates.getLatitude(), coordinates.getLongitude(), prev);
        // Build response
        VectorTS ts = GrpcMessageBuilder.buildVectorTimestamp(updateID.toList());
        return GrpcMessageBuilder.buildCamJoinResponse(ts);
    }

    /**
     * Process cam report request
     * @param request to process
     * @return response to send if no error has occurred
     * @throws InvalidPersonIdException if the person id format is invalid
     * @throws InvalidCarPlateException if the car plate format is invalid
     * @throws CameraNotFoundException if no camera with the given name exists
     * @throws UnknownObjectTypeException if the given object type is not known
     * @throws MissingRequiredFieldException if a required field is missing
     */
    public CamReportResponse handleCamReport(CamReportRequest request)
            throws InvalidPersonIdException, InvalidCarPlateException, CameraInvalidArgumentsException,
            CameraNotFoundException, UnknownObjectTypeException, MissingRequiredFieldException {
        // Parse request
        VectorTimestamp prev = new VectorTimestamp(request.getPrev().getTimestampList());
        String name = CheckUtils.notNull(request.getCamName(), CAM_NAME);
        List<ObjectInfo> observations = CheckUtils.notNull(request.getObservationsList(), OBSERVATION_LIST);
        List<Long> peopleIds = new ArrayList<>();
        List<String> carPlates = new ArrayList<>();
        fillIdentifiersLists(observations, peopleIds, carPlates);
        // Execute request
        VectorTimestamp updateID = replicaService.camReport(name, peopleIds, carPlates, prev);
        // Build response
        VectorTS ts = GrpcMessageBuilder.buildVectorTimestamp(updateID.toList());
        return GrpcMessageBuilder.buildCamReportResponse(ts);
    }

    /**
     * Process clear request
     * @param request to process
     * @return response to send if no error has occurred
     */
    public ClearResponse handleClear(ClearRequest request) {
        // Parse request
        VectorTimestamp prev = new VectorTimestamp(request.getPrev().getTimestampList());
        // Execute request
        VectorTimestamp updateID = replicaService.clear(prev);
        // Build response
        VectorTS ts = GrpcMessageBuilder.buildVectorTimestamp(updateID.toList());
        return GrpcMessageBuilder.buildClearResponse(ts);
    }

    /**
     * Process init request
     * @param request to process
     * @return response to send if no error has occurred
     * @throws CameraNotFoundException if no camera with the given name exists
     * @throws CameraInvalidArgumentsException if camera has invalid arguments
     * @throws CameraAlreadyExistsException if a camera already exists
     * @throws InvalidPersonIdException if the person id format is invalid
     * @throws InvalidCarPlateException if the car plate format is invalid
     * @throws MissingRequiredFieldException if a required field is missing
     */
    public InitResponse handleInit(InitRequest request)
            throws CameraNotFoundException, CameraInvalidArgumentsException, CameraAlreadyExistsException,
            InvalidPersonIdException, InvalidCarPlateException, MissingRequiredFieldException {
        // Parse request
        VectorTimestamp prev = new VectorTimestamp(request.getPrev().getTimestampList());
        List<CamInfo> camInfos = CheckUtils.notNull(request.getCamerasList(), CAM_LIST);
        List<ObservationInfo> observationInfos = CheckUtils.notNull(request.getObservationsList(), OBSERVATION_LIST);
        List<Camera> cameras = new ArrayList<>();
        List<Person> people = new ArrayList<>();
        List<Car> cars = new ArrayList<>();
        fillCamerasList(camInfos, cameras);
        fillObservationsList(observationInfos, people, cars);
        // Execute request
        VectorTimestamp updateID = replicaService.init(cameras, people, cars, prev);
        // Build response
        VectorTS ts = GrpcMessageBuilder.buildVectorTimestamp(updateID.toList());
        return GrpcMessageBuilder.buildInitResponse(ts);
    }

    /**
     * Process gossip request
     * @param request to process
     * @return response to send if no error has occurred
     * @throws UnknownObjectTypeException if a given object type is unknown
     * @throws MissingRequiredFieldException if a field is missing
     */
    public GossipResponse handleGossip(GossipRequest request)
            throws MissingRequiredFieldException, UnknownObjectTypeException {
        // Parse request
        VectorTimestamp timestamp = new VectorTimestamp(request.getTs().getTimestampList());
        int replicaInstance = request.getReplicaInstance();
        List<Operation> log = request.getLogList();
        List<Update> updateLog = buildUpdateLog(log);
        // Execute request
        replicaService.gossip(updateLog, timestamp, replicaInstance);
        // Build response
        return GrpcMessageBuilder.buildGossipResponse();
    }

    /* Auxiliary functions to parse requests */

    private List<Update> buildUpdateLog(List<Operation> log)
            throws MissingRequiredFieldException, UnknownObjectTypeException {
        List<Update> updates = new ArrayList<>();
        for (Operation operation : log) {
            Update update = buildUpdate(operation);
            updates.add(update);
        }
        return updates;
    }

    private Update buildUpdate(Operation operation)
            throws MissingRequiredFieldException, UnknownObjectTypeException {
        VectorTimestamp prev = new VectorTimestamp(operation.getPrev().getTimestampList());
        VectorTimestamp timestamp = new VectorTimestamp(operation.getTs().getTimestampList());
        int replicaInstance = operation.getReplicaInstance();
        Update update = null;
        switch (operation.getOperation()) {
            case CAM_JOIN:
                update = buildCamJoinUpdate(operation, replicaInstance, timestamp, prev);
                break;
            case CAM_REPORT:
                update = buildCamReportUpdate(operation, replicaInstance, timestamp, prev);
                break;
            case CLEAR:
                update = new Clear(replicaInstance, timestamp, prev);
                break;
            case INIT:
                return buildInitUpdate(operation, replicaInstance, timestamp, prev);
            case UNKNOWN_OP:
            default:
                break;
        }
        return update;
    }

    private Update buildCamJoinUpdate(Operation operation, int replicaInstance,
                                      VectorTimestamp timestamp, VectorTimestamp prev) {
        CamJoinData camJoinData = operation.getCamJoinData();
        String name = camJoinData.getName();
        Coordinates coordinates = camJoinData.getCoordinates();
        Double latitude = coordinates.getLatitude();
        Double longitude = coordinates.getLongitude();
        return new CamJoin(name, latitude, longitude, replicaInstance, timestamp, prev);
    }

    private Update buildCamReportUpdate(Operation operation, int replicaInstance,
                                        VectorTimestamp timestamp, VectorTimestamp prev)
            throws MissingRequiredFieldException, UnknownObjectTypeException {
        CamReportData camReportData = operation.getCamReportData();
        String name = camReportData.getName();
        List<Observation> observations = camReportData.getObservationsList();
        Timestamp observationTimestamp = observations.get(0).getTimestamp();
        Instant instant = buildInstant(observationTimestamp);
        List<ObjectInfo> objectInfos = observations.stream()
                .map(Observation::getObjectInfo).collect(Collectors.toList());
        List<Long> peopleIds = new ArrayList<>();
        List<String> carPlates = new ArrayList<>();
        fillIdentifiersLists(objectInfos, peopleIds, carPlates);
        return new CamReport(name, peopleIds, carPlates, instant, replicaInstance, timestamp, prev);
    }

    private Update buildInitUpdate(Operation operation, int replicaInstance,
                                   VectorTimestamp timestamp, VectorTimestamp prev)
            throws MissingRequiredFieldException {
        InitData initData = operation.getInitData();
        List<CamInfo> camInfos = initData.getCamerasList();
        List<ObservationInfo> observationInfos = initData.getObservationInfosList();
        List<Camera> cameras = new ArrayList<>();
        List<Person> people = new ArrayList<>();
        List<Car> cars = new ArrayList<>();
        fillCamerasList(camInfos, cameras);
        fillObservationsList(observationInfos, people, cars);
        return new Init(cameras, people, cars, replicaInstance, timestamp, prev);
    }

    private void fillIdentifiersLists(List<ObjectInfo> objectInfos, List<Long> peopleIds, List<String> carPlates)
            throws UnknownObjectTypeException, MissingRequiredFieldException {
        for (ObjectInfo objectInfo : objectInfos) {
            ObjectType objectType = objectInfo.getType();
            switch (objectType) {
                case CAR:
                    String plate = objectInfo.getAlphanumericId();
                    carPlates.add(CheckUtils.notNull(plate, CAR_PLATE));
                    break;
                case PERSON:
                    Long id = objectInfo.getNumericId();
                    peopleIds.add(CheckUtils.notNull(id, PERSON_ID));
                    break;
                case UNKNOWN_TYPE:
                default:
                    throw new UnknownObjectTypeException(objectType);
            }
        }
    }

    private void fillCamerasList(List<CamInfo> camInfos, List<Camera> cameras)
            throws MissingRequiredFieldException {
        for (CamInfo camInfo : camInfos) {
            checkNotNullCamInfo(camInfo);
            cameras.add(buildCamera(camInfo));
        }
    }

    private void fillObservationsList(List<ObservationInfo> observationInfos, List<Person> people, List<Car> cars)
            throws MissingRequiredFieldException {
        for (ObservationInfo observationInfo : observationInfos) {
            checkNotNullObservationInfo(observationInfo);
            switch (observationInfo.getObservation().getObjectInfo().getType()) {
                case PERSON:
                    people.add(buildPeople(observationInfo));
                    break;
                case CAR:
                    cars.add(buildCar(observationInfo));
                    break;
                case UNKNOWN_TYPE:
                default:
                    throw new MissingRequiredFieldException(OBJECT_TYPE);
            }
        }
    }

    private Person buildPeople(ObservationInfo observationInfo) {
        return new Person(observationInfo.getObservation().getObjectInfo().getNumericId(),
                buildInstant(observationInfo.getObservation().getTimestamp()),
                buildCamera(observationInfo.getCamInfo()));
    }

    private Car buildCar(ObservationInfo observationInfo) {
        return new Car(observationInfo.getObservation().getObjectInfo().getAlphanumericId(),
                buildInstant(observationInfo.getObservation().getTimestamp()),
                buildCamera(observationInfo.getCamInfo()));
    }

    private Camera buildCamera(CamInfo camInfo) {
        return new Camera(camInfo.getName(),
                camInfo.getCoordinates().getLatitude(),
                camInfo.getCoordinates().getLongitude());
    }

    private Instant buildInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds());
    }

    /* Auxiliary methods for null checking */

    private void checkNotNullObservationInfo(ObservationInfo observationInfo)
            throws MissingRequiredFieldException {
        CheckUtils.notNull(observationInfo, OBSERVATION_INFO);
        checkNotNullCamInfo(observationInfo.getCamInfo());
        checkNotNullObservation(observationInfo.getObservation());
    }

    private void checkNotNullCamInfo(CamInfo camInfo)
            throws MissingRequiredFieldException {
        CheckUtils.notNull(camInfo, CAM_INFO);
        CheckUtils.notNullOrBlank(camInfo.getName(), CAM_NAME);
        CheckUtils.notNull(camInfo.getCoordinates(), CAM_COORDINATES);
        CheckUtils.notNull(camInfo.getCoordinates().getLatitude(), CAM_LATITUDE);
        CheckUtils.notNull(camInfo.getCoordinates().getLongitude(), CAM_LONGITUDE);
    }

    private void checkNotNullObservation(Observation observation)
            throws MissingRequiredFieldException {
        CheckUtils.notNull(observation, OBSERVATION);
        CheckUtils.notNull(observation.getObjectInfo(), OBJECT_INFO);
        CheckUtils.notNull(observation.getObjectInfo().getType(), OBJECT_TYPE);
        switch (observation.getObjectInfo().getType()) {
            case CAR:
                CheckUtils.notNullOrBlank(observation.getObjectInfo().getAlphanumericId(), PERSON_ID);
                break;
            case PERSON:
                CheckUtils.notNull(observation.getObjectInfo().getNumericId(), CAR_PLATE);
                break;
            case UNKNOWN_TYPE:
            default:
                throw new MissingRequiredFieldException(OBJECT_TYPE);
        }
        CheckUtils.notNull(observation.getTimestamp(), OBSERVATION_TIMESTAMP);
    }
}
