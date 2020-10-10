package pt.tecnico.sauron.silo.client.track;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.client.RequestBuilder;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.grpc.Status.Code.*;

public class TrackIT extends BaseIT {

    private static final CamInfo[] cameras = new CamInfo[1];
    private static final ObjectInfo[] cars = new ObjectInfo[4];
    private static final ObjectInfo[] people = new ObjectInfo[4];
    private static final ObjectInfo[] unknown = new ObjectInfo[4];

    private static final Timestamp[] carsTimestamps = new Timestamp[4];
    private static final Timestamp[] peopleTimestamps = new Timestamp[4];

    private static final Observation[] carsObservations = new Observation[4];
    private static final Observation[] peopleObservations = new Observation[4];

    private static final ObservationInfo[] carsObservationInfos = new ObservationInfo[2];
    private static final ObservationInfo[] peopleObservationInfos = new ObservationInfo[2];

    @BeforeAll
    public static void oneTimeSetUp() {
        Coordinates coordinates = RequestBuilder.buildCoordinates(1.0, 1.0);
        cameras[0] = RequestBuilder.buildCamInfo("Cam1", coordinates);

        /* build cars */
        // correct
        cars[0] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "AA0000");
        carsTimestamps[0] = RequestBuilder.buildTimeStamp(Instant.now());
        carsObservations[0] = RequestBuilder.buildObservation(cars[0], carsTimestamps[0]);

        // most recent observation of same car
        carsTimestamps[1] = RequestBuilder.buildTimeStamp(Instant.now().plusSeconds(1));
        carsObservations[1] = RequestBuilder.buildObservation(cars[0], carsTimestamps[1]);

        // not found
        cars[1] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "00AA00");
        // invalid plate
        cars[2] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "000000");

        // cars in repository
        carsObservationInfos[0] = RequestBuilder.buildObservationInfo(carsObservations[0], cameras[0]);
        carsObservationInfos[1] = RequestBuilder.buildObservationInfo(carsObservations[1], cameras[0]);

        /* build people */
        people[0] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 1L);
        peopleTimestamps[0] = RequestBuilder.buildTimeStamp(Instant.now());
        peopleObservations[0] = RequestBuilder.buildObservation(people[0], peopleTimestamps[0]);

        // most recent observation of same person
        peopleTimestamps[1] = RequestBuilder.buildTimeStamp(Instant.now().plusSeconds(1));
        peopleObservations[1] = RequestBuilder.buildObservation(people[0], peopleTimestamps[1]);

        // not found
        people[1] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 99L);

        // invalid id
        people[2] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, -1L);

        // people in repository
        peopleObservationInfos[0] = RequestBuilder.buildObservationInfo(peopleObservations[0], cameras[0]);
        peopleObservationInfos[1] = RequestBuilder.buildObservationInfo(peopleObservations[1], cameras[0]);

        /* build unknown */
        unknown[0] = RequestBuilder.buildObjectInfo(ObjectType.UNKNOWN_TYPE, "0000AA");
        unknown[1] = RequestBuilder.buildObjectInfo(ObjectType.UNKNOWN_TYPE, 1L);

    }

    @AfterAll
    public static void oneTimeTearDown() {
    }

    @BeforeEach
    public void setUp() {
        List<CamInfo> camInfoList = new ArrayList<>(Arrays.asList(cameras));
        List<ObservationInfo> observationInfoList = new ArrayList<>();
        observationInfoList.addAll(Arrays.asList(carsObservationInfos));
        observationInfoList.addAll(Arrays.asList(peopleObservationInfos));
        frontend.sendCtrlInit(camInfoList, observationInfoList);
    }

    @AfterEach
    public void tearDown() {
        frontend.sendCtrlClear();
    }

    /* Correct tests */

    @Test
    public void trackCarTest() {
        SpotterTrackResponse response = frontend.sendSpotterTrack(cars[0]);
        Assertions.assertEquals(response.getObservationInfo(), carsObservationInfos[1]);
    }

    @Test
    public void trackPersonTest() {
        SpotterTrackResponse response = frontend.sendSpotterTrack(people[0]);
        Assertions.assertEquals(response.getObservationInfo(), peopleObservationInfos[1]);
    }

    /* Failure tests */

    @Test
    public void trackCarNotMostRecentObservationTest() {
        SpotterTrackResponse response = frontend.sendSpotterTrack(cars[0]);
        Assertions.assertNotEquals(response.getObservationInfo(), carsObservationInfos[0]);
    }

    @Test
    public void trackPersonNotMostRecentObservationTest() {
        SpotterTrackResponse response = frontend.sendSpotterTrack(people[0]);
        Assertions.assertNotEquals(response.getObservationInfo(), peopleObservationInfos[0]);
    }

    @Test
    public void trackCarNotFoundTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrack(cars[1]));
        Assertions.assertEquals(NOT_FOUND, exception.getStatus().getCode());
    }

    @Test
    public void trackPersonNotFoundTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrack(people[1]));
        Assertions.assertEquals(NOT_FOUND, exception.getStatus().getCode());
    }

    /* Invalid arguments tests */

    @Test
    public void trackCarInvalidPlateTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrack(cars[2]));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void trackPersonInvalidIdTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrack(people[2]));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void trackUnknownTypeCarPlateTest() {
        // Frontend does not allow unknown object type
        Assertions.assertThrows(IllegalStateException.class, () -> frontend.sendSpotterTrack(unknown[0]));
    }

    @Test
    public void trackUnknownTypePersonIdTest() {
        // Frontend does not allow unknown object type
        Assertions.assertThrows(IllegalStateException.class, () -> frontend.sendSpotterTrack(unknown[1]));
    }

}
