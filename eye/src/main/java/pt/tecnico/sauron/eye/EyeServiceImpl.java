package pt.tecnico.sauron.eye;

import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.client.RequestBuilder;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.util.ArrayList;
import java.util.List;

public class EyeServiceImpl {

    private static final String PERSON_TYPE = "person";
    private static final String CAR_TYPE = "car";

    private final SiloFrontend frontend;
    private final String camName;
    private final List<ObjectInfo> observations;

    public EyeServiceImpl(String zooHost, String zooPort, int numReplicas, int cacheSize, String camName) {
        this.frontend = new SiloFrontend(zooHost, zooPort, numReplicas, cacheSize);
        this.camName = camName;
        this.observations = new ArrayList<>();
    }

    public EyeServiceImpl(String zooHost, String zooPort, String serverInstance, int numReplicas, int cacheSize, String camName) {
        this.frontend = new SiloFrontend(zooHost, zooPort, serverInstance, numReplicas, cacheSize);
        this.camName = camName;
        this.observations = new ArrayList<>();
    }

    public CamJoinResponse sendCamJoin(String name, Double latitude, Double longitude) {
        Coordinates coordinates = RequestBuilder.buildCoordinates(latitude, longitude);
        CamInfo camInfo = RequestBuilder.buildCamInfo(name, coordinates);
        return frontend.sendCamJoin(camInfo);
    }

    public CamReportResponse sendCamReport() {
        CamReportResponse response = frontend.sendCamReport(camName, observations);
        observations.clear();
        return response;
    }

    public void addObservation(String type, String alphanumericId) {
        ObjectType objectType = getObjectType(type);
        ObjectInfo objectInfo = getObjectInfo(objectType, alphanumericId);
        observations.add(objectInfo);
    }

    public boolean hasObservations() {
        return !observations.isEmpty();
    }

    public void close() {
        frontend.close();
    }

    private ObjectType getObjectType(String type) {
        ObjectType objectType;
        switch (type) {
            case PERSON_TYPE:
                objectType = RequestBuilder.buildObjectTypePerson();
                break;
            case CAR_TYPE:
                objectType = RequestBuilder.buildObjectTypeCar();
                break;
            default:
                objectType = RequestBuilder.buildObjectTypeUnknown();
                break;
        }
        return objectType;
    }

    private ObjectInfo getObjectInfo(ObjectType objectType, String alphanumericId) {
        ObjectInfo objectInfo;
        try {
            Long numericId = Long.parseLong(alphanumericId);
            objectInfo = RequestBuilder.buildObjectInfo(objectType, numericId);
        } catch (NumberFormatException exception) {
            objectInfo = RequestBuilder.buildObjectInfo(objectType, alphanumericId);
        }
        return objectInfo;
    }

}
