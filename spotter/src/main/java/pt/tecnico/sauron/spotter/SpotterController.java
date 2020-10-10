package pt.tecnico.sauron.spotter;

import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.client.RequestBuilder;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.util.List;

/**
 * Class to transform the received inputs send the controller requests to the server
 * through the frontend
 */
public class SpotterController {

    private final SiloFrontend frontend;

    public SpotterController(String zooHost, String zooPort, int numReplicas, int cacheSize) {
        this.frontend = new SiloFrontend(zooHost, zooPort, numReplicas, cacheSize);
    }

    public SpotterController(String zooHost, String zooPort, String serverPath, int numReplicas, int cacheSize) {
        this.frontend = new SiloFrontend(zooHost, zooPort, serverPath, numReplicas, cacheSize);
    }

    public CamInfoResponse camInfo(String camName) {
        return frontend.sendCamInfo(camName);
    }

    public SpotterTrackResponse spotPerson(Long id) {
        ObjectInfo observation = RequestBuilder.buildObjectInfo(ObjectType.PERSON, id);
        return frontend.sendSpotterTrack(observation);
    }

    public SpotterTrackResponse spotCar(String plate) {
        ObjectInfo observation = RequestBuilder.buildObjectInfo(ObjectType.CAR, plate);
        return frontend.sendSpotterTrack(observation);
    }

    public SpotterTrackMatchResponse spotPersonMatch(String regex) {
        return frontend.sendSpotterTrackMatch(ObjectType.PERSON, regex);
    }

    public SpotterTrackMatchResponse spotCarMatch(String regex) {
        return frontend.sendSpotterTrackMatch(ObjectType.CAR, regex);
    }

    public SpotterTraceResponse trailPerson(Long id) {
        ObjectInfo observation = RequestBuilder.buildObjectInfo(ObjectType.PERSON, id);
        return frontend.sendSpotterTrace(observation);
    }

    public SpotterTraceResponse trailCar(String plate) {
        ObjectInfo observation = RequestBuilder.buildObjectInfo(ObjectType.CAR, plate);
        return frontend.sendSpotterTrace(observation);
    }

    public PingResponse ping(String message) {
        return frontend.sendCtrlPing(message);
    }

    public ClearResponse clear() {
        return frontend.sendCtrlClear();
    }

    public InitResponse init(List<CamInfo> camInfoList, List<ObservationInfo> observationInfoList) {
        return frontend.sendCtrlInit(camInfoList, observationInfoList);
    }

    public void close() {
        frontend.close();
    }
}
