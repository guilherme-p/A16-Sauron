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
import java.util.Comparator;
import java.util.List;

import static io.grpc.Status.Code.INVALID_ARGUMENT;

public class TraceIT extends BaseIT {

    private static final Comparator<ObservationInfo> OBSERVATION_INFO_COMPARATOR =
            (o1, o2) -> (int) (o2.getObservation().getTimestamp().getSeconds()
                        - o1.getObservation().getTimestamp().getSeconds());

    private static final CamInfo[] cameras = new CamInfo[1];
    private static final ObjectInfo[] cars = new ObjectInfo[4];
    private static final ObjectInfo[] people = new ObjectInfo[4];
    private static final ObjectInfo[] unknown = new ObjectInfo[4];

    private static final Timestamp[] carsTimestamps = new Timestamp[4];
    private static final Timestamp[] peopleTimestamps = new Timestamp[4];

    private static final Observation[] carsObservations = new Observation[4];
    private static final Observation[] peopleObservations = new Observation[4];

    private static final ObservationInfo[] carsObservationInfos = new ObservationInfo[3];
    private static final ObservationInfo[] peopleObservationInfos = new ObservationInfo[3];

    private List<ObservationInfo> observationInfos;

    @BeforeAll
    public static void oneTimeSetUp() {
        Coordinates coordinates = RequestBuilder.buildCoordinates(1.0, 1.0);
        cameras[0] = RequestBuilder.buildCamInfo("Cam1", coordinates);

        /* build cars */
        cars[0] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "AA0000");
        carsTimestamps[0] = RequestBuilder.buildTimeStamp(Instant.now());
        carsObservations[0] = RequestBuilder.buildObservation(cars[0], carsTimestamps[0]);

        cars[1] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "00AA00");
        carsTimestamps[1] = RequestBuilder.buildTimeStamp(Instant.now());
        carsObservations[1] = RequestBuilder.buildObservation(cars[1], carsTimestamps[1]);

        carsTimestamps[2] = RequestBuilder.buildTimeStamp(Instant.now().plusSeconds(1));
        carsObservations[2] = RequestBuilder.buildObservation(cars[1], carsTimestamps[2]);

        // not found
        cars[2] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "0000AA");
        // invalid plate
        cars[3] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "000000");

        carsObservationInfos[0] = RequestBuilder.buildObservationInfo(carsObservations[0], cameras[0]);
        carsObservationInfos[1] = RequestBuilder.buildObservationInfo(carsObservations[1], cameras[0]);
        carsObservationInfos[2] = RequestBuilder.buildObservationInfo(carsObservations[2], cameras[0]);

        /* build people */
        people[0] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 1L);
        peopleTimestamps[0] = RequestBuilder.buildTimeStamp(Instant.now());
        peopleObservations[0] = RequestBuilder.buildObservation(people[0], peopleTimestamps[0]);

        people[1] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 2L);
        peopleTimestamps[1] = RequestBuilder.buildTimeStamp(Instant.now());
        peopleObservations[1] = RequestBuilder.buildObservation(people[1], peopleTimestamps[1]);

        peopleTimestamps[2] = RequestBuilder.buildTimeStamp(Instant.now().plusSeconds(1));
        peopleObservations[2] = RequestBuilder.buildObservation(people[1], peopleTimestamps[2]);

        // not found
        people[2] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 99L);
        // invalid id
        people[3] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, -1L);

        peopleObservationInfos[0] = RequestBuilder.buildObservationInfo(peopleObservations[0], cameras[0]);
        peopleObservationInfos[1] = RequestBuilder.buildObservationInfo(peopleObservations[1], cameras[0]);
        peopleObservationInfos[2] = RequestBuilder.buildObservationInfo(peopleObservations[2], cameras[0]);

        /* build unknown */
        unknown[0] = RequestBuilder.buildObjectInfo(ObjectType.UNKNOWN_TYPE, "0000AA");
        unknown[1] = RequestBuilder.buildObjectInfo(ObjectType.UNKNOWN_TYPE, 1L);
    }

    @AfterAll
    public static void oneTimeTearDown() {}

    @BeforeEach
    public void setUp() {
        observationInfos = new ArrayList<>();
        List<CamInfo> camInfoList = new ArrayList<>(Arrays.asList(cameras));
        List<ObservationInfo> observationInfoList = new ArrayList<>();
        observationInfoList.addAll(Arrays.asList(carsObservationInfos));
        observationInfoList.addAll(Arrays.asList(peopleObservationInfos));
        frontend.sendCtrlInit(camInfoList, observationInfoList);
    }

    @AfterEach
    public void tearDown() {
        observationInfos.clear();
        frontend.sendCtrlClear();
    }

    /* Correct tests */

    @Test
    public void traceCarOneObservationTest() {
        observationInfos.add(carsObservationInfos[0]);
        SpotterTraceResponse response = frontend.sendSpotterTrace(cars[0]);
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void traceCarMultipleObservationsTest() {
        observationInfos.add(carsObservationInfos[1]);
        observationInfos.add(carsObservationInfos[2]);
        observationInfos.sort(OBSERVATION_INFO_COMPARATOR);
        SpotterTraceResponse response = frontend.sendSpotterTrace(cars[1]);
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void tracePersonOneObservationTest() {
        observationInfos.add(peopleObservationInfos[0]);
        SpotterTraceResponse response = frontend.sendSpotterTrace(people[0]);
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void tracePersonMultipleObservationsTest() {
        observationInfos.add(peopleObservationInfos[1]);
        observationInfos.add(peopleObservationInfos[2]);
        observationInfos.sort(OBSERVATION_INFO_COMPARATOR);
        SpotterTraceResponse response = frontend.sendSpotterTrace(people[1]);
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    /* No observations found tests */

    @Test
    public void traceCarNoObservationsFoundTest() {
        SpotterTraceResponse response = frontend.sendSpotterTrace(cars[2]);
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void tracePersonNoObservationsFoundTest() {
        SpotterTraceResponse response = frontend.sendSpotterTrace(people[2]);
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    /* Invalid arguments tests */

    @Test
    public void traceCarInvalidPlateTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrace(cars[3]));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void tracePersonInvalidIdTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrace(people[3]));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void traceUnknownCarPlateTest() {
        // Frontend does not allow unknown object type
        Assertions.assertThrows(IllegalStateException.class, () -> frontend.sendSpotterTrace(unknown[0]));
    }

    @Test
    public void traceUnknownPersonIdTest() {
        // Frontend does not allow unknown object type
        Assertions.assertThrows(IllegalStateException.class, () -> frontend.sendSpotterTrace(unknown[1]));
    }


}
