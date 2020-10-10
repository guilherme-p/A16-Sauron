package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import pt.tecnico.sauron.silo.grpc.PingRequest;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.IOException;
import java.util.Properties;


public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	protected static SiloFrontend frontend;
	
	@BeforeAll
	public static void oneTimeSetup() throws IOException {
		testProps = new Properties();
		
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Test properties:");
			System.out.println(testProps);
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		final String zooHost = testProps.getProperty("zoo.host");
		final String zooPort = testProps.getProperty("zoo.port");
		final String serverInstance = testProps.getProperty("server.instance");
		final int numReplicas = Integer.parseInt(testProps.getProperty("server.numReplicas"));
		final int cacheSize = Integer.parseInt(testProps.getProperty("frontend.cache"));
		frontend = new SiloFrontend(zooHost, zooPort, serverInstance, numReplicas, cacheSize);
	}
	
	@AfterAll
	public static void cleanup() {
		frontend.close();
	}

	protected PingRequest buildPingRequest(String message) {
		return PingRequest.newBuilder().setMessage(message).build();
	}
}
