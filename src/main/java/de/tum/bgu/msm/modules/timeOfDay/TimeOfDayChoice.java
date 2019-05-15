package de.tum.bgu.msm.modules.timeOfDay;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.readers.TimeOfDayDistributionsReader;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.EnumMap;


public class TimeOfDayChoice extends Module {

    private static final Logger logger = Logger.getLogger(TimeOfDayChoice.class);

    private EnumMap<Purpose, DoubleMatrix1D> arrivalMinuteCumProbByPurpose;
    private EnumMap<Purpose, DoubleMatrix1D> durationMinuteCumProbByPurpose;

    private double speedWalk = 5 / 3.6;
    private double speedBicycle = 13 / 3.6;

    private long counter = 0;
    private int issues = 0;

    public TimeOfDayChoice(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        loadProbabilities();
        chooseDepartureTimes();
        logger.info("Time of day choice completed");

    }

    private void loadProbabilities() {
        TimeOfDayDistributionsReader reader = new TimeOfDayDistributionsReader(dataSet);
        reader.read();
        arrivalMinuteCumProbByPurpose = reader.getArrivalMinuteCumProbByPurpose();
        durationMinuteCumProbByPurpose = reader.getDurationMinuteCumProbByPurpose();

    }


    private void chooseDepartureTimes() {

        dataSet.getTrips().values().forEach(trip -> {
                    try {
                        int arrivalTimeInMinutes = chooseArrivalTime(trip);
                        int departureTimeInMinutes = arrivalTimeInMinutes - (int) estimateTravelTimeForDeparture(trip, arrivalTimeInMinutes);
                        //if departure is before midnight
                        if (departureTimeInMinutes < 0) {
                            departureTimeInMinutes = departureTimeInMinutes + 24 * 60;
                        }
                        trip.setDepartureInMinutes(departureTimeInMinutes);
                        if (trip.isHomeBased()) {
                            trip.setDepartureInMinutesReturnTrip(chooseDepartureTimeForReturnTrip(trip, departureTimeInMinutes));
                        }

                    } catch (Exception e) {
                        issues++;
                    }
                    counter++;
                    if (LongMath.isPowerOfTwo(counter)) {
                        logger.info(counter + " times of day assigned");
                    }
                }
        );
        logger.warn(issues  + " trips have no time of day since they have no origin, destination or mode" );
    }


    private int chooseArrivalTime(MitoTrip mitoTrip) {
        return MitoUtil.select(arrivalMinuteCumProbByPurpose.get(mitoTrip.getTripPurpose()).toArray(), MitoUtil.getRandomObject());
    }

    private int chooseDepartureTimeForReturnTrip(MitoTrip mitoTrip, int arrivalTime) {

        //if departure is after midnight
        int duration = MitoUtil.select(durationMinuteCumProbByPurpose.get(mitoTrip.getTripPurpose()).toArray(), MitoUtil.getRandomObject());
        if (arrivalTime + duration > 24 * 60) {
            return arrivalTime + duration - 24 * 60;
        } else {
            return arrivalTime + duration;
        }
    }

    private double estimateTravelTimeForDeparture(MitoTrip trip, double arrivalInMinutes) {
        if (trip.getTripMode().equals(Mode.walk)) {
            return dataSet.getTravelDistancesNMT().getTravelDistance(trip.getTripOrigin().getZoneId(), trip.getTripDestination().getZoneId()) / speedWalk;
        } else if (trip.getTripMode().equals(Mode.bicycle)) {
            return dataSet.getTravelDistancesNMT().getTravelDistance(trip.getTripOrigin().getZoneId(), trip.getTripDestination().getZoneId()) / speedBicycle;
        } else {
            //both transit and car use here travel times by car
            return dataSet.getTravelTimes().getTravelTime(trip.getTripOrigin(), trip.getTripDestination(), arrivalInMinutes * 60, "car");
        }
    }
}
