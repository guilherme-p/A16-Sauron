package pt.tecnico.sauron.silo.client.control;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.client.RequestBuilder;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClearIT extends BaseIT {

    /* Test Context */
    private static final CamInfo[] cameras = new CamInfo[2];
    private static final ObjectInfo[] cars = new ObjectInfo[3];
    private static final ObjectInfo[] people = new ObjectInfo[1];

    private static final Observation[] observations = new Observation[4];

    private static final ObservationInfo[] observationInfos = new ObservationInfo[4];

    @BeforeAll
    public static void oneTimeSetUp() {

        /* Define all test objects */

        Coordinates coordinates = RequestBuilder.buildCoordinates(0.0, 0.0);

        cameras[0] = RequestBuilder.buildCamInfo("Cam1", coordinates);
        cameras[1] = RequestBuilder.buildCamInfo("Cam2", coordinates);

        cars[0] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "00AA00");
        cars[1] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "0000AA");
        cars[2] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "AA0000");

        people[0] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 1L);

        Timestamp timestamp = RequestBuilder.buildTimeStamp(Instant.now());

        observations[0] = RequestBuilder.buildObservation(cars[0], timestamp);
        observations[1] = RequestBuilder.buildObservation(cars[1], timestamp);
        observations[2] = RequestBuilder.buildObservation(cars[2], timestamp);
        observations[3] = RequestBuilder.buildObservation(people[0], timestamp);

        observationInfos[0] = RequestBuilder.buildObservationInfo(observations[0], cameras[0]);
        observationInfos[1] = RequestBuilder.buildObservationInfo(observations[1], cameras[0]);
        observationInfos[2] = RequestBuilder.buildObservationInfo(observations[2], cameras[0]);
        observationInfos[3] = RequestBuilder.buildObservationInfo(observations[3], cameras[0]);
    }

    @BeforeEach
    public void setUp() {
        List<CamInfo> camInfos = new ArrayList<>(Arrays.asList(cameras));
        List<ObservationInfo> observationInfosList = new ArrayList<>(Arrays.asList(observationInfos));
        frontend.sendCtrlInit(camInfos, observationInfosList);
    }

    @Test
    public void clearOkTest() {
        Assertions.assertDoesNotThrow(() -> frontend.sendCtrlClear());
    }
}
