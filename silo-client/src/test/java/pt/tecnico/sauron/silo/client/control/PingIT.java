package pt.tecnico.sauron.silo.client.control;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.grpc.*;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PingIT extends BaseIT {

    @Test
    public void pingOkTest() {
        PingResponse pingResponse = frontend.sendCtrlPing("friend");
        assertEquals("Hello friend!", pingResponse.getMessage());
    }

    @Test
    public void pingEmptyTest() {
        PingRequest pingRequest = buildPingRequest("");
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.sendCtrlPing(""));
        assertEquals(INVALID_ARGUMENT, exception.getStatus().getCode());
    }
}
