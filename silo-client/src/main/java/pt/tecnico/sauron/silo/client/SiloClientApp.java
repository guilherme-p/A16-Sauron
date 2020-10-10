package pt.tecnico.sauron.silo.client;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class SiloClientApp {
	
	public static void main(String[] args) {
		System.out.println(SiloClientApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String serverInstance = args[2];
		final int numReplicas = Integer.parseInt(args[3]);
		final int cacheSize = Integer.parseInt(args[4]);

		try (SiloFrontend frontend = new SiloFrontend(zooHost, zooPort, serverInstance, numReplicas, cacheSize)) {
			PingResponse response = frontend.sendCtrlPing("Hello");
			System.out.println(response);
		} catch (StatusRuntimeException exception) {
			System.out.println("Caught exception: " + exception.getStatus().getDescription());
			exception.printStackTrace();
		}
	}
	
}
