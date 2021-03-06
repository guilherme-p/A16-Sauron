package pt.tecnico.sauron.silo.client.control;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.client.RequestBuilder;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.grpc.Status.Code.*;

public class InitIT extends BaseIT {

    private static final CamInfo[] cameras = new CamInfo[1];
    private static final ObjectInfo[] cars = new ObjectInfo[3];
    private static final ObjectInfo[] people = new ObjectInfo[3];
    private static final ObjectInfo[] unknown = new ObjectInfo[2];

    private static final Timestamp[] carsTimestamps = new Timestamp[2];
    private static final Timestamp[] peopleTimestamps = new Timestamp[2];
    private static final Timestamp[] unknownTimestamps = new Timestamp[1];

    private static final Observation[] carsObservations = new Observation[2];
    private static final Observation[] peopleObservations = new Observation[2];
    private static final Observation[] unknownObservations = new Observation[2];

    private static final ObservationInfo[] carsObservationInfos = new ObservationInfo[2];
    private static final ObservationInfo[] peopleObservationInfos = new ObservationInfo[2];
    private static final ObservationInfo[] unknownObservationInfos = new ObservationInfo[2];

    @BeforeAll
    public static void oneTimeSetUp() {

        /* Define all tests objects */

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
        unknownTimestamps[0] = RequestBuilder.buildTimeStamp(Instant.now());

        unknownObservations[0] = RequestBuilder.buildObservation(unknown[0], unknownTimestamps[0]);
        unknownObservations[1] = RequestBuilder.buildObservation(unknown[1], unknownTimestamps[0]);

        unknownObservationInfos[0] = RequestBuilder.buildObservationInfo(unknownObservations[0], cameras[0]);
        unknownObservationInfos[1] = RequestBuilder.buildObservationInfo(unknownObservations[1], cameras[0]);
    }

    @AfterAll
    public static void oneTimeTearDown() {
    }

    /* Correct Tests */

    @Test
    public void initOkTest() {
        List<CamInfo> camInfoList = new ArrayList<>(Arrays.asList(cameras));
        List<ObservationInfo> observationInfoList = new ArrayList<>();
        observationInfoList.addAll(Arrays.asList(carsObservationInfos));
        observationInfoList.addAll(Arrays.asList(peopleObservationInfos));
        Assertions.assertDoesNotThrow(() -> frontend.sendCtrlInit(camInfoList, observationInfoList));
    }

    /* Failure Tests */

    @Test
    public void initNoCamerasTest() {
        List<ObservationInfo> observationInfoList = new ArrayList<>();
        observationInfoList.addAll(Arrays.asList(carsObservationInfos));
        observationInfoList.addAll(Arrays.asList(peopleObservationInfos));
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class,
                () -> frontend.sendCtrlInit(new ArrayList<>(), observationInfoList));
        Assertions.assertEquals(NOT_FOUND, exception.getStatus().getCode());
    }

    @Test
    public void initUnknownTypeWithPersonIdTest() {
        List<CamInfo> camInfoList = new ArrayList<>(Arrays.asList(cameras));
        List<ObservationInfo> observationInfoList = new ArrayList<>();
        observationInfoList.add(unknownObservationInfos[0]);
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class,
                () -> frontend.sendCtrlInit(camInfoList, observationInfoList));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void initUnknownTypeWithCarPlateTest() {
        List<CamInfo> camInfoList = new ArrayList<>(Arrays.asList(cameras));
        List<ObservationInfo> observationInfoList = new ArrayList<>();
        observationInfoList.add(unknownObservationInfos[1]);
        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class,
                () -> frontend.sendCtrlInit(camInfoList, observationInfoList));
        Assertions.assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }
}
