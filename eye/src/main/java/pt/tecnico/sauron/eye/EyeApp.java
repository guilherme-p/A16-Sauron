package pt.tecnico.sauron.eye;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.CamJoinResponse;
import pt.tecnico.sauron.silo.grpc.CamReportResponse;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class EyeApp {

	public static void main(String[] args) throws InterruptedException {
		System.out.println("--- EYE ---");

		EyeServiceImpl service = null;
		String zooHost;
		String zooPort;
		int numReplicas;
		int cacheSize;
		String name = null;
		Double latitude = null;
		Double longitude = null;

		zooHost = args[0];
		zooPort = args[1];

		if (args.length == 7) {
			numReplicas = Integer.parseInt(args[2]);
			cacheSize = Integer.parseInt(args[3]);
			name = args[4];
			latitude = Double.parseDouble(args[5]);
			longitude = Double.parseDouble(args[6]);
			service = new EyeServiceImpl(zooHost, zooPort, numReplicas, cacheSize, name);
		}
		else if (args.length == 8) {
			String serverPath = args[2];
			numReplicas = Integer.parseInt(args[3]);
			cacheSize = Integer.parseInt(args[4]);
			name = args[5];
			latitude = Double.parseDouble(args[6]);
			longitude = Double.parseDouble(args[7]);
			service = new EyeServiceImpl(zooHost, zooPort, serverPath, numReplicas, cacheSize, name);
		}
		else {
			System.out.println("Invalid arguments: Insert zoo_host zoo_port [server_instance] num_replicas cache_size name latitude longitude");
			System.exit(1);
		}

		System.out.println(zooHost + ":" + zooPort);
		System.out.println(String.format("%s (%f, %f)", name, latitude, longitude));

		try {
			CamJoinResponse response = service.sendCamJoin(name, latitude, longitude);
			System.out.println("Cam join successful");
		} catch (StatusRuntimeException exception) {
			System.out.println("Caught exception: " + exception.getStatus().getCode());
			System.exit(1);
		}

		final Scanner scanner = new Scanner(System.in);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			if (line.isEmpty()) {
				if (service.hasObservations()) {
					handleCamReport(service);
				}
			}
			else if (isComment(line)) {
				/* Do nothing */
			}
			else if (isPause(line)) {
				String[] data = line.split(",");
				int time = Integer.parseInt(data[1]);
				TimeUnit.MILLISECONDS.sleep(time);
			}
			else {
				String[] data = line.split(",");
				if (data.length != 2) {
					System.err.println("Invalid input");
				}

				service.addObservation(data[0], data[1]);
			}
		}

		if (service.hasObservations()) {
			handleCamReport(service);
		}

		scanner.close();
		service.close();
	}

	private static void handleCamReport(EyeServiceImpl service) {
		try {
			CamReportResponse response = service.sendCamReport();
			System.out.println("Sent observations");
		} catch (StatusRuntimeException exception) {
			System.out.println("Caught exception: " + exception.getStatus().getDescription());
			exception.printStackTrace();
		}
	}

	private static boolean isComment(String line) {
		return line.charAt(0) == '#';
	}

	private static boolean isPause(String line) {
		return line.startsWith("zzz");
	}

}
