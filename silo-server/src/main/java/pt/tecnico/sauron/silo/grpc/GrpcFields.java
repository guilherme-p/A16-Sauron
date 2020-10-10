package pt.tecnico.sauron.silo.grpc;

/**
 * Class with gRPC fields for easy string manipulation
 * with the fields names and values
 */
public class GrpcFields {

    private GrpcFields() {}

    static final String CAM_INFO = "Camera info";
    static final String CAM_NAME = "Camera name";
    static final String CAM_COORDINATES = "Camera Coordinates";
    static final String CAM_LATITUDE = "Camera Latitude";
    static final String CAM_LONGITUDE = "Camera Longitude";
    static final String CAM_LIST = "Cameras List";

    static final String PERSON_ID = "Person Id";
    static final String CAR_PLATE = "Car Id";

    static final String OBJECT_INFO = "Object Info";
    static final String OBJECT_TYPE = "Object Type";

    static final String OBSERVATION_INFO = "Observation Info";
    static final String OBSERVATION = "Observation";
    static final String OBSERVATION_LIST = "Observations List";
    static final String OBSERVATION_TIMESTAMP = "Observation Timestamp";
}
