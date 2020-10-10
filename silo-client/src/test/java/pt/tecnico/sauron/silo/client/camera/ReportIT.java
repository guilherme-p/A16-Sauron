package pt.tecnico.sauron.silo.client.camera;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.client.RequestBuilder;
import pt.tecnico.sauron.silo.grpc.*;

import java.util.ArrayList;
import java.util.List;

import static io.grpc.Status.Code.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReportIT extends BaseIT {

    /* Test Context */
    private static final CamInfo[] cameras = new CamInfo[7];
    private static final ObjectInfo[] cars = new ObjectInfo[8];
    private static final ObjectInfo[] people = new ObjectInfo[3];
    private static final ObjectInfo[] unknown = new ObjectInfo[4];

    @BeforeAll
    public static void oneTimeSetUp() {

        /* Define all test objects */

        Coordinates coordinates = RequestBuilder.buildCoordinates(0.0, 0.0);

        cameras[0] = RequestBuilder.buildCamInfo("Cam1", coordinates);
        cameras[1] = RequestBuilder.buildCamInfo("Cam2", coordinates);

        cars[0] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "00AA00");
        cars[1] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "0000AA");
        cars[2] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "AA0000");
        cars[3] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "");
        cars[4] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "00AA0");
        cars[5] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "00AA000");
        cars[6] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "00aa00");
        cars[7] = RequestBuilder.buildObjectInfo(ObjectType.CAR, "00 AA 00");

        people[0] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 1L);
        people[1] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, 2L);
        people[2] = RequestBuilder.buildObjectInfo(ObjectType.PERSON, -1L);

        unknown[0] = RequestBuilder.buildObjectInfo(ObjectType.UNKNOWN_TYPE, 1L);
        unknown[1] = RequestBuilder.buildObjectInfo(ObjectType.UNKNOWN_TYPE, "00AA00");
    }

    @AfterAll
    public static void oneTimeTearDown() { /* Do nothing */ }

    @BeforeEach
    public void setUp() {
        List<CamInfo> camInfos = new ArrayList<>();
        camInfos.add(cameras[0]);
        frontend.sendCtrlInit(camInfos, new ArrayList<>());
    }

    @AfterEach
    public void tearDown() {
        /* Clear the state */
        frontend.sendCtrlClear();
    }

    /* Correct Tests */
    @Test
    public void reportPersonTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(people[0]);
        Assertions.assertDoesNotThrow(() -> frontend.sendCamReport(cameras[0].getName(), observations));
    }

    @Test
    public void reportMultiplePeopleTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(people[0]);
        observations.add(people[1]);
        Assertions.assertDoesNotThrow(() -> frontend.sendCamReport(cameras[0].getName(), observations));
    }

    @Test
    public void reportSamePersonMultipleTimesTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(people[0]);
        observations.add(people[0]);
        // Not an error since the same person can be recorded multiple times
        Assertions.assertDoesNotThrow(() -> frontend.sendCamReport(cameras[0].getName(), observations));
    }

    @Test
    public void reportCarNNLLNNTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(cars[0]);
        Assertions.assertDoesNotThrow(() -> frontend.sendCamReport(cameras[0].getName(), observations));
    }

    @Test
    public void reportCarNNNNLLTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(cars[1]);
        Assertions.assertDoesNotThrow(() -> frontend.sendCamReport(cameras[0].getName(), observations));
    }

    @Test
    public void reportCarLLNNNNTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(cars[2]);
        Assertions.assertDoesNotThrow(() -> frontend.sendCamReport(cameras[0].getName(), observations));
    }

    @Test
    public void reportMultipleCarsTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(cars[0]);
        observations.add(cars[1]);
        observations.add(cars[2]);
        Assertions.assertDoesNotThrow(() -> frontend.sendCamReport(cameras[0].getName(), observations));
    }

    @Test
    public void reportSameCarMultipleTimesTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(cars[0]);
        observations.add(cars[0]);
        assertDoesNotThrow(() -> frontend.sendCamReport(cameras[0].getName(), observations));
    }

    /* Invalid Arguments Tests */

    @Test
    public void reportCamNotFoundTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(people[0]);
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamReport(cameras[1].getName(), observations));
        assertEquals(NOT_FOUND, exception.getStatus().getCode());
    }

    @Test
    public void reportPersonNegativeIdTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(people[2]);
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamReport(cameras[0].getName(), observations));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void reportCarNoPlateTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(cars[3]);
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamReport(cameras[0].getName(), observations));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void reportCarPlateMissingCharTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(cars[4]);
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamReport(cameras[0].getName(), observations));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void reportCarPlateExtraCharTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(cars[5]);
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamReport(cameras[0].getName(), observations));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void reportCarPlateLowerCaseTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(cars[6]);
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamReport(cameras[0].getName(), observations));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void reportCarPlateWithSpacesTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(cars[7]);
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamReport(cameras[0].getName(), observations));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void reportUnknownTypeWithPersonIdTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(unknown[0]);
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamReport(cameras[0].getName(), observations));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void reportUnknownTypeWithCarPlateTest() {
        List<ObjectInfo> observations = new ArrayList<>();
        observations.add(unknown[1]);
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamReport(cameras[0].getName(), observations));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }
}
