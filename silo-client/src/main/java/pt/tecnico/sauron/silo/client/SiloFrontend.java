package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.*;
import java.util.stream.IntStream;

public class SiloFrontend implements AutoCloseable {

    private static final String REPLICA_BASE_PATH = "/grpc/sauron/silo";
    private static final int MAX_RETRY = 3;

    private ManagedChannel channel;
    private SiloGrpc.SiloBlockingStub stub;
    private final Long[] prev;
    private final ZKNaming zkNaming;
    private String currentTarget;

    // Set to true if any replica can be used to connect
    private final boolean randomTargetReplica;

    // Set to true if there is an active connection
    private boolean connected;

    // Replica path if can only connect to single replica
    private final String replicaPath;

    private final Cache<String, CamInfoResponse> camInfoCache;
    private final Cache<String, SpotterTrackResponse> spotterTrackCache;
    private final Cache<String, SpotterTrackMatchResponse> spotterTrackMatchCache;
    private final Cache<String, SpotterTraceResponse> spotterTraceCache;

    // Auxiliary classes to handle the context of requests
    private RequestContext requestContext = new RequestContext();

    public SiloFrontend(String zooHost, String zooPort, int numReplicas, int maxCacheSize) {
        randomTargetReplica = true;
        camInfoCache = new Cache<>(maxCacheSize);
        spotterTrackCache = new Cache<>(maxCacheSize);
        spotterTrackMatchCache = new Cache<>(maxCacheSize);
        spotterTraceCache = new Cache<>(maxCacheSize);

        zkNaming = new ZKNaming(zooHost, zooPort);
        replicaPath = null;
        connectRandomReplica();
        connected = true;

        this.prev = new Long[numReplicas];
        Arrays.fill(this.prev, 0L);
    }

    public SiloFrontend(String zooHost, String zooPort, String replicaInstance, int numReplicas, int maxCacheSize) {
        randomTargetReplica = false;
        camInfoCache = new Cache<>(maxCacheSize);
        spotterTrackCache = new Cache<>(maxCacheSize);
        spotterTrackMatchCache = new Cache<>(maxCacheSize);
        spotterTraceCache = new Cache<>(maxCacheSize);

        zkNaming = new ZKNaming(zooHost, zooPort);
        replicaPath = String.join("/", REPLICA_BASE_PATH, replicaInstance);
        connectTargetReplica();
        connected = true;

        this.prev = new Long[numReplicas];
        Arrays.fill(this.prev, 0L);
    }

    /* Queries */

    public CamInfoResponse sendCamInfo(String name) {
        VectorTS prevTS = RequestBuilder.buildVectorTimestamp(prev);
        CamInfoRequest request = RequestBuilder.buildCamInfoRequest(name, prevTS);
        // Key to later search cache
        String key = createKey(request);

        CamInfoResponse received = null;
        requestContext.start(camInfoCache.containsKey(key));
        while (requestContext.isTrying()) {
            try {
                connect();
                received = stub.camInfo(request);
                requestContext.completed();
            } catch (StatusRuntimeException exception) {
                requestContext.onStatusRuntimeException(exception);
            }
        }

        // Update value in cache if out of date
        updateCache(key, received);

        // Return value from cache
        CamInfoResponse response = camInfoCache.get(key);
        Long[] newTS = createTimestampArray(response.getNew());
        merge(prev, newTS);
        return received;
    }

    public SpotterTrackResponse sendSpotterTrack(ObjectInfo objectInfo) {
        VectorTS prevTS = RequestBuilder.buildVectorTimestamp(prev);
        SpotterTrackRequest request = RequestBuilder.buildSpotterTrackRequest(objectInfo, prevTS);
        // Key to later search cache
        String key = createKey(request);

        SpotterTrackResponse received = null;
        requestContext.start(spotterTrackCache.containsKey(key));
        while (requestContext.isTrying()) {
            try {
                connect();
                received = stub.spotterTrack(request);
                requestContext.completed();
            } catch (StatusRuntimeException exception) {
                requestContext.onStatusRuntimeException(exception);
            }
        }

        // Update value in cache if out of date
        updateCache(key, received);

        // Return value from cache
        SpotterTrackResponse response = spotterTrackCache.get(key);
        Long[] newTS = createTimestampArray(response.getNew());
        merge(prev, newTS);
        return response;
    }

    public SpotterTrackMatchResponse sendSpotterTrackMatch(ObjectType objectType, String regex) {
        VectorTS prevTS = RequestBuilder.buildVectorTimestamp(prev);
        SpotterTrackMatchRequest request = RequestBuilder.buildSpotterTrackMatchRequest(objectType, regex, prevTS);
        // Key to later search cache
        String key = createKey(request);

        SpotterTrackMatchResponse received = null;
        requestContext.start(spotterTrackMatchCache.containsKey(key));
        while (requestContext.isTrying()) {
            try {
                connect();
                received = stub.spotterTrackMatch(request);
                requestContext.completed();
            } catch (StatusRuntimeException exception) {
                requestContext.onStatusRuntimeException(exception);
            }
        }

        // Update value in cache if needed
        updateCache(key, received);

        // Return value from cache
        SpotterTrackMatchResponse response = spotterTrackMatchCache.get(key);
        Long[] newTS = createTimestampArray(response.getNew());
        merge(prev, newTS);
        return response;
    }

    public SpotterTraceResponse sendSpotterTrace(ObjectInfo objectInfo) {
        VectorTS prevTS = RequestBuilder.buildVectorTimestamp(prev);
        SpotterTraceRequest request = RequestBuilder.buildSpotterTraceRequest(objectInfo, prevTS);
        // Key to later search cache
        String key = createKey(request);

        SpotterTraceResponse received = null;
        requestContext.start(spotterTraceCache.containsKey(key));
        while (requestContext.isTrying()) {
            try {
                connect();
                received = stub.spotterTrace(request);
                requestContext.completed();
            } catch (StatusRuntimeException exception) {
                requestContext.onStatusRuntimeException(exception);
            }
        }

        // Update value in cache if needed
        updateCache(key, received);

        // Return value from cache
        SpotterTraceResponse response = spotterTraceCache.get(key);
        Long[] newTS = createTimestampArray(response.getNew());
        merge(prev, newTS);
        return response;
    }

    /* Updates */

    public CamJoinResponse sendCamJoin(CamInfo camInfo) {
        VectorTS prevTS = RequestBuilder.buildVectorTimestamp(prev);
        CamJoinRequest request = RequestBuilder.buildCamJoinRequest(camInfo, prevTS);

        CamJoinResponse response = null;
        requestContext.start();
        while (requestContext.isTrying()) {
            try {
                connect();
                response = stub.camJoin(request);
                requestContext.completed();
            } catch (StatusRuntimeException exception) {
                requestContext.onStatusRuntimeException(exception);
            }
        }

        Long[] newTS = createTimestampArray(response.getUpdateID());
        merge(prev, newTS);
        return response;
    }

    public CamReportResponse sendCamReport(String camName, List<ObjectInfo> objectInfos) {
        VectorTS prevTS = RequestBuilder.buildVectorTimestamp(prev);
        CamReportRequest request = RequestBuilder.buildCamReportRequest(camName, objectInfos, prevTS);

        CamReportResponse response = null;
        requestContext.start();
        while (requestContext.isTrying()) {
            try {
                connect();
                response = stub.camReport(request);
                requestContext.completed();
            } catch (StatusRuntimeException exception) {
                requestContext.onStatusRuntimeException(exception);
            }
        }

        Long[] newTS = createTimestampArray(response.getUpdateID());
        merge(prev, newTS);
        return response;
    }

    public PingResponse sendCtrlPing(String message) {
        PingRequest request = RequestBuilder.buildPingRequest(message);
        PingResponse response = null;
        requestContext.start();
        while (requestContext.isTrying()) {
            try {
                connect();
                response = stub.ctrlPing(request);
                requestContext.completed();
            } catch (StatusRuntimeException exception) {
                requestContext.onStatusRuntimeException(exception);
            }
        }
        return response;
    }

    public ClearResponse sendCtrlClear() {
        VectorTS prevTS = RequestBuilder.buildVectorTimestamp(prev);
        ClearRequest request = RequestBuilder.buildClearRequest(prevTS);

        ClearResponse response = null;
        requestContext.start();
        while (requestContext.isTrying()) {
            try {
                connect();
                response = stub.ctrlClear(request);
                requestContext.completed();
            } catch (StatusRuntimeException exception) {
                requestContext.onStatusRuntimeException(exception);
            }
        }

        Long[] newTS = createTimestampArray(response.getUpdateID());
        merge(prev, newTS);
        return response;
    }

    public InitResponse sendCtrlInit(List<CamInfo> camInfos, List<ObservationInfo> observationInfos) {
        VectorTS prevTS = RequestBuilder.buildVectorTimestamp(prev);
        InitRequest request = RequestBuilder.buildInitRequest(camInfos, observationInfos, prevTS);

        InitResponse response = null;
        requestContext.start();
        while (requestContext.isTrying()) {
            try {
                connect();
                response = stub.ctrlInit(request);
                requestContext.completed();
            } catch (StatusRuntimeException exception) {
                requestContext.onStatusRuntimeException(exception);
            }
        }

        Long[] newTS = createTimestampArray(response.getUpdateID());
        merge(prev, newTS);
        return response;
    }

    @Override
    public void close() {
        if (channel != null) channel.shutdown();
    }

    /* Timestamps auxiliary functions */

    private Long[] createTimestampArray(VectorTS ts) {
        return ts.getTimestampList().toArray(new Long[0]);
    }

    private void merge(Long[] prevTS, Long[] newTS) {
        Arrays.setAll(prevTS, index -> Math.max(prevTS[index], newTS[index]));
    }

    private boolean happensBefore(Long[] timestamp, Long[] other) {
        return IntStream.range(0, timestamp.length)
                .allMatch(index -> timestamp[index] <= other[index]);
    }

    /* Connection auxiliary functions */

    private void connect() {
        if (!connected) {
            if (randomTargetReplica) {
                connectRandomReplica();
            }
            else {
                connectTargetReplica();
            }
        }
    }

    private void connectRandomReplica() {
        try {
            List<ZKRecord> zkRecords = new ArrayList<>(zkNaming.listRecords(REPLICA_BASE_PATH));
            if (zkRecords.isEmpty()) {
                System.out.println("SEVERE: No available replicas");
                System.exit(1); // No replica to connect to
            }
            Collections.shuffle(zkRecords);
            Iterator<ZKRecord> recordIterator = zkRecords.iterator();
            connected = false;
            // Iter replicas in random order and try to connect to every one
            while (!connected && recordIterator.hasNext()) {
                ZKRecord zkRecord = recordIterator.next();
                connectSingleRandomTarget(zkRecord);
            }
            // If in the end cant connect to any replica exit
            if (!connected) {
                System.out.println("SEVERE: Could not connect to any server");
                System.exit(1);
            }
        } catch (ZKNamingException exception) {
            System.out.println("SEVERE: Could not connect to any server");
            System.exit(1);
        }
    }

    private void connectSingleRandomTarget(ZKRecord zkRecord) {
        try {
            System.out.println(String.format("INFO: Connecting to server '%s' at '%s'", zkRecord.getPath(), zkRecord.getURI()));
            currentTarget = zkRecord.getURI();
            channel = ManagedChannelBuilder.forTarget(currentTarget)
                    .usePlaintext()
                    .build();
            stub = SiloGrpc.newBlockingStub(channel);
            connected = true;
            System.out.println("INFO: Connection successful");
        } catch (StatusRuntimeException exception) {
            System.out.println("WARNING: Connection failed");
        }
    }

    private void connectTargetReplica() {
        try {
            ZKRecord zkRecord = zkNaming.lookup(replicaPath);
            System.out.println(String.format("INFO: Connecting to server '%s' at '%s'", zkRecord.getPath(), zkRecord.getURI()));
            currentTarget = zkRecord.getURI();
            channel = ManagedChannelBuilder.forTarget(currentTarget)
                    .usePlaintext()
                    .build();
            stub = SiloGrpc.newBlockingStub(channel);
            System.out.println("INFO: Connection successful");
        } catch (ZKNamingException exception) {
            System.out.println(String.format("SEVERE: Could not connect to '%s'", replicaPath));
            System.exit(1);
        }
    }

    /* Cache auxiliary functions */

    private String createKey(CamInfoRequest request) {
        return request.getName();
    }

    private String createKey(SpotterTrackRequest request) {
        ObjectInfo objectInfo = request.getObjectInfo();
        ObjectType type = objectInfo.getType();
        switch (type) {
            case CAR:
                return type.toString().concat(objectInfo.getAlphanumericId());
            case PERSON:
                return type.toString().concat(Long.toString(objectInfo.getNumericId()));
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private String createKey(SpotterTrackMatchRequest request) {
        return request.getObjectType().toString().concat(request.getRegex());
    }

    private String createKey(SpotterTraceRequest request) {
        ObjectInfo objectInfo = request.getObjectInfo();
        ObjectType type = objectInfo.getType();
        switch (type) {
            case CAR:
                return type.toString().concat(objectInfo.getAlphanumericId());
            case PERSON:
                return type.toString().concat(Long.toString(objectInfo.getNumericId()));
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

    }

    private void updateCache(String key, CamInfoResponse received) {
        if (received != null) {
            if (camInfoCache.containsKey(key)) {
                CamInfoResponse cachedResponse = camInfoCache.get(key);
                Long[] cachedNew = createTimestampArray(cachedResponse.getNew());
                Long[] responseNew = createTimestampArray(received.getNew());
                if (happensBefore(cachedNew, responseNew)) {
                    // replace value in cache with response received
                    camInfoCache.replace(key, received);
                }
            }
            else {
                camInfoCache.put(key, received);
            }
        }
    }

    private void updateCache(String key, SpotterTrackResponse received) {
        if (received != null) {
            // Update value in cache
            if (spotterTrackCache.containsKey(key)) {
                SpotterTrackResponse cachedResponse = spotterTrackCache.get(key);
                Long[] cachedNew = createTimestampArray(cachedResponse.getNew());
                Long[] responseNew = createTimestampArray(received.getNew());
                if (happensBefore(cachedNew, responseNew)) {
                    // replace value in cache with response received
                    spotterTrackCache.replace(key, received);
                }
            }
            else {
                spotterTrackCache.put(key, received);
            }
        }
    }

    private void updateCache(String key, SpotterTrackMatchResponse received) {
        if (received != null) {
            if (spotterTrackMatchCache.containsKey(key)) {
                SpotterTrackMatchResponse cachedResponse = spotterTrackMatchCache.get(key);
                Long[] cachedNew = createTimestampArray(cachedResponse.getNew());
                Long[] responseNew = createTimestampArray(received.getNew());
                if (happensBefore(cachedNew, responseNew)) {
                    // replace value in cache with response received
                    spotterTrackMatchCache.replace(key, received);
                }
            } else {
                spotterTrackMatchCache.put(key, received);
            }
        }
    }

    private void updateCache(String key, SpotterTraceResponse received) {
        if (received != null) {
            if (spotterTraceCache.containsKey(key)) {
                SpotterTraceResponse cachedResponse = spotterTraceCache.get(key);
                Long[] cachedNew = createTimestampArray(cachedResponse.getNew());
                Long[] responseNew = createTimestampArray(received.getNew());
                if (happensBefore(cachedNew, responseNew)) {
                    // replace value in cache with response received
                    spotterTraceCache.replace(key, received);
                }
            }
            else {
                spotterTraceCache.put(key, received);
            }
        }
    }

    class RequestContext {

        private boolean trying;
        private int numberOfTries;
        private boolean cachedResponse;

        public RequestContext() { /* No need to init variables. Will be in start */ }

        /**
         * Method to call before every Request with no cache
         */
        public void start() {
            this.trying = true;
            this.numberOfTries = 0;
            this.cachedResponse = false;
        }

        /**
         * Method to call before every Request
         * @param cachedResponse true if the response is cached and false other wise
         */
        public void start(boolean cachedResponse) {
            this.trying = true;
            this.numberOfTries = 0;
            this.cachedResponse = cachedResponse;
        }

        public boolean isTrying() {
            return trying;
        }

        public void completed() {
            trying = false;
        }

        public void onStatusRuntimeException(StatusRuntimeException exception) {
            // If an exception is status unavailable retry connection
            if (exception.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                channel.shutdownNow();
                connected = false;
                System.out.println(String.format("INFO: Unable to send to '%s'", currentTarget));
                if (++numberOfTries == MAX_RETRY) System.exit(1);
            }
            // Not found object but have previous response in cache
            else if (cachedResponse && exception.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                // Stop retrying, result was found
                trying = false;
            }
            // In other cases rethrow exception
            else { throw exception; }
        }

    }

    class Cache<K, V> extends LinkedHashMap<K, V> {

        private final int maxCacheSize;

        Cache(int maxCacheSize) {
            // Default values for initial capacity and load factor
            super(16, 0.75F, true);
            this.maxCacheSize = maxCacheSize;

        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxCacheSize;
        }
    }
}
