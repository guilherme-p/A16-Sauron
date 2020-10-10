package pt.tecnico.sauron.silo.service;

import pt.tecnico.sauron.silo.domain.*;
import pt.tecnico.sauron.silo.exceptions.*;
import pt.tecnico.sauron.silo.replication.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ReplicaService {

    private final StorageHandler storageHandler;
    private final QueryHandler queryHandler;
    private final GossipSender gossipSender;

    /* Updates already applied to the repositories */
    private final VectorTimestamp valueTimestamp;
    /* Updates that have been accepted (may not be stable) */
    private final VectorTimestamp replicaTimestamp;
    /* Replicas timestamps received from gossip messages */
    private final VectorTimestamp[] timestampTable;
    /* Contains all update operations received that are not stable (not yet applied) */
    private static final BlockingQueue<Update> updateLog = new PriorityBlockingQueue<>();
    /* Contains all stable and applied updates operations not confirmed by all other replicas */
    private static final Set<Update> unconfirmedOperations = ConcurrentHashMap.newKeySet();
    /* Executor service for the threads */
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    private final int replicaInstance;
    private final int gossipRate;
    private static boolean running = true;
    private static final Object updateLogMonitor = new Object();

    private static boolean setContext = false;
    private static boolean multipleReplicas;

    public ReplicaService(CameraRepository cameraRepository,
                          ObservationRepository<Long, Person> personRepository,
                          ObservationRepository<String, Car> carRepository,
                          VectorTimestamp valueTimestamp,
                          VectorTimestamp replicaTimestamp,
                          VectorTimestamp[] timestampTable,
                          int replicaInstance,
                          GossipSender gossipSender,
                          int gossipRate) {
        this.storageHandler = new StorageHandler(cameraRepository, personRepository, carRepository);
        this.queryHandler = new QueryHandler(cameraRepository, personRepository, carRepository);
        this.gossipSender = gossipSender;
        this.gossipRate = gossipRate;

        this.valueTimestamp = valueTimestamp;
        this.replicaTimestamp = replicaTimestamp;
        this.timestampTable = timestampTable;
        this.replicaInstance = replicaInstance;
        this.timestampTable[this.replicaInstance - 1] = this.replicaTimestamp;

        setContext();
    }

    /**
     * Method to set the context of all the threads.
     * All threads might wait but the method should only be called once
     */
    private void setContext() {
        synchronized (ReplicaService.class) {
            // Only the first thread will run the method operations
            if (!setContext) {
                // Starts threads
                executorService.execute(this::processLog);
                executorService.scheduleAtFixedRate(this::sendLog, gossipRate, gossipRate, TimeUnit.SECONDS);
                setContext = true;
                multipleReplicas = (replicaTimestamp.toList().size() > 1);
            }
        }
    }

    /**
     * Method to process the update log
     * Waits on new updates and processing them when the are stable
     */
    private void processLog() {
        // Process log forever
        while (running) {
            try {
                Update current = updateLog.take();
                synchronized (valueTimestamp) {
                    // Check if update is stable
                    if (current.getPrev().happensBefore(valueTimestamp)) {
                        // Executes the update
                        current.accept(storageHandler);
                        valueTimestamp.merge(current.getTimestamp());
                        // If multiple replicas save for later gossip
                        if (multipleReplicas) unconfirmedOperations.add(current);
                        // Notify threads that might be waiting for updates
                        synchronized (updateLogMonitor) {
                            updateLogMonitor.notifyAll();
                        }
                    } else {
                        // Most recent update cant be executed. Put back and wait for new updates.
                        updateLog.add(current);
                        valueTimestamp.wait();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("WARNING: Process log interrupted");
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Method to send the logs to other replicas
     */
    private void sendLog() {
        List<Update> updatesToSend;
        VectorTimestamp timestamp;

        // Copy timestamp so that if an update is received between the copy of updateLog
        // and sending the gossip, the sent replica timestamp is not updated
        synchronized (replicaTimestamp) {
            timestamp = VectorTimestamp.copyOf(replicaTimestamp);
            // copies updates to another list to release locks
            // for update log and unconfirmed operations while sending
            synchronized (updateLog) {
                updatesToSend = new ArrayList<>(updateLog);
            }
        }
        synchronized (unconfirmedOperations) {
            updatesToSend.addAll(unconfirmedOperations);
        }
        gossipSender.send(updatesToSend, timestamp, timestampTable);
    }

    /* Query Operations */

    /**
     * Info of camera's position
     * @param name of the camera
     * @return coordinates of the camera
     * @throws CameraInvalidArgumentsException if name is invalid
     * @throws CameraNotFoundException if there is no camera with that name
     * @throws MissingRequiredFieldException if a field is missing
     */
    public QueryResponse<CameraCoordinates> camInfo(String name)
            throws CameraInvalidArgumentsException, CameraNotFoundException, MissingRequiredFieldException {
        QueryResponse<CameraCoordinates> response = new QueryResponse<>();
        CameraCoordinates cameraCoordinates = queryHandler.camInfo(name);
        response.setData(cameraCoordinates);
        response.setNewTS(valueTimestamp);
        return response;
    }

    /**
     * Tracks a person by its id
     * @param id of the person
     * @return the person observation or null if none was found
     * @throws ObservationNotFoundException if no such person exists
     * @throws InvalidPersonIdException if the id is invalid
     * @throws MissingRequiredFieldException if the given param is null
     */
    public QueryResponse<Person> trackPerson(Long id)
            throws ObservationNotFoundException, InvalidPersonIdException, MissingRequiredFieldException {
        QueryResponse<Person> response = new QueryResponse<>();
        Person person = queryHandler.trackPerson(id);
        response.setData(person);
        response.setNewTS(valueTimestamp);
        return response;
    }

    /**
     * Tracks a car by its plate
     * @param plate of the car
     * @return the car observation or null if none was found
     * @throws ObservationNotFoundException if no such car exists
     * @throws InvalidCarPlateException if the plate is invalid
     * @throws MissingRequiredFieldException if the given param is null
     */
    public QueryResponse<Car> trackCar(String plate)
            throws ObservationNotFoundException, InvalidCarPlateException, MissingRequiredFieldException {
        QueryResponse<Car> response = new QueryResponse<>();
        Car car = queryHandler.trackCar(plate);
        response.setData(car);
        response.setNewTS(valueTimestamp);
        return response;
    }

    /**
     * Tracks all last person observations whose ids match
     * a regular expression
     * @param regex to match
     * @return a list of person observations with no specific order
     * @throws InvalidPersonIdRegexException if the id regex is invalid
     * @throws MissingRequiredFieldException if the given param is null
     */
    public QueryResponse<List<Person>> trackPersonMatch(String regex)
            throws InvalidPersonIdRegexException, MissingRequiredFieldException {
        QueryResponse<List<Person>> response = new QueryResponse<>();
        List<Person> people = queryHandler.trackPersonMatch(regex);
        response.setData(people);
        response.setNewTS(valueTimestamp);
        return response;
    }


    /**
     * Tracks all last car observations whose ids match
     * a regular expression
     * @param regex to match
     * @return a list of car observations with no specific order
     * @throws InvalidCarPlateRegexException if the plate regex is invalid
     * @throws MissingRequiredFieldException if the given param is null
     */
    public QueryResponse<List<Car>> trackCarMatch(String regex)
            throws InvalidCarPlateRegexException, MissingRequiredFieldException {
        QueryResponse<List<Car>> response = new QueryResponse<>();
        List<Car> cars = queryHandler.trackCarMatch(regex);
        response.setData(cars);
        response.setNewTS(valueTimestamp);
        return response;
    }

    /**
     * Traces the observations of a given person
     * @param id of the person
     * @return a list of person observations sorted by timestamp
     * @throws InvalidPersonIdException if the id is invalid
     * @throws MissingRequiredFieldException if the given param is null
     */
    public QueryResponse<List<Person>> tracePerson(Long id)
            throws InvalidPersonIdException, MissingRequiredFieldException {
        QueryResponse<List<Person>> response = new QueryResponse<>();
        List<Person> personList = queryHandler.tracePerson(id);
        response.setData(personList);
        response.setNewTS(valueTimestamp);
        return response;
    }

    /**
     * Traces the observations of a car
     * @param plate of the car
     * @return a list of car observations sorted by timestamp
     * @throws InvalidCarPlateException if the plate is invalid
     * @throws MissingRequiredFieldException if the given param is null
     */
    public QueryResponse<List<Car>> traceCar(String plate)
            throws InvalidCarPlateException, MissingRequiredFieldException {
        QueryResponse<List<Car>> response = new QueryResponse<>();
        List<Car> carList = queryHandler.traceCar(plate);
        response.setData(carList);
        response.setNewTS(valueTimestamp);
        return response;
    }

    /* Update Operations */

    /**
     * Registers a new camera
     * @param name of the camera
     * @param latitude of the camera's position
     * @param longitude of the camera's position
     * @param prev timestamp received from frontend
     * @return unique generated update id
     * @throws CameraInvalidArgumentsException if any of the arguments is invalid
     * @throws CameraAlreadyExistsException if already exists a camera with the same name
     * @throws MissingRequiredFieldException if a field is missing
     */
    public VectorTimestamp camJoin(String name, Double latitude, Double longitude, VectorTimestamp prev)
            throws CameraInvalidArgumentsException, CameraAlreadyExistsException, MissingRequiredFieldException {
        System.out.println(String.format("INFO: Received join from '%s'", name));
        storageHandler.checkCamJoin(name, latitude, longitude);
        replicaTimestamp.incrementReplicaInstanceValue(replicaInstance);
        VectorTimestamp ts = VectorTimestamp.copyOf(prev);
        ts.setReplicaInstanceValue(replicaInstance, replicaTimestamp.getReplicaInstanceValue(replicaInstance));
        // Submit operation for later execution
        updateLog.add(new CamJoin(name, latitude, longitude, replicaInstance, ts, prev));
        return ts;
    }

    /**
     * Reports camera observations
     * @param name of the camera
     * @param peopleIds ids of the people that were observed
     * @param carPlates plates of the cars that were observed
     * @param prev timestamp received from frontend
     * @return unique generated update id
     * @throws CameraNotFoundException if camera with name doesn't exist
     * @throws InvalidCarPlateException if a car plate is invalid
     * @throws InvalidPersonIdException if a person id is invalid
     * @throws MissingRequiredFieldException if a field is missing
     */
    public VectorTimestamp camReport(String name, List<Long> peopleIds, List<String> carPlates, VectorTimestamp prev)
            throws CameraInvalidArgumentsException, CameraNotFoundException, InvalidCarPlateException,
            InvalidPersonIdException, MissingRequiredFieldException {
        System.out.println(String.format("INFO: Received report from '%s'", name));
        storageHandler.checkCamReport(name, peopleIds, carPlates);
        replicaTimestamp.incrementReplicaInstanceValue(replicaInstance);
        VectorTimestamp ts = VectorTimestamp.copyOf(prev);
        ts.setReplicaInstanceValue(replicaInstance, replicaTimestamp.getReplicaInstanceValue(replicaInstance));
        // Submit operation for later execution
        updateLog.add(new CamReport(name, peopleIds, carPlates, Instant.now(), replicaInstance, ts, prev));
        return ts;
    }

    /**
     * Clears all cameras and observations from repository
     * @param prev timestamp received from frontend
     * @return unique generated update id
     */
    public VectorTimestamp clear(VectorTimestamp prev) {
        replicaTimestamp.incrementReplicaInstanceValue(replicaInstance);
        VectorTimestamp ts = VectorTimestamp.copyOf(prev);
        ts.setReplicaInstanceValue(replicaInstance, replicaTimestamp.getReplicaInstanceValue(replicaInstance));
        // Submit operation for later execution
        updateLog.add(new Clear(replicaInstance, ts, prev));
        return ts;
    }

    /**
     * Initializes the service repositories with the given entries
     * @param cameras to add
     * @param people to add
     * @param cars to add
     * @param prev timestamp received from frontend
     * @return unique generated update id
     * @throws InvalidPersonIdException if a person id is invalid
     * @throws InvalidCarPlateException if a car plate is invalid
     * @throws CameraNotFoundException if an observation's camera is not in the cameras to add
     * @throws CameraInvalidArgumentsException if a camera argument is invalid
     * @throws CameraAlreadyExistsException if a camera already exists in the repository
     * @throws MissingRequiredFieldException if a field is missing
     */
    public VectorTimestamp init(List<Camera> cameras, List<Person> people, List<Car> cars, VectorTimestamp prev)
            throws InvalidPersonIdException, InvalidCarPlateException, CameraNotFoundException,
            CameraInvalidArgumentsException, MissingRequiredFieldException, CameraAlreadyExistsException {
        storageHandler.checkInit(cameras, people, cars);
        replicaTimestamp.incrementReplicaInstanceValue(replicaInstance);
        VectorTimestamp ts = VectorTimestamp.copyOf(prev);
        ts.setReplicaInstanceValue(replicaInstance, replicaTimestamp.getReplicaInstanceValue(replicaInstance));
        // Submit operation for later execution
        updateLog.add(new Init(cameras, people, cars, replicaInstance, ts, prev));
        return ts;
    }

    /**
     * Updates the service with the other replica log
     * @param updates to add to the log
     * @param timestamp of the gossip message
     * @param otherReplica replica that sent the message
     */
    public void gossip(List<Update> updates, VectorTimestamp timestamp, int otherReplica) {
        System.out.println(String.format("INFO: Received %d updates from replica %d with timestamp '%s': %s ",
                                        updates.size(), otherReplica, timestamp,  updates));
        // Merge update log
        synchronized (updateLog) {
            updateLog.addAll(updates.stream()
                    .filter(update -> !update.getTimestamp().happensBefore(replicaTimestamp))
                    .collect(Collectors.toList()));
            replicaTimestamp.merge(timestamp);
            // No need to apply stable updates, thread that is always executing

            // Update saved timestamp for other replica
            timestampTable[otherReplica - 1] = timestamp;
        }
        // Eliminate from unconfirmed operations updates that were applied everywhere
        synchronized (unconfirmedOperations) {
            unconfirmedOperations.removeIf(this::updateReceivedInAllReplicas);
        }
        // Notify for new updates if thread is waiting
        synchronized (valueTimestamp) {
            valueTimestamp.notifyAll();
        }
    }

    private boolean updateReceivedInAllReplicas(Update update) {
        int c = update.getReplicaInstance();
        for (VectorTimestamp vectorTimestamp : timestampTable) {
            if (vectorTimestamp.getReplicaInstanceValue(c) < update.getTimestamp().getReplicaInstanceValue(c)) {
                return false;
            }
        }
        return true;
    }

}
