package de.tum.bgu.msm.modules.timeOfDay;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.io.input.readers.TimeOfDayDistributionsReader;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;


public class TimeOfDayChoice extends Module {


    private static final Logger logger = Logger.getLogger(TimeOfDayChoice.class);

    private DoubleMatrix2D arrivalMinuteCumProbByPurpose;
    private DoubleMatrix2D durationMinuteCumProvByPurpose;

    private double speedWalk = 5 / 3.6;
    private double speedBicycle = 13 / 3.6;

    private long counter = 0;

    public TimeOfDayChoice(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        loadProbabilities();
        chooseDepartureTimes();
        logger.info("Time of day choice completed");

    }

    void loadProbabilities() {
        TimeOfDayDistributionsReader reader = new TimeOfDayDistributionsReader(dataSet);
        reader.read();
        arrivalMinuteCumProbByPurpose = reader.getArrivalMinuteCumProbByPurpose();
        durationMinuteCumProvByPurpose = reader.getDurationMinuteCumProvByPurpose();
    }


    void chooseDepartureTimes() {
        dataSet.getTrips().values().forEach(trip -> {
                try {
                        int arrivalInMinutes = chooseDepartureTime(trip);
                        arrivalInMinutes = arrivalInMinutes - (int) estimateTravelTimeForDeparture(trip, arrivalInMinutes);
                        //if departure is before midnight
                        if (arrivalInMinutes < 0) {
                            arrivalInMinutes = arrivalInMinutes + 24 * 60;
                        }
                        trip.setDepartureInMinutes(arrivalInMinutes);
                        if (trip.isHomeBased()) {
                            trip.setDepartureInMinutesReturnTrip(chooseDepartureTimeForReturnTrip(trip, arrivalInMinutes));
                        }

                    } catch (Exception e) {
                }
                    counter++;
                    if (LongMath.isPowerOfTwo(counter)){
                        logger.info(counter + " times of day assigned");
                    }
                }
        );
    }




    int chooseDepartureTime(MitoTrip mitoTrip) {
        return MitoUtil.select(arrivalMinuteCumProbByPurpose.viewColumn(mitoTrip.getTripPurpose().ordinal()).toArray(), MitoUtil.getRandomObject());
    }

    int chooseDepartureTimeForReturnTrip(MitoTrip mitoTrip, int arrivalTime) {

        //if departure is after midnight
        int duration = MitoUtil.select(durationMinuteCumProvByPurpose.viewColumn(mitoTrip.getTripPurpose().ordinal()).toArray(), MitoUtil.getRandomObject());
        if (arrivalTime + duration > 24 * 60) {
            return arrivalTime + duration - 24 * 60;
        } else {
            return arrivalTime + duration;
        }
    }

    public double estimateTravelTimeForDeparture(MitoTrip trip, double arrivalInMinutes) {
        if (trip.getTripMode().equals(Mode.walk)) {
            return dataSet.getTravelDistancesNMT().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId()) / speedWalk;
        } else if (trip.getTripMode().equals(Mode.bicycle)) {
            return dataSet.getTravelDistancesNMT().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId()) / speedBicycle;
        } else {
            //both transit and car use here travel times by car
            return dataSet.getTravelTimes().getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), arrivalInMinutes * 60, "car");
        }
    }


}
