package pt.tecnico.sauron.spotter;


import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.client.RequestBuilder;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.BufferedInputStream;
import java.time.Instant;
import java.util.*;

public class SpotterApp {
	public static void main(String[] args) {
		System.out.println("--- SPOTTER ---");

		SpotterController spotterController = null;

		final String zooHost = args[0];
		final String zooPort = args[1];

		System.out.println(zooHost + ":" + zooPort + "\n");
		if (args.length == 4) {
			final int numReplicas = Integer.parseInt(args[2]);
			final int cacheSize = Integer.parseInt(args[3]);
			spotterController = new SpotterController(zooHost, zooPort, numReplicas, cacheSize);
		}
		else if (args.length == 5) {
			final String serverInstance = args[2];
			final int numReplicas = Integer.parseInt(args[3]);
			final int cacheSize = Integer.parseInt(args[4]);
			System.out.println("Instance: " + serverInstance + ", replicas" + numReplicas);
			spotterController = new SpotterController(zooHost, zooPort, serverInstance, numReplicas, cacheSize);
		}
		else {
			System.out.println("Invalid arguments: Insert zoo_host zoo_port [server_instance] num_replicas cache_size");
			System.exit(1);
		}
		final SpotterHandler spotterHandler = new SpotterHandler(spotterController);

		Scanner stdin = new Scanner(new BufferedInputStream(System.in));
		boolean running = true;
		while (running && stdin.hasNextLine()) {
			final String[] input = stdin.nextLine().split("\\s+");
			final String command = input[0];

			switch(command) {
				case "spot":
					spotParser(input, spotterHandler);
					break;

				case "trail":
					trailParser(input, spotterHandler);
					break;

				case "ping":
					pingParser(input, spotterHandler);
					break;

				case "clear":
					clearParser(input, spotterHandler);
					break;

				case "init":
					initParser(input, spotterHandler);
					break;

				case "info":
					camInfo(input, spotterHandler);
					break;

				case "help":
					helpHandler();
					break;

				case "exit":
					running = false;
					break;

				default:
					invalidHandler();
			}

		}

		spotterController.close();
	}

	private static void showInvalid() {
		final String INVALID_ARGUMENTS = "\n\nINVALID ARGUMENTS\n";

		System.out.println(INVALID_ARGUMENTS);
	}

	private static void showHelp() {
		final String HELP_MESSAGE = "Spotter is a command-line interface used to interact with a silo server.\n"
				                  + "usage: $ spotter <host> <port> e.g. $ spotter localhost 8080\n\n"
								  + "Commands:\n"
								  + "\t spot: searches for latest observation given an exact id, or latest observations\n \t\twhich match a partial id (with * wildcard, sorted by id)\n\n"
								  + "\t\t usage: $ spot <observationType> <id> e.g. spot person 14388236 e.g. spot car 70*\n"
								  + "\t\t returns: <observationType>,<id>,<observationTimestamp>,<camName>,<camLatitude>,<camLongitude>\n\n"
								  + "\t trail: searches for all observations given an exact id, sorted from most recent -> oldest\n\n"
								  + "\t\t usage: $ trail <observationType> <id> e.g. trail person 14388236\n"
								  + "\t\t returns: <observationType>,<id>,<observationTimestamp>,<camName>,<camLatitude>,<camLongitude>\n\n"
								  + "\t info: gets camera information from silo\n\n"
								  + "\t\t usage: $ info <camName> e.g. info Tagus\n"
								  + "\t\t returns: <camName>: <camLatitude>, <camLongitude>\n\n"
								  + "\t ping: pings silo server\n\n"
								  + "\t\t usage: $ ping <message> e.g. ping Client\n"
								  + "\t\t returns: Hello <message>!\n\n"
								  + "\t clear: clears silo server\n\n"
								  + "\t\t usage: $ clear\n"
								  + "\t\t returns: Silo server cleared\n\n"
								  + "\t init: initializes silo server with Cameras and Observations\n\n"
								  + "\t\t usage: $ init camInfos(<camName>,<camLatitude>,<camLongitude>)  observationInfos(<observationType>,<id>,<observationTimestamp>,<camName>,<camLatitude>,<camLongitude>)\n \t\t\t  separated by ;\n\n"
								  + "\t\t e.g. init Tagus,38.737613,-9.303164 car,5759LL,2019-10-22T09:07:51,Tagus,38.737613,-9.303164;car,7013LL,2019-10-04T11:02:07,Tagus,38.737613,-9.303164\n"
								  + "\t\t returns: Silo server initialized\n\n";

		System.out.println(HELP_MESSAGE);
	}

	private static void spotParser(String[] input, SpotterHandler spotterHandler) {
		if (input.length == 3) {
			final String observationType = input[1];

			if (observationType.equals("person")) {
				final String personId = input[2];

				if (personId.contains("*")) {
					System.out.println(spotterHandler.handleSpotMatchPerson(personId));
					return;
				}

				else if (isNumber(personId)) {
					System.out.println(spotterHandler.handleSpotPerson(Long.parseLong(personId)));
					return;
				}
			}

			else if (observationType.equals("car")) {
				final String plate = input[2];

				if (plate.contains("*")) {
					System.out.println(spotterHandler.handleSpotMatchCar(plate));
				}

				else {
					System.out.println(spotterHandler.handleSpotCar(plate));
				}

				return;
			}
		}

		invalidHandler();
	}

	private static void trailParser(String[] input, SpotterHandler spotterHandler) {
		if (input.length == 3) {
			final String observationType = input[1];

			if (observationType.equals("person")) {
				final String personId = input[2];

				if (isNumber(personId)) {
					System.out.println(spotterHandler.handleTrailPerson(Long.parseLong(personId)));
					return;
				}
			}

			else if (observationType.equals("car")) {
				final String plate = input[2];

				System.out.println(spotterHandler.handleTrailCar(plate));
				return;
			}
		}

		invalidHandler();
	}

	private static void pingParser(String[] input, SpotterHandler spotterHandler) {
		if (input.length == 2) {
			final String message = input[1];

			System.out.println(spotterHandler.handlePing(message));
			return;
		}

		invalidHandler();
	}

	private static void clearParser(String[] input, SpotterHandler spotterHandler) {
		if (input.length == 1) {
			System.out.println(spotterHandler.handleClear());
			return;
		}

		invalidHandler();
	}

	private static void initParser(String[] input, SpotterHandler spotterHandler) {
		if (input.length == 3) {
			final String[] camInfoListStr = input[1].split(";");
			final String[] observationInfoListStr = input[2].split(";");

			List<CamInfo> camInfoList = camInfoParser(camInfoListStr);
			List<ObservationInfo> observationInfoList = observationInfoParser(observationInfoListStr);

			if (!camInfoList.isEmpty() && !observationInfoList.isEmpty()) {
				System.out.println(spotterHandler.handleInit(camInfoList, observationInfoList));
				return;
			}
		}

		invalidHandler();
	}

	private static void camInfo(String[] input, SpotterHandler spotterHandler) {
		if (input.length == 2) {
			final String camName = input[1];

			System.out.println(spotterHandler.handleCamInfo(camName));
			return;
		}

		invalidHandler();
	}

	private static void helpHandler() {
		showHelp();
	}

	private static void invalidHandler() {
		showInvalid();
		showHelp();
	}

	private static List<CamInfo> camInfoParser(String[] camInfoListStr) {
		List<CamInfo> camInfoList = new ArrayList<>();

		for (String camInfoStr : camInfoListStr) {
			String[] camInfoSplit = camInfoStr.split(",");

			if (camInfoSplit.length == 3) {
				String camName = camInfoSplit[0];
				String latitudeStr = camInfoSplit[1];
				String longitudeStr = camInfoSplit[2];

				if (isDouble(latitudeStr) && isDouble(longitudeStr)) {
					Double latitude = Double.parseDouble(latitudeStr);
					Double longitude = Double.parseDouble(longitudeStr);

					Coordinates coords = RequestBuilder.buildCoordinates(latitude, longitude);
					CamInfo camInfo = RequestBuilder.buildCamInfo(camName, coords);

					camInfoList.add(camInfo);
					continue;
				}
			}

			return Collections.emptyList();
		}

		return camInfoList;
	}

	private static List<ObservationInfo> observationInfoParser(String[] observationInfoListStr) {
		List<ObservationInfo> observationInfoList = new ArrayList<>();

		for (String observationInfoStr : observationInfoListStr) {
			String[] observationInfoSplit = observationInfoStr.split(",");

			if (observationInfoSplit.length == 6) {
				String observationType = observationInfoSplit[0];
				String idStr = observationInfoSplit[1];
				String timestampStr = observationInfoSplit[2] + "Z";
				String camName = observationInfoSplit[3];
				String latitudeStr = observationInfoSplit[4];
				String longitudeStr = observationInfoSplit[5];

				if (observationType.equals("person")) {
					if (isNumber(idStr) && isDouble(latitudeStr) && isDouble(longitudeStr)) {
						Long personId = Long.parseLong(idStr);
						Double latitude = Double.parseDouble(latitudeStr);
						Double longitude = Double.parseDouble(longitudeStr);

						Coordinates coords = RequestBuilder.buildCoordinates(latitude, longitude);
						CamInfo camInfo = RequestBuilder.buildCamInfo(camName, coords);

						ObjectInfo objectInfo = RequestBuilder.buildObjectInfo(ObjectType.PERSON, personId);

						Timestamp timestamp = RequestBuilder.buildTimeStamp(Instant.parse(timestampStr));

						Observation observation = RequestBuilder.buildObservation(objectInfo, timestamp);

						ObservationInfo observationInfo = RequestBuilder.buildObservationInfo(observation, camInfo);

						observationInfoList.add(observationInfo);
						continue;
					}
				}

				else if (observationType.equals("car")) {
					if (isDouble(latitudeStr) && isDouble(longitudeStr)) {
						String plate = idStr;
						Double latitude = Double.parseDouble(latitudeStr);
						Double longitude = Double.parseDouble(longitudeStr);

						Coordinates coords = RequestBuilder.buildCoordinates(latitude, longitude);
						CamInfo camInfo = RequestBuilder.buildCamInfo(camName, coords);

						ObjectInfo objectInfo = RequestBuilder.buildObjectInfo(ObjectType.CAR, plate);

						Timestamp timestamp = RequestBuilder.buildTimeStamp(Instant.parse(timestampStr));

						Observation observation = RequestBuilder.buildObservation(objectInfo, timestamp);

						ObservationInfo observationInfo = RequestBuilder.buildObservationInfo(observation, camInfo);

						observationInfoList.add(observationInfo);
						continue;
					}
				}
			}

			return Collections.emptyList();
		}

		return observationInfoList;
	}


	private static boolean isNumber(String s) {
		return s.matches("\\d+");
	}

	private static boolean isDouble(String s) {
		return s.matches("-?\\d+\\.\\d+");
	}

}
