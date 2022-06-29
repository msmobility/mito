package de.tum.bgu.msm.data.timeOfDay;


public class TimeOfDayUtils {

    private static final int SEARCH_INTERVAL_MIN = 5;
    private static final int MAP_SIZE = 48 * 60;


    /**
     * Converts the departure time distribution to a modified version of it where non-available time windows have a probability of zero
     * @param originalTOD
     * @param availableTOD
     * @return
     */
    public static TimeOfDayDistribution updateTODWithAvailability(TimeOfDayDistribution originalTOD,
                                                                  AvailableTimeOfDay availableTOD) {

        TimeOfDayDistribution newTOD = new TimeOfDayDistribution();
        for (int minute : originalTOD.getMinutes()) {
            newTOD.setProbability(minute, originalTOD.probability(minute) * (double) availableTOD.isAvailable(minute));
        }
        return newTOD;
    }


    /**
     * Converts the available time object to a new one that can accomodate a trip of certain duration. It avoids starting a trip that will overlap
     * with an already existing one
     * @param baseAvailableTOD
     * @param tripDuration
     * @return
     */
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

    /**
     * Returns the negative of the availability object to put non-home based trips only when home base trips are happening
     * @param baseAvailableTOD
     * @return
     */
    public static AvailableTimeOfDay convertToNonHomeBasedTrip(AvailableTimeOfDay baseAvailableTOD) {
        AvailableTimeOfDay newAvailableTOD = new AvailableTimeOfDay();
        for (int minute : baseAvailableTOD.getMinutes()) {
            if (baseAvailableTOD.isAvailable(minute) == 1){
                newAvailableTOD.blockTime(minute - SEARCH_INTERVAL_MIN, minute);
            }
        }
        return newAvailableTOD;
    }

    public static AvailableTimeOfDay updateAvailableTimeToAvoidTooLateTermination(AvailableTimeOfDay availableTODNextTrip, int tripDuration) {
        for (int minute : availableTODNextTrip.getMinutes()) {
            if (minute + tripDuration > 26 * 60 ){
                availableTODNextTrip.blockTime(minute - SEARCH_INTERVAL_MIN, minute);
            } else {
               //do nothing
            }

        }
        return availableTODNextTrip;
    }
}
