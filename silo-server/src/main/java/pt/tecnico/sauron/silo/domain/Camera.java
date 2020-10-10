package pt.tecnico.sauron.silo.domain;

public class Camera {

    private String name;
    private CameraCoordinates coordinates;

    public Camera(String name, Double latitude, Double longitude) {
        this.name = name;
        this.coordinates = new CameraCoordinates(latitude, longitude);
    }

    public String getName() {
        return name;
    }

    public CameraCoordinates getCoordinates() {
        return coordinates;
    }

    public Double getLatitude() {
        return coordinates.getLatitude();
    }

    public Double getLongitude() {
        return coordinates.getLongitude();
    }

    @Override
    public String toString() {
        return "Camera{" +
                "name='" + name + '\'' +
                ", coordinates=" + coordinates +
                '}';
    }
}
