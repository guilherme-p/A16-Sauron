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

public class InfoIT extends BaseIT {

    /* Test Context */
    private static final Coordinates[] coordinates = new Coordinates[1];
    private static final CamInfo[] cameras = new CamInfo[6];

    @BeforeAll
    public static void oneTimeSetUp() {

        /* Define all test objects */

        coordinates[0] = RequestBuilder.buildCoordinates(0.0, 0.0);

        cameras[0] = RequestBuilder.buildCamInfo("Cam1", coordinates[0]);
        cameras[1] = RequestBuilder.buildCamInfo("Cam", coordinates[0]);
        cameras[2] = RequestBuilder.buildCamInfo("Cam100000000000", coordinates[0]);
        cameras[3] = RequestBuilder.buildCamInfo("Ca", coordinates[0]);
        cameras[4] = RequestBuilder.buildCamInfo("Cam1000000000000", coordinates[0]);
        cameras[5] = RequestBuilder.buildCamInfo("Cam2", coordinates[0]);

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
    public void getInfoTest() {
        List<CamInfo> camInfos = new ArrayList<>();
        camInfos.add(cameras[0]);
        frontend.sendCtrlInit(camInfos, new ArrayList<>());

        CamInfoResponse response = assertDoesNotThrow(() -> (frontend.sendCamInfo(cameras[0].getName())));
        assertEquals(cameras[0].getCoordinates(), response.getCoordinates());
    }

    @Test
    public void getInfoLength3Test() {
        List<CamInfo> camInfos = new ArrayList<>();
        camInfos.add(cameras[1]);
        frontend.sendCtrlInit(camInfos, new ArrayList<>());

        CamInfoResponse response = assertDoesNotThrow(() -> (frontend.sendCamInfo(cameras[1].getName())));
        assertEquals(cameras[1].getCoordinates(), response.getCoordinates());
    }

    @Test
    public void getInfoLength15Test() {
        List<CamInfo> camInfos = new ArrayList<>();
        camInfos.add(cameras[2]);
        frontend.sendCtrlInit(camInfos, new ArrayList<>());

        CamInfoResponse response = assertDoesNotThrow(() -> (frontend.sendCamInfo(cameras[2].getName())));
        assertEquals(cameras[2].getCoordinates(), response.getCoordinates());
    }

    /* Invalid Arguments Tests */

    @Test
    public void getInfoLength2Test() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamInfo(cameras[3].getName()));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void getInfoLength16Test() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamInfo(cameras[4].getName()));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    /* Doesnt exist test */

    @Test
    public void getInfoDoesntExistTest() {
        List<CamInfo> camInfos = new ArrayList<>();
        camInfos.add(cameras[0]);
        frontend.sendCtrlInit(camInfos, new ArrayList<>());

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCamInfo(cameras[5].getName()));
        assertEquals(NOT_FOUND, exception.getStatus().getCode());
    }
}