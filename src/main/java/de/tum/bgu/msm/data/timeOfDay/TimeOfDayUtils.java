package de.tum.bgu.msm.data.timeOfDay;


public class TimeOfDayUtils {

    private static final int SEARCH_INTERVAL_MIN = 5;
    private static final int MAP_SIZE = 48 * 60;

    public static TimeOfDayDistribution updateTODWithAvailability(TimeOfDayDistribution originalTOD,
                                                                  AvailableTimeOfDay availableTOD) {

        TimeOfDayDistribution newTOD = new TimeOfDayDistribution();
        for (int minute : originalTOD.getMinutes()) {
            newTOD.setProbability(minute, originalTOD.probability(minute) * (double) availableTOD.isAvailable(minute));
        }
        return newTOD;
    }

    public static AvailableTimeOfDay updateAvailableTimeForNextTrip(AvailableTimeOfDay baseAvailableTOD, int tripDuration) {

        AvailableTimeOfDay newAvailableTOD = new AvailableTimeOfDay();

        for (int minute = SEARCH_INTERVAL_MIN; minute < MAP_SIZE; minute = minute + SEARCH_INTERVAL_MIN) {
            if (baseAvailableTOD.isAvailable(minute) == 0 && baseAvailableTOD.isAvailable(minute - SEARCH_INTERVAL_MIN) == 1){
                newAvailableTOD.blockTime(Math.max(0, minute - tripDuration), minute);
            } else if (baseAvailableTOD.isAvailable(minute) == 0) {
                newAvailableTOD.blockTime(minute - SEARCH_INTERVAL_MIN, minute);
            }
        }
        return newAvailableTOD;
    }

    public static AvailableTimeOfDay convertToNonHomeBasedTrip(AvailableTimeOfDay baseAvailableTOD) {
        AvailableTimeOfDay newAvailableTOD = new AvailableTimeOfDay();
        for (int minute : baseAvailableTOD.getMinutes()) {
            if (baseAvailableTOD.isAvailable(minute) == 1){
                newAvailableTOD.blockTime(minute-1, minute);
            }
        }
        return newAvailableTOD;
    }
}
