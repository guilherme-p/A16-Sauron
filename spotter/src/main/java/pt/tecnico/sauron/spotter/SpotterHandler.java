package pt.tecnico.sauron.spotter;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class to handle output and exceptions of the server responses
 */
public class SpotterHandler {

    /* Comparator to sort observationInfos by id */
    private static final Comparator<ObservationInfo> OBSERVATION_INFO_COMPARATOR_NUMERIC =
            Comparator.comparingLong(o -> o.getObservation().getObjectInfo().getNumericId());

    private static final Comparator<ObservationInfo> OBSERVATION_INFO_COMPARATOR_ALPHANUMERIC =
            Comparator.comparing(o -> o.getObservation().getObjectInfo().getAlphanumericId());

    private SpotterController spotterController;

    public SpotterHandler(SpotterController spotterController) {
        this.spotterController = spotterController;
    }

    public String handleCamInfo(String camName) {
        try {
            CamInfoResponse response = spotterController.camInfo(camName);
            return camName + ": " + response.getCoordinates().getLatitude() + ", " + response.getCoordinates().getLongitude() + "\n";
        } catch (StatusRuntimeException exception) {
            return "Caught exception: " + exception.getStatus().getDescription() + "\n";
        }
    }

    public String handleSpotPerson(Long id) {
       try {
           SpotterTrackResponse response = spotterController.spotPerson(id);

           CamInfo camInfo = response.getObservationInfo().getCamInfo();
           Observation observation = response.getObservationInfo().getObservation();

           return printPerson(camInfo, observation) + "\n";

       } catch (StatusRuntimeException exception) {
           return "Caught exception: " + exception.getStatus().getDescription() + "\n";
       }
    }

    public String handleSpotCar(String plate) {
        try {
            SpotterTrackResponse response = spotterController.spotCar(plate);

            CamInfo camInfo = response.getObservationInfo().getCamInfo();
            Observation observation = response.getObservationInfo().getObservation();

            return printCar(camInfo, observation) + "\n";

        } catch (StatusRuntimeException exception) {
            return "Caught exception: " + exception.getStatus().getDescription() + "\n";
        }
    }

    public String handleSpotMatchPerson(String regex) {
        try {
            String output = "";

            SpotterTrackMatchResponse response = spotterController.spotPersonMatch(regex);
            List<ObservationInfo> observationInfoList = new ArrayList<>(response.getObservationInfosList());

            observationInfoList.sort(OBSERVATION_INFO_COMPARATOR_NUMERIC);

            for (ObservationInfo observationInfo : observationInfoList) {
                CamInfo camInfo = observationInfo.getCamInfo();
                Observation observation = observationInfo.getObservation();

                output += printPerson(camInfo, observation) + "\n";
            }

            return output.length() == 0 ? "Person(s) not found\n" : output;

        } catch (StatusRuntimeException exception) {
            return "Caught exception: " + exception.getStatus().getDescription() + "\n";
        }
    }

    public String handleSpotMatchCar(String regex) {
        try {
            String output = "";

            SpotterTrackMatchResponse response = spotterController.spotCarMatch(regex);
            List<ObservationInfo> observationInfoList = new ArrayList<>(response.getObservationInfosList());

            observationInfoList.sort(OBSERVATION_INFO_COMPARATOR_ALPHANUMERIC);

            for (ObservationInfo observationInfo : observationInfoList) {
                CamInfo camInfo = observationInfo.getCamInfo();
                Observation observation = observationInfo.getObservation();

                output += printCar(camInfo, observation) + "\n";
            }

            return output.length() == 0 ? "Car(s) not found\n" : output;

        } catch (StatusRuntimeException exception) {
            return "Caught exception: " + exception.getStatus().getDescription() + "\n";
        }
    }

    public String handleTrailPerson(Long id) {
        try {
            StringBuilder output = new StringBuilder();

            SpotterTraceResponse response = spotterController.trailPerson(id);
            List<ObservationInfo> observationInfoList = response.getObservationInfosList();

            for (ObservationInfo observationInfo : observationInfoList) {
                CamInfo camInfo = observationInfo.getCamInfo();
                Observation observation = observationInfo.getObservation();

                output.append(printPerson(camInfo, observation)).append("\n");
            }

            return output.toString();

        } catch (StatusRuntimeException exception) {
            return "Caught exception: " + exception.getStatus().getDescription() + "\n";
        }
    }

    public String handleTrailCar(String plate) {
        try {
            StringBuilder output = new StringBuilder();

            SpotterTraceResponse response = spotterController.trailCar(plate);
            List<ObservationInfo> observationInfoList = response.getObservationInfosList();

            for (ObservationInfo observationInfo : observationInfoList) {
                CamInfo camInfo = observationInfo.getCamInfo();
                Observation observation = observationInfo.getObservation();

                output.append(printCar(camInfo, observation)).append("\n");
            }

            return output.toString();

        } catch (StatusRuntimeException exception) {
            return "Caught exception: " + exception.getStatus().getDescription() + "\n";
        }
    }

    public String handlePing(String message) {
        try {
            PingResponse response = spotterController.ping(message);
            return response.getMessage() + "\n";
        } catch (StatusRuntimeException exception) {
            return "Caught exception: " + exception.getStatus().getDescription() + "\n";
        }
    }

    public String handleClear() {
        try {
            ClearResponse response = spotterController.clear();
            return "Silo cleared\n";
        } catch (StatusRuntimeException exception) {
            return "Caught exception: " + exception.getStatus().getDescription() + "\n";
        }
    }

    public String handleInit(List<CamInfo> camInfoList, List<ObservationInfo> observationInfoList) {
        try {
            InitResponse response = spotterController.init(camInfoList, observationInfoList);
            return "Silo initiated\n";
        } catch (StatusRuntimeException exception) {
            return "Caught exception: " + exception.getStatus().getDescription() + "\n";
        }
    }

    private String printPerson(CamInfo camInfo, Observation observation) {
        Instant timestamp = Instant.ofEpochSecond(observation.getTimestamp().getSeconds());
        return String.join(",",
                "person",
                String.valueOf(observation.getObjectInfo().getNumericId()),
                timestamp.toString().replace("Z", ""),
                camInfo.getName(),
                String.valueOf(camInfo.getCoordinates().getLatitude()),
                String.valueOf(camInfo.getCoordinates().getLongitude()));
    }

    private String printCar(CamInfo camInfo, Observation observation) {
        Instant timestamp = Instant.ofEpochSecond(observation.getTimestamp().getSeconds());
        return String.join(",",
                "car",
                observation.getObjectInfo().getAlphanumericId(),
                timestamp.toString().replace("Z", ""),
                camInfo.getName(),
                String.valueOf(camInfo.getCoordinates().getLatitude()),
                String.valueOf(camInfo.getCoordinates().getLongitude()));
    }
}
