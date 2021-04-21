package de.tum.bgu.msm.modules.timeOfDay;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.timeOfDay.AvailableTimeOfDay;
import de.tum.bgu.msm.data.timeOfDay.TimeOfDayDistribution;
import de.tum.bgu.msm.data.timeOfDay.TimeOfDayUtils;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public final class TimeOfDayChoice extends Module {

    private static final Logger logger = Logger.getLogger(TimeOfDayChoice.class);

    private EnumMap<Purpose, TimeOfDayDistribution> arrivalMinuteCumProbByPurpose;
    private EnumMap<Purpose, TimeOfDayDistribution> durationMinuteCumProbByPurpose;
    private EnumMap<Purpose, TimeOfDayDistribution> departureMinuteCumProbByPurpose;

    private final static double SPEED_WALK_MS = 5 / 3.6;
    private final static double SPEED_BICYCLE_MS = 13 / 3.6;

    private AtomicInteger counter = new AtomicInteger(0);
    private AtomicInteger issues = new AtomicInteger(0);
    private AtomicInteger unplausible = new AtomicInteger(0);

    public TimeOfDayChoice(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet, purposes);
    }

    @Override
    public void run() {
        arrivalMinuteCumProbByPurpose = dataSet.getArrivalMinuteCumProbByPurpose();
        durationMinuteCumProbByPurpose = dataSet.getDurationMinuteCumProbByPurpose();
        departureMinuteCumProbByPurpose = dataSet.getDepartureMinuteCumProbByPurpose();
        chooseDepartureTimes();
        logger.info("Time of day choice completed");

    }

    private void chooseDepartureTimes() {

        dataSet.getHouseholds().values().parallelStream().forEach(hh -> {
            for (MitoPerson person : hh.getPersons().values()) {
                AvailableTimeOfDay totalAvailableTOD = new AvailableTimeOfDay();
                AvailableTimeOfDay hbwAvailableTOD = new AvailableTimeOfDay();
                AvailableTimeOfDay nonHbwAvailableTOD = new AvailableTimeOfDay();
                Map<Purpose, List<MitoTrip>> tripsByPurpose = new HashMap<>();
                for (Purpose purpose : Purpose.getAllPurposes()) {
                    tripsByPurpose.putIfAbsent(purpose, new ArrayList<>());
                    List<MitoTrip> tripsPerPerson = hh.getTripsForPurpose(purpose).stream().filter(trip -> trip.getPerson().equals(person)).collect(Collectors.toList());
                    for (MitoTrip trip : tripsPerPerson) {
                        if (trip.getTripOrigin() != null && trip.getTripDestination() != null
                                && trip.getTripMode() != null) {
                            if (purposes.contains(purpose)){
                                //to be processed this time
                                if (trip.getTripPurpose().equals(purpose)) {

                                    tripsByPurpose.get(purpose).add(trip);
                                }
                            } else {
                                //was already processed?
                                if (trip.getDepartureInMinutes()>0 && trip.isHomeBased()){
                                    totalAvailableTOD.blockTime(trip.getDepartureInMinutes(),
                                            trip.getDepartureInMinutesReturnTrip() + estimateTravelTimeForTripInMinutes(trip));
                                    if (trip.getTripPurpose().equals(Purpose.HBW)){
                                        hbwAvailableTOD.blockTime(trip.getDepartureInMinutes(),
                                                trip.getDepartureInMinutesReturnTrip() + estimateTravelTimeForTripInMinutes(trip));
                                    } else {
                                        nonHbwAvailableTOD.blockTime(trip.getDepartureInMinutes(),
                                                trip.getDepartureInMinutesReturnTrip() + estimateTravelTimeForTripInMinutes(trip));
                                    }

                                }
                            }

                        } else {
                            //cannot complete the TOD choice since some info is missing
                            issues.incrementAndGet();
                        }
                    }
                }

                for (Purpose purpose : purposes.stream().filter(p -> isHomeBasedPurpose(p)).collect(Collectors.toList())) {
                    for (MitoTrip trip : tripsByPurpose.get(purpose)) {

                        int actDuration;
                        if (purpose == Purpose.HBW || purpose == Purpose.HBE) {
                            MitoOccupation occupation = trip.getPerson().getOccupation();
                            if (occupation != null) {
                                int endTime = occupation.getEndTime_min().orElseGet(() -> durationMinuteCumProbByPurpose.get(purpose).selectTime());
                                int startTime = occupation.getStartTime_min().orElse(0);
                                actDuration = endTime - startTime;
                            } else {
                                actDuration = durationMinuteCumProbByPurpose.get(trip.getTripPurpose()).selectTime();
                            }
                        } else {
                            actDuration = durationMinuteCumProbByPurpose.get(trip.getTripPurpose()).selectTime();
                        }

                        int travelTime = estimateTravelTimeForTripInMinutes(trip);
                        int tripDuration = actDuration + 2 * travelTime;

                        AvailableTimeOfDay availableTODNextTrip = TimeOfDayUtils.updateAvailableTimeForNextTrip(totalAvailableTOD, tripDuration);
                        TimeOfDayDistribution TODforThisTrip = TimeOfDayUtils.updateTODWithAvailability(arrivalMinuteCumProbByPurpose.get(purpose),
                                availableTODNextTrip);
                        int arrivalTime = TODforThisTrip.selectTime();
                        if (arrivalTime != -1){
                            int departureTime = arrivalTime - travelTime;
                            totalAvailableTOD.blockTime(departureTime, departureTime + tripDuration);
                            if (trip.getTripPurpose().equals(Purpose.HBW)){
                                hbwAvailableTOD.blockTime(departureTime, departureTime + tripDuration);
                            } else {
                                nonHbwAvailableTOD.blockTime(departureTime, departureTime + tripDuration);
                            }

                            int departureTimeReturn = arrivalTime + actDuration;
                            trip.setDepartureInMinutes(departureTime);
                            trip.setDepartureInMinutesReturnTrip(departureTimeReturn);
                            counter.incrementAndGet();
                            if (LongMath.isPowerOfTwo(counter.get())) {
                                logger.info(counter.get() + " times of day assigned");
                            }
                        } else {
                            unplausible.incrementAndGet();
                        }
                    }
                }

                for (Purpose purpose : purposes.stream().filter(p -> !isHomeBasedPurpose(p)).collect(Collectors.toList())) {
                    for (MitoTrip trip : tripsByPurpose.get(purpose)) {
                        AvailableTimeOfDay availableTODNextTrip;
                        if (purpose.equals(Purpose.AIRPORT)){
                            availableTODNextTrip = new AvailableTimeOfDay();
                            //todo needs further revision!
                        }else if (purpose.equals(Purpose.NHBW)) {
                            availableTODNextTrip = TimeOfDayUtils.convertToNonHomeBasedTrip(hbwAvailableTOD);
                        }else if (purpose.equals(Purpose.NHBO)){
                            availableTODNextTrip = TimeOfDayUtils.convertToNonHomeBasedTrip(nonHbwAvailableTOD);
                        } else {
                            throw new RuntimeException("Other purpose");
                        }

                        int departureTime = TimeOfDayUtils.updateTODWithAvailability(arrivalMinuteCumProbByPurpose.get(purpose),
                                availableTODNextTrip).selectTime();

                        if (departureTime != -1){
                            trip.setDepartureInMinutes(departureTime - estimateTravelTimeForTripInMinutes(trip));
                            counter.incrementAndGet();
                            if (LongMath.isPowerOfTwo(counter.get())) {
                                logger.info(counter.get() + " times of day assigned");
                            }
                        } else {
                            unplausible.incrementAndGet();
                        }
                    }
                }
            }
        });

        logger.warn(issues + " trips have no time of day since they have no origin, destination or mode");
        logger.warn(issues + " trips are not plausible due to absence of available time or absence of HB trips from which NHB trips can start");
    }


    private boolean isHomeBasedPurpose(Purpose p) {
        if (p.equals(Purpose.HBE) || p.equals(Purpose.HBW) || p.equals(Purpose.HBO) || p.equals(Purpose.HBR) || p.equals(Purpose.HBS)){
            return true;
        } else {
            return false;
        }

    }




    private int estimateTravelTimeForTripInMinutes(MitoTrip trip) {
        if (trip.getTripMode().equals(Mode.walk)) {
            return (int) (dataSet.getTravelDistancesNMT().
                    getTravelDistance(trip.getTripOrigin().getZoneId(), trip.getTripDestination().getZoneId()) / SPEED_WALK_MS * 60);
        } else if (trip.getTripMode().equals(Mode.bicycle)) {
            return (int) (dataSet.getTravelDistancesNMT().
                    getTravelDistance(trip.getTripOrigin().getZoneId(), trip.getTripDestination().getZoneId()) / SPEED_BICYCLE_MS * 60);
        } else {
            //both transit and car use here travel times by car
            return (int) (dataSet.getTravelTimes().getTravelTime(trip.getTripOrigin(), trip.getTripDestination(), 0, "car"));
        }
    }
}
