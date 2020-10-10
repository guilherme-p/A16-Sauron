package pt.tecnico.sauron.silo.service;

import pt.tecnico.sauron.silo.domain.*;
import pt.tecnico.sauron.silo.exceptions.*;
import pt.tecnico.sauron.silo.utils.CheckUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static pt.tecnico.sauron.silo.service.DomainFields.*;

/**
 * Class responsible to execute the clients queries
 */
public class QueryHandler {

    /* Comparator to sort observations by decreasing timestamp */
    private static final Comparator<SavedObservation<?>> TIMESTAMP_COMPARATOR =
            (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp());

    private final CameraRepository cameraRepository;
    private final ObservationRepository<Long, Person> personRepository;
    private final ObservationRepository<String, Car> carRepository;

    public QueryHandler(CameraRepository cameraRepository,
                          ObservationRepository<Long, Person> personRepository,
                          ObservationRepository<String, Car> carRepository) {
        this.cameraRepository = cameraRepository;
        this.personRepository = personRepository;
        this.carRepository = carRepository;
    }

    /**
     * Info of camera's position
     * @param name of the camera
     * @return the camera coordinates to send
     * @throws CameraInvalidArgumentsException if the name is invalid
     * @throws CameraNotFoundException if the name does not exist
     * @throws MissingRequiredFieldException if the name is null
     */
    public CameraCoordinates camInfo(String name)
            throws CameraInvalidArgumentsException, CameraNotFoundException, MissingRequiredFieldException {
        String checkedName = CheckUtils.checkCameraName(CheckUtils.notNull(name, CAMERA_NAME));
        Camera camera = cameraRepository.find(checkedName)
                .orElseThrow(() -> new CameraNotFoundException(checkedName));
        return camera.getCoordinates();
    }

    /**
     * Tracks a person by its id
     * @param id of the person
     * @return the person with the given id
     * @throws ObservationNotFoundException if no such person exists
     * @throws InvalidPersonIdException if the id is invalid
     * @throws MissingRequiredFieldException if the id is null
     */
    public Person trackPerson(Long id)
            throws ObservationNotFoundException, InvalidPersonIdException, MissingRequiredFieldException {
        Long checkedId = CheckUtils.checkPersonId(CheckUtils.notNull(id, PERSON_ID));
        return personRepository.track(checkedId)
                .orElseThrow(() -> new PersonNotFoundException(checkedId));
    }

    /**
     * Tracks a car by its plate
     * @param plate of the car
     * @return the car with the given plate
     * @throws ObservationNotFoundException if no such car exists
     * @throws InvalidCarPlateException if the car plate is invalid
     * @throws MissingRequiredFieldException if the car plate is null
     */
    public Car trackCar(String plate)
            throws ObservationNotFoundException, InvalidCarPlateException, MissingRequiredFieldException {
        String checkedPlate = CheckUtils.checkCarPlate(CheckUtils.notNullOrBlank(plate, CAR_PLATE));
        return carRepository.track(checkedPlate)
                .orElseThrow(() -> new CarNotFoundException(plate));
    }

    /**
     * Tracks all last person observations whose ids match a regular expression
     * @param regex to match
     * @return the list of person whose ids match the regex
     * @throws InvalidPersonIdRegexException if the regex is invalid
     * @throws MissingRequiredFieldException if the regex is null
     */
    public List<Person> trackPersonMatch(String regex)
            throws InvalidPersonIdRegexException, MissingRequiredFieldException {
        String checkedRegex = CheckUtils.checkPersonIdRegex(CheckUtils.notNullOrBlank(regex, PERSON_SEARCH_REGEX));
        return personRepository.match(transformRegex(checkedRegex))
                .collect(Collectors.toList());
    }


    /**
     * Tracks all last car observations whose ids match a regular expression
     * @param regex to match
     * @return the list of car whose ids match the regex
     * @throws InvalidCarPlateRegexException if the regex is invalid
     * @throws MissingRequiredFieldException if the regex is null
     */
    public List<Car> trackCarMatch(String regex)
            throws InvalidCarPlateRegexException, MissingRequiredFieldException {
        String checkedRegex = CheckUtils.checkCarPlateRegex(CheckUtils.notNullOrBlank(regex, CAR_SEARCH_REGEX));
        return carRepository.match(transformRegex(checkedRegex))
                .collect(Collectors.toList());
    }

    /**
     * Traces the observations of a given person
     * @param id of the person
     * @return the list of that person observations
     * @throws InvalidPersonIdException if the person id is invalid
     * @throws MissingRequiredFieldException if the person id is null
     */
    public List<Person> tracePerson(Long id)
            throws InvalidPersonIdException, MissingRequiredFieldException {
        Long checkedId = CheckUtils.checkPersonId(CheckUtils.notNull(id, PERSON_ID));
        return personRepository.trace(checkedId)
                .sorted(TIMESTAMP_COMPARATOR)
                .collect(Collectors.toList());
    }

    /**
     * Traces the observations of a car
     * @param plate of the car
     * @return the list of that car observations
     * @throws InvalidCarPlateException if the car plate is invalid
     * @throws MissingRequiredFieldException if the car plate is null
     */
    public List<Car> traceCar(String plate)
            throws InvalidCarPlateException, MissingRequiredFieldException {
        String checkedPlate = CheckUtils.checkCarPlate(CheckUtils.notNullOrBlank(plate, CAR_PLATE));
        return carRepository.trace(checkedPlate)
                .sorted(TIMESTAMP_COMPARATOR)
                .collect(Collectors.toList());
    }

    /* Auxiliary method to parse received regex */

    private String transformRegex(String regex) {
        return regex.replace("*", ".*");
    }
}
