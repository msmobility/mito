package de.tum.bgu.msm.data.timeOfDay;


public class TimeOfDayUtils {
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
        for (int minute = 10; minute < 24*60; minute = minute + 10) {
            if (baseAvailableTOD.isAvailable(minute) == 0 && baseAvailableTOD.isAvailable(minute - 10) == 1){
                newAvailableTOD.blockTime(minute, Math.max(0, minute - tripDuration));

            }
        }
        return newAvailableTOD;
    }

    public static AvailableTimeOfDay convertToNonHomeBasedTrip(AvailableTimeOfDay baseAvailableTOD) {
        AvailableTimeOfDay newAvailableTOD = new AvailableTimeOfDay();
        for (int minute : baseAvailableTOD.getMinutes()) {
            if (baseAvailableTOD.isAvailable(minute) == 1){
                newAvailableTOD.blockTime(minute, minute);
            }
        }
        return newAvailableTOD;
    }
}
