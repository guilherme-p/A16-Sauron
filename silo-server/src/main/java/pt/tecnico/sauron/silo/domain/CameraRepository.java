package pt.tecnico.sauron.silo.domain;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for Cameras. Offers basic storage and
 * search operations for Cameras
 */
public class CameraRepository {

    private ConcurrentHashMap<String, Camera> repository = new ConcurrentHashMap<>();
    // Lock for methods with multiple operations on the repository
    private final Object lock = new Object();

    /**
     * Finds a camera by its name
     * @param name of the camera to find
     * @return optional with camera if it exists or empty
     * if no camera with the given name was found found
     */
    public Optional<Camera> find(String name) {
        // No need for lock because get is synchronized
        return Optional.ofNullable(repository.get(name));
    }

    /**
     * Adds a new camera to the repository. Only possible to add a new camera
     * with a new name or if the name exists with same latitude and longitude.
     * @param camera to add to the repository
     * @return true if the camera was added or a camera with the same name, latitude
     * and longitude already exists and false otherwise
     */
    public boolean join(Camera camera) {
        Camera other;
        synchronized (lock) {
            other = repository.get(camera.getName());
            if (other == null) {
                repository.put(camera.getName(), camera);
                return true;
            }
        }
        return other.getLatitude().equals(camera.getLatitude())
                && other.getLongitude().equals(camera.getLongitude());
    }

    /**
     * Checks if the repository contains a camera with the given name
     * @param name of the camera
     * @return true if camera if name exists, false otherwise
     */
    public boolean exists(String name) {
        return repository.containsKey(name);
    }

    /**
     * Clears all the cameras from repository
     */
    public void clear() {
        repository.clear();
    }

}
