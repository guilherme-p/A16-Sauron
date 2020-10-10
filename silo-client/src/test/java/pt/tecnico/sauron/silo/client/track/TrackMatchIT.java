package pt.tecnico.sauron.silo.client.track;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.client.RequestBuilder;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.Instant;
import java.util.*;

import static io.grpc.Status.Code.INVALID_ARGUMENT;

public class TrackMatchIT extends BaseIT {

    private static final Comparator<ObservationInfo> CAR_OBSERVATION_INFO_COMPARATOR =
            (c1, c2) -> c2.getObservation().getObjectInfo().getAlphanumericId()
                    .compareTo(c1.getObservation().getObjectInfo().getAlphanumericId());

    private static final Comparator<ObservationInfo> PERSON_OBSERVATION_INFO_COMPARATOR =
            (p1, p2) -> (int) (p2.getObservation().getObjectInfo().getNumericId()
                    - p1.getObservation().getObjectInfo().getNumericId());

    private static final CamInfo[] cameras = new CamInfo[1];
    private static final ObjectInfo[] cars = new ObjectInfo[4];
    private static final ObjectInfo[] people = new ObjectInfo[4];

    private static final Timestamp[] carsTimestamps = new Timestamp[4];
    private static final Timestamp[] peopleTimestamps = new Timestamp[4];

    private static final Observation[] carsObservations = new Observation[4];
    private static final Observation[] peopleObservations = new Observation[4];

    private static final ObservationInfo[] carsObservationInfos = new ObservationInfo[4];
    private static final ObservationInfo[] peopleObservationInfos = new ObservationInfo[4];

    private List<ObservationInfo> observationInfos;

    @BeforeAll
    public static void  oneTimeSetUp() {
        Coordinates coordinates = RequestBuilder.buildCoordinates(1.0, 1.0);
        cameras[0] = RequestBuilder.buildCamInfo("Cam1", coordinates);

        /* build cars */
        cars[0] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "AA00BB");
        carsTimestamps[0] = RequestBuilder.buildTimeStamp(Instant.now());
        carsObservations[0] = RequestBuilder.buildObservation(cars[0], carsTimestamps[0]);

        cars[1] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "AA11CC");
        carsTimestamps[1] = RequestBuilder.buildTimeStamp(Instant.now());
        carsObservations[1] = RequestBuilder.buildObservation(cars[1], carsTimestamps[1]);

        cars[2] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "DD00CC");
        carsTimestamps[2] = RequestBuilder.buildTimeStamp(Instant.now());
        carsObservations[2] = RequestBuilder.buildObservation(cars[2], carsTimestamps[2]);

        cars[3] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "AA12CC");
        carsTimestamps[3] = RequestBuilder.buildTimeStamp(Instant.now());
        carsObservations[3] = RequestBuilder.buildObservation(cars[3], carsTimestamps[3]);

        carsObservationInfos[0] = RequestBuilder.buildObservationInfo(carsObservations[0], cameras[0]);
        carsObservationInfos[1] = RequestBuilder.buildObservationInfo(carsObservations[1], cameras[0]);
        carsObservationInfos[2] = RequestBuilder.buildObservationInfo(carsObservations[2], cameras[0]);
        carsObservationInfos[3] = RequestBuilder.buildObservationInfo(carsObservations[3], cameras[0]);

        /* build people */
        people[0] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 112233L);
        peopleTimestamps[0] = RequestBuilder.buildTimeStamp(Instant.now());
        peopleObservations[0] = RequestBuilder.buildObservation(people[0], peopleTimestamps[0]);

        people[1] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 114455L);
        peopleTimestamps[1] = RequestBuilder.buildTimeStamp(Instant.now());
        peopleObservations[1] = RequestBuilder.buildObservation(people[1], peopleTimestamps[1]);

        people[2] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 662255L);
        peopleTimestamps[2] = RequestBuilder.buildTimeStamp(Instant.now());
        peopleObservations[2] = RequestBuilder.buildObservation(people[2], peopleTimestamps[2]);

        people[3] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 114655L);
        peopleTimestamps[3] = RequestBuilder.buildTimeStamp(Instant.now());
        peopleObservations[3] = RequestBuilder.buildObservation(people[3], peopleTimestamps[3]);

        peopleObservationInfos[0] = RequestBuilder.buildObservationInfo(peopleObservations[0], cameras[0]);
        peopleObservationInfos[1] = RequestBuilder.buildObservationInfo(peopleObservations[1], cameras[0]);
        peopleObservationInfos[2] = RequestBuilder.buildObservationInfo(peopleObservations[2], cameras[0]);
        peopleObservationInfos[3] = RequestBuilder.buildObservationInfo(peopleObservations[3], cameras[0]);

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
    public void trackMatchOneCarStartRegexTest() {
        observationInfos.add(carsObservationInfos[0]);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.CAR, "AA00*");
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void trackMatchOneCarMiddleRegexTest() {
        observationInfos.add(carsObservationInfos[0]);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.CAR, "*A00B*");
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void trackMatchOneCarEndRegexTest() {
        observationInfos.add(carsObservationInfos[0]);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.CAR, "*00BB");
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void trackMatchOneCarSeveralPartsRegexTest() {
        observationInfos.add(carsObservationInfos[0]);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.CAR, "*A*0*B*");
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void trackMatchAllCarsTest() {
        observationInfos.addAll(Arrays.asList(carsObservationInfos));
        observationInfos.sort(CAR_OBSERVATION_INFO_COMPARATOR);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.CAR, "*");
        List<ObservationInfo> responseObservations = new ArrayList<>(response.getObservationInfosList());
        responseObservations.sort(CAR_OBSERVATION_INFO_COMPARATOR);
        Assertions.assertIterableEquals(responseObservations, observationInfos);
    }

    @Test
    public void trackMatchMultipleCarsStartRegexTest() {
        observationInfos.add(carsObservationInfos[0]);
        observationInfos.add(carsObservationInfos[1]);
        observationInfos.add(carsObservationInfos[3]);
        observationInfos.sort(CAR_OBSERVATION_INFO_COMPARATOR);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.CAR, "AA*");
        List<ObservationInfo> responseObservations = new ArrayList<>(response.getObservationInfosList());
        responseObservations.sort(CAR_OBSERVATION_INFO_COMPARATOR);
        Assertions.assertIterableEquals(responseObservations, observationInfos);
    }

    @Test
    public void trackMatchMultipleCarsMiddleRegexTest() {
        observationInfos.add(carsObservationInfos[0]);
        observationInfos.add(carsObservationInfos[2]);
        observationInfos.sort(CAR_OBSERVATION_INFO_COMPARATOR);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.CAR, "*00*");
        List<ObservationInfo> responseObservations = new ArrayList<>(response.getObservationInfosList());
        responseObservations.sort(CAR_OBSERVATION_INFO_COMPARATOR);
        Assertions.assertIterableEquals(responseObservations, observationInfos);
    }

    @Test
    public void trackMatchMultipleCarsEndRegexTest() {
        observationInfos.add(carsObservationInfos[1]);
        observationInfos.add(carsObservationInfos[2]);
        observationInfos.add(carsObservationInfos[3]);
        observationInfos.sort(CAR_OBSERVATION_INFO_COMPARATOR);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.CAR, "*CC");
        List<ObservationInfo> responseObservations = new ArrayList<>(response.getObservationInfosList());
        responseObservations.sort(CAR_OBSERVATION_INFO_COMPARATOR);
        Assertions.assertIterableEquals(responseObservations, observationInfos);
    }

    @Test
    public void trackMatchMultipleCarsSeveralPartsRegexTest() {
        observationInfos.add(carsObservationInfos[1]);
        observationInfos.add(carsObservationInfos[3]);
        observationInfos.sort(CAR_OBSERVATION_INFO_COMPARATOR);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.CAR, "*A*1*C*");
        List<ObservationInfo> responseObservations = new ArrayList<>(response.getObservationInfosList());
        responseObservations.sort(CAR_OBSERVATION_INFO_COMPARATOR);
        Assertions.assertIterableEquals(responseObservations, observationInfos);
    }

    @Test
    public void trackMatchOnePersonStartRegexTest() {
        observationInfos.add(peopleObservationInfos[0]);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.PERSON, "1122*");
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void trackMatchOnePersonMiddleRegexTest() {
        observationInfos.add(peopleObservationInfos[0]);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.PERSON, "*1223*");
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void trackMatchOnePersonEndRegexTest() {
        observationInfos.add(peopleObservationInfos[0]);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.PERSON, "*2233");
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void trackMatchOnePersonSeveralPartsRegexTest() {
        observationInfos.add(peopleObservationInfos[0]);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.PERSON, "*1*2*3*");
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void trackMatchAllPeopleTest() {
        observationInfos.addAll(Arrays.asList(peopleObservationInfos));
        observationInfos.sort(PERSON_OBSERVATION_INFO_COMPARATOR);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.PERSON, "*");
        List<ObservationInfo> responseObservations = new ArrayList<>(response.getObservationInfosList());
        responseObservations.sort(PERSON_OBSERVATION_INFO_COMPARATOR);
        Assertions.assertIterableEquals(responseObservations, observationInfos);
    }

    @Test
    public void trackMatchMultiplePeopleStartRegexTest() {
        observationInfos.add(peopleObservationInfos[0]);
        observationInfos.add(peopleObservationInfos[1]);
        observationInfos.add(peopleObservationInfos[3]);
        observationInfos.sort(PERSON_OBSERVATION_INFO_COMPARATOR);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.PERSON, "11*");
        List<ObservationInfo> responseObservations = new ArrayList<>(response.getObservationInfosList());
        responseObservations.sort(PERSON_OBSERVATION_INFO_COMPARATOR);
        Assertions.assertIterableEquals(responseObservations, observationInfos);
    }

    @Test
    public void trackMatchMultiplePeopleMiddleRegexTest() {
        observationInfos.add(peopleObservationInfos[0]);
        observationInfos.add(peopleObservationInfos[2]);
        observationInfos.sort(PERSON_OBSERVATION_INFO_COMPARATOR);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.PERSON, "*22*");
        List<ObservationInfo> responseObservations = new ArrayList<>(response.getObservationInfosList());
        responseObservations.sort(PERSON_OBSERVATION_INFO_COMPARATOR);
        Assertions.assertIterableEquals(responseObservations, observationInfos);
    }

    @Test
    public void trackMatchMultiplePeopleEndRegexTest() {
        observationInfos.add(peopleObservationInfos[1]);
        observationInfos.add(peopleObservationInfos[2]);
        observationInfos.add(peopleObservationInfos[3]);
        observationInfos.sort(PERSON_OBSERVATION_INFO_COMPARATOR);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.PERSON, "*55");
        List<ObservationInfo> responseObservations = new ArrayList<>(response.getObservationInfosList());
        responseObservations.sort(PERSON_OBSERVATION_INFO_COMPARATOR);
        Assertions.assertIterableEquals(responseObservations, observationInfos);
    }

    @Test
    public void trackMatchMultiplePeopleSeveralPartsRegexTest() {
        observationInfos.add(peopleObservationInfos[1]);
        observationInfos.add(peopleObservationInfos[3]);
        observationInfos.sort(PERSON_OBSERVATION_INFO_COMPARATOR);
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.PERSON, "*1*4*5*");
        List<ObservationInfo> responseObservations = new ArrayList<>(response.getObservationInfosList());
        responseObservations.sort(PERSON_OBSERVATION_INFO_COMPARATOR);
        Assertions.assertIterableEquals(responseObservations, observationInfos);
    }

    /* No observations found tests */

    @Test
    public void trackMatchNoCarsFoundTest() {
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.CAR, "ZZ*");
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    @Test
    public void trackMatchNoPeopleFoundTest() {
        SpotterTrackMatchResponse response = frontend.sendSpotterTrackMatch(ObjectType.PERSON, "99*");
        Assertions.assertIterableEquals(response.getObservationInfosList(), observationInfos);
    }

    /* Invalid arguments tests */

    @Test
    public void trackMatchCarEmptyRegexTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrackMatch(ObjectType.CAR, ""));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void trackMatchPersonEmptyRegexTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrackMatch(ObjectType.PERSON, ""));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void trackMatchCarInvalidRegexTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrackMatch(ObjectType.CAR, "XXYYWWZZ*"));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void trackMatchPersonInvalidRegexTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrackMatch(ObjectType.PERSON, "ABC*"));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void trackMatchUnknownCarRegexTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrackMatch(ObjectType.UNKNOWN_TYPE, "AA00*"));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void trackMatchUnknownPersonRegexTest() {
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> frontend.sendSpotterTrackMatch(ObjectType.UNKNOWN_TYPE, "12345*"));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

}
