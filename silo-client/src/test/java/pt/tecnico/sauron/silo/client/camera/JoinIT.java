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

public class JoinIT extends BaseIT {

    /* Test Context */
    private static final Coordinates[] coordinates = new Coordinates[7];
    private static final CamInfo[] cameras = new CamInfo[13];

    @BeforeAll
    public static void oneTimeSetUp() {

        /* Define all test objects */

        coordinates[0] = RequestBuilder.buildCoordinates(0.0, 0.0);
        coordinates[1] = RequestBuilder.buildCoordinates(-91.0, 0.0);
        coordinates[2] = RequestBuilder.buildCoordinates(91.0, 0.0);
        coordinates[3] = RequestBuilder.buildCoordinates(0.0, -181.0);
        coordinates[4] = RequestBuilder.buildCoordinates(0.0, 181.0);
        coordinates[5] = RequestBuilder.buildCoordinates(91.0, -181.0);
        coordinates[6] = RequestBuilder.buildCoordinates(90.0, -180.0);

        cameras[0] = RequestBuilder.buildCamInfo("Cam1", coordinates[0]);
        cameras[1] = RequestBuilder.buildCamInfo("Cam2", coordinates[1]);
        cameras[2] = RequestBuilder.buildCamInfo("Cam3", coordinates[2]);
        cameras[3] = RequestBuilder.buildCamInfo("Cam4", coordinates[3]);
        cameras[4] = RequestBuilder.buildCamInfo("Cam5", coordinates[4]);
        cameras[5] = RequestBuilder.buildCamInfo("Cam6", coordinates[5]);
        cameras[6] = RequestBuilder.buildCamInfo("Cam7", coordinates[6]);

        cameras[7] = RequestBuilder.buildCamInfo("Cam1", coordinates[6]);

        cameras[8] = RequestBuilder.buildCamInfo("Ca", coordinates[0]);
        cameras[9] = RequestBuilder.buildCamInfo("Cam", coordinates[0]);
        cameras[10] = RequestBuilder.buildCamInfo("Camera100000000", coordinates[0]);
        cameras[11] = RequestBuilder.buildCamInfo("Camera1000000000", coordinates[0]);
        cameras[12] = RequestBuilder.buildCamInfo("Cam_", coordinates[0]);
    }

    @AfterAll
    public static void oneTimeTearDown() { /* Do nothing */ }

    @BeforeEach
    public void setUp() { /* Do nothing*/ }

    @AfterEach
    public void tearDown() {
        /* Clear the state */
        frontend.sendCtrlClear();
    }

    /* Correct Tests */

    @Test
    public void joinCameraTest() {
        assertDoesNotThrow(() -> frontend.sendCamJoin(cameras[0]));
    }

    @Test
    public void joinCameraLengthTest() {
        assertDoesNotThrow(() -> frontend.sendCamJoin(cameras[9]));
    }

    @Test
    public void joinCameraLength15Test() {
        assertDoesNotThrow(() -> frontend.sendCamJoin(cameras[10]));
    }

    @Test
    public void joinCameraLonLatBoundsTest() {
        assertDoesNotThrow(() -> frontend.sendCamJoin(cameras[6]));
    }

    /* Invalid Arguments Tests */

    @Test
    public void joinCameraLength2Test() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamJoin(cameras[8]));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void joinCameraLength16Test() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamJoin(cameras[11]));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void joinCameraNotAlphanumericTest() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamJoin(cameras[11]));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void joinCameraLatMinus91Test() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamJoin(cameras[1]));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void joinCameraLat91Test() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamJoin(cameras[2]));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void joinCameraLonMinus181Test() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamJoin(cameras[3]));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void joinCameraLon181Test() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamJoin(cameras[4]));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void joinCameraLat91LonMinus181Test() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamJoin(cameras[5]));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    /* Already exists test */

    @Test
    public void joinCameraAlreadyExistsTest() {
        List<CamInfo> camInfos = new ArrayList<>();
        camInfos.add(cameras[0]);
        frontend.sendCtrlInit(camInfos, new ArrayList<>());

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamJoin(cameras[7]));
        assertEquals(ALREADY_EXISTS, exception.getStatus().getCode());
    }
}

