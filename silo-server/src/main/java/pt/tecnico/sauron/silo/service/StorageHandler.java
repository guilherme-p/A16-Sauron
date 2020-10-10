package pt.tecnico.sauron.silo.service;

import pt.tecnico.sauron.silo.domain.*;
import pt.tecnico.sauron.silo.exceptions.*;
import pt.tecnico.sauron.silo.replication.*;
import pt.tecnico.sauron.silo.utils.CheckUtils;

import java.time.Instant;
import java.util.*;

import static pt.tecnico.sauron.silo.service.DomainFields.*;

/**
 * Class responsible to execute the clients updates
 */
public class StorageHandler implements UpdateVisitor {

    private final CameraRepository cameraRepository;
    private final ObservationRepository<Long, Person> personRepository;
    private final ObservationRepository<String, Car> carRepository;

    public StorageHandler(CameraRepository cameraRepository,
                          ObservationRepository<Long, Person> personRepository,
                          ObservationRepository<String, Car> carRepository) {
        this.cameraRepository = cameraRepository;
        this.personRepository = personRepository;
        this.carRepository = carRepository;
    }

    /**
     * Registers a new camera
     * @param camJoin operation to execute
     */
    @Override
    public void visit(CamJoin camJoin) {
        Camera camera = new Camera(camJoin.getName(), camJoin.getLatitude(), camJoin.getLongitude());
        if (!cameraRepository.join(camera)) {
            System.out.println(String.format("SEVERE: Camera with duplicate name submitted: '%s'.", camJoin.getName()));
        }
    }

    /**
     * Reports camera observations
     * @param camReport operation to execute
     */
    @Override
    public void visit(CamReport camReport) {
        Camera camera = cameraRepository.find(camReport.getName())
                .orElse(null);
        // Check if camera is already registered
        // Should never happen because because updates can only be submitted to a replica
        // that already registered the camera
        if (camera == null) {
            System.out.println(String.format("SEVERE: Report with unknown camera: '%s'.", camReport.getName()));
            return;
        }
        // Save the report instant for all observations
        Instant instant = camReport.getInstant();
        List<Person> people = new ArrayList<>(camReport.getPeopleIds().size());
        camReport.getPeopleIds().forEach(id -> people.add(new Person(id, instant, camera)));

        List<Car> cars = new ArrayList<>(camReport.getCarPlates().size());
        camReport.getCarPlates().forEach(plate -> cars.add(new Car(plate, instant, camera)));

        // Safe to report all the observations
        cars.forEach(carRepository::report);
        people.forEach(personRepository::report);
    }

    /**
     * Clears all cameras and observations from repository
     * @param clear operation to execute
     */
    @Override
    public void visit(Clear clear) {
        clear();
    }

    /**
     * Initializes the service repositories with the given entries
     * @param init operation to execute
     */
    @Override
    public void visit(Init init) {
        // No updates can be done to the repositories while init is running
        synchronized (this) {
            clear();
            init.getCameras().forEach(cameraRepository::join);
            init.getPeople().forEach(personRepository::report);
            init.getCars().forEach(carRepository::report);
        }
    }

    /**
     * Checks if the given arguments are well formatted for a camera join
     * @param name name of the camera
     * @param latitude latitude of the camera
     * @param longitude longitude of the camera
     * @throws CameraInvalidArgumentsException if an parameter format is wrong
     * @throws MissingRequiredFieldException if a parameter is missing
     */
    public void checkCamJoin(String name, Double latitude, Double longitude)
            throws CameraAlreadyExistsException, CameraInvalidArgumentsException,
            MissingRequiredFieldException {
        checkCamera(name, latitude, longitude);
        Camera camera = cameraRepository.find(name).orElse(null);
        // If existing camera has the different coordinates then the new camera is duplicate
        if (camera != null &&
            !(camera.getLatitude().equals(latitude) && camera.getLongitude().equals(longitude))) {
            throw new CameraAlreadyExistsException();
        }
    }

    /**
     * Checks if the given arguments are well formatted for a camera report
     * @param name if the camera
     * @param peopleIds ids of the reported people
     * @param carPlates plates of the reported cars
     * @throws CameraInvalidArgumentsException if a camera parameter is invalid
     * @throws InvalidPersonIdException if a person id is invalid
     * @throws InvalidCarPlateException if a car plate is invalid
     * @throws MissingRequiredFieldException if a parameter is missing
     */
    public void checkCamReport(String name, List<Long> peopleIds, List<String> carPlates)
            throws CameraNotFoundException, CameraInvalidArgumentsException,
            InvalidPersonIdException, InvalidCarPlateException, MissingRequiredFieldException {
        CheckUtils.checkCameraName(CheckUtils.notNull(name, CAMERA_NAME));
        // If camera does not exist the update can not be submitted
        // The cameras must join before reporting
        if (!cameraRepository.exists(name)) {
            throw new CameraNotFoundException(name);
        }
        for (Long id : peopleIds) {
            CheckUtils.checkPersonId(CheckUtils.notNull(id, PERSON_ID));
        }
        for (String plate : carPlates) {
            CheckUtils.checkCarPlate(CheckUtils.notNull(plate, CAR_PLATE));
        }
    }

    /**
     * Checks if the given arguments are well formatted for a init
     * @param cameras cameras to add
     * @param people people to add
     * @param cars cars to add
     * @throws CameraInvalidArgumentsException if a camera parameter is invalid
     * @throws InvalidPersonIdException if a person id is invalid
     * @throws InvalidCarPlateException if a car plate is invalid
     * @throws CameraAlreadyExistsException if a camera is duplicate
     * @throws CameraNotFoundException if an observation's camera is not in the cameras to add
     * @throws MissingRequiredFieldException if a parameter is missing
     */
    public void checkInit(List<Camera> cameras, List<Person> people, List<Car> cars)
            throws CameraInvalidArgumentsException, InvalidPersonIdException, InvalidCarPlateException,
            CameraAlreadyExistsException, CameraNotFoundException, MissingRequiredFieldException {
        // Received cameras for checking existence in later observations
        Map<String, Camera> receivedCameras = new HashMap<>();
        for (Camera camera : cameras) {
            checkCamera(camera);
            if (receivedCameras.containsKey(camera.getName())) {
                throw new CameraAlreadyExistsException();
            }
            else {
                receivedCameras.put(camera.getName(), camera);
            }
        }
        for (Person person : people) {
            checkCamera(CheckUtils.notNull(person.getCamera(), OBSERVATION_CAMERA));
            CheckUtils.checkPersonId(CheckUtils.notNull(person.getId(), PERSON_ID));
            if (!receivedCameras.containsKey(person.getCamera().getName())) {
                throw new CameraNotFoundException(person.getCamera().getName());
            }
        }
        for (Car car : cars) {
            checkCamera(CheckUtils.notNull(car.getCamera(), OBSERVATION_CAMERA));
            CheckUtils.checkCarPlate(CheckUtils.notNull(car.getId(), CAR_PLATE));
            if (!receivedCameras.containsKey(car.getCamera().getName())) {
                throw new CameraNotFoundException(car.getCamera().getName());
            }
        }
    }

    /* Clears all cameras and observations from repository */
    private void clear() {
        cameraRepository.clear();
        personRepository.clear();
        carRepository.clear();
    }

    /* Auxiliary functions to check params */

    private void checkCamera(Camera camera)
            throws CameraInvalidArgumentsException, MissingRequiredFieldException {
        checkCamera(camera.getName(), camera.getLatitude(), camera.getLongitude());
    }

    private void checkCamera(String name, Double latitude, Double longitude)
            throws CameraInvalidArgumentsException, MissingRequiredFieldException {
        CheckUtils.checkCameraName(CheckUtils.notNull(name, CAMERA_NAME));
        CheckUtils.checkLatitude(CheckUtils.notNull(latitude, CAMERA_LATITUDE));
        CheckUtils.checkLongitude(CheckUtils.notNull(longitude, CAMERA_LONGITUDE));
    }
}
