package pt.tecnico.sauron.silo.utils;

import pt.tecnico.sauron.silo.exceptions.*;

import java.util.Optional;

public class CheckUtils {

    private CheckUtils() {}

    public static <T> T notNull(T field, String fieldName)
            throws MissingRequiredFieldException {
        return Optional.of(field)
                .orElseThrow(() -> new MissingRequiredFieldException(fieldName));
    }

    public static String notNullOrBlank(String field, String fieldName)
            throws MissingRequiredFieldException {
        return Optional.of(field).filter(s -> !s.isBlank())
                .orElseThrow(() -> new MissingRequiredFieldException(fieldName));
    }

    public static String checkCameraName(String name)
            throws CameraInvalidArgumentsException {
        return Optional.of(name).filter(n -> n.length() >= 3 && n.length() <= 15 && n.matches("[a-zA-Z\\d]+"))
                .orElseThrow(() -> new CameraInvalidArgumentsException(CameraInvalidArgumentsException.INVALID_CAMERA_NAME));
    }

    public static Double checkLatitude(Double latitude)
            throws CameraInvalidArgumentsException {
        return Optional.of(latitude).filter(d -> d >= -90 && d <= 90)
                .orElseThrow(() -> new CameraInvalidArgumentsException(CameraInvalidArgumentsException.INVALID_LATITUDE));
    }

    public static Double checkLongitude(Double longitude)
            throws CameraInvalidArgumentsException {
        return Optional.of(longitude).filter(d -> d >= -180 && d <= 180)
                .orElseThrow(() -> new CameraInvalidArgumentsException(CameraInvalidArgumentsException.INVALID_LONGITUDE));
    }

    public static Long checkPersonId(Long id)
            throws InvalidPersonIdException {
        return Optional.of(id).filter(num -> num > 0)
                .orElseThrow(() -> new InvalidPersonIdException(id));
    }

    public static String checkCarPlate(String plate)
            throws InvalidCarPlateException {
        return Optional.of(plate).filter(CheckUtils::checkPlate)
                .orElseThrow(() -> new InvalidCarPlateException(plate));
    }

    public static String checkPersonIdRegex(String regex)
            throws InvalidPersonIdRegexException {
        return Optional.of(regex).filter(ex -> ex.matches("[\\d*]+"))
                .orElseThrow(() -> new InvalidPersonIdRegexException(regex));
    }

    public static String checkCarPlateRegex(String regex)
            throws InvalidCarPlateRegexException {
        return Optional.of(regex).filter(ex -> ex.length() > 0 && countAlphaNumeric(ex) <= 6 && ex.matches("[\\dA-Z*]+"))
                .orElseThrow(() -> new InvalidCarPlateRegexException(regex));
    }

    private static boolean checkPlate(String plate) {
        return plate.length() == 6 && checkGroups(plate);
    }

    private static boolean checkGroups(String plate) {
        String g1 = plate.substring(0, 2);
        String g2 = plate.substring(2, 4);
        String g3 = plate.substring(4, 6);

        String[] groups = {g1, g2, g3};

        int digitGroups = 0;
        int letterGroups = 0;

        for (String g : groups) {
            digitGroups += isDigitGroup(g) ? 1 : 0;
            letterGroups += isLetterGroup(g) ? 1 : 0;
        }

        return (digitGroups == 1 && letterGroups == 2) || (digitGroups == 2 && letterGroups == 1);
    }

    private static boolean isDigitGroup(String g) {
        return g.matches("\\d{2}");
    }

    private static boolean isLetterGroup(String g) {
        return g.matches("[A-Z]{2}");
    }

    private static long countAlphaNumeric(String param) {
        return param.chars()
                .filter(ch -> Character.isUpperCase(ch) || Character.isDigit(ch))
                .count();
    }
}
