package pt.tecnico.sauron.silo;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.sauron.silo.grpc.GrpcController;
import pt.tecnico.sauron.silo.domain.*;
import pt.tecnico.sauron.silo.grpc.GrpcGossipSender;
import pt.tecnico.sauron.silo.replication.VectorTimestamp;
import pt.tecnico.sauron.silo.service.ReplicaService;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.IOException;

public class SiloServerApp {

	private static final String SERVER_PATH = "/grpc/sauron/silo";

	private static String serverHost;
	private static String serverPort;
	private static String serverPath;
	private static ZKNaming zkNaming = null;

	private static final void close() {
		try {
			System.out.println("Terminating");
			if (zkNaming != null) {
				System.out.println("Unbinding server with path " + serverPath + " at " + serverHost + ":" + serverPort);
				zkNaming.unbind(serverPath, serverHost, serverPort);
			}
		} catch (ZKNamingException exception) {
			System.out.println("SEVERE: Unable to unbind server");
			exception.printStackTrace();
		}
	}
	
	public static void main(String[] args)
			throws IOException, InterruptedException, ZKNamingException {

		System.out.println(SiloServerApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.println(String.format("Received %d arguments", args.length));
		for (int i = 0; i < args.length; i++) {
			System.out.println(String.format("arg[%d] = %s", i, args[i]));
		}

		// Parse args
		final String zooHost = args[0];
		final String zooPort = args[1];
		final String serverInstance = args[2];
		serverHost = args[3];
		serverPort = args[4];
		serverPath = SERVER_PATH  + "/" + serverInstance;
		final int numReplicas = Integer.parseInt(args[5]);
		final int gossipRate = Integer.parseInt(args[6]);

		// Build Repositories
        final CameraRepository cameraRepository = new CameraRepository();
		final ObservationRepository<Long, Person> personRepository = new PersonRepository();
		final ObservationRepository<String, Car> carRepository = new CarRepository();

		// Build Replication Structures
		final VectorTimestamp valueTimestamp = new VectorTimestamp(numReplicas);
		final VectorTimestamp replicaTimestamp = new VectorTimestamp(numReplicas);
		final VectorTimestamp[] timestampTable = new VectorTimestamp[numReplicas];
		for (int i = 0; i < numReplicas; i++) {
			timestampTable[i] = new VectorTimestamp(numReplicas);
		}

		// Build Service
		final ReplicaService replicaService = new ReplicaService(
				cameraRepository, personRepository, carRepository,
				valueTimestamp, replicaTimestamp, timestampTable,
				Integer.parseInt(serverInstance),
				new GrpcGossipSender(zooHost, zooPort, SERVER_PATH, Integer.parseInt(serverInstance)),
				gossipRate);

		// Build Controller
		final GrpcController grpcController = new GrpcController(replicaService);

		// Build grpc service
		final BindableService siloServerService = new SiloServerServiceImpl(grpcController);
		final BindableService siloReplicationService = new SiloServerReplicationImpl(grpcController);
		final Server server = ServerBuilder.forPort(Integer.parseInt(serverPort))
				.addService(siloServerService)
				.addService(siloReplicationService)
				.build();

		// Run server
		try {
			zkNaming = new ZKNaming(zooHost, zooPort);
			// publish
			zkNaming.rebind(serverPath, serverHost, serverPort);

			Runtime.getRuntime().addShutdownHook(new Thread(SiloServerApp::close));

			// start gRPC server
			server.start();
			System.out.println("Server started");

			// await termination
			server.awaitTermination();
		} finally {
			if (zkNaming != null) {
				zkNaming.unbind(serverPath, serverHost, serverPort);
			}
		}
	}
	
}
