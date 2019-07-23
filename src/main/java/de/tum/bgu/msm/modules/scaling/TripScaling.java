package de.tum.bgu.msm.modules.scaling;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class TripScaling extends Module {

    private Map<Mode, Double> tripScalingFactors;
    private static final Logger logger = Logger.getLogger(TripScaling.class);

    public TripScaling(DataSet dataSet) {
        super(dataSet);
        tripScalingFactors = new HashMap<>();
        double globalScaleFactor =Resources.INSTANCE.getDouble(Properties.TRIP_SCALING_FACTOR, 0.05);
        for (Mode mode : Mode.values()){
            tripScalingFactors.put(mode,
                    Resources.INSTANCE.getDouble(Properties.TRIP_SCALING_FACTOR + "." + mode.toString(), globalScaleFactor));
        }

        for (Mode mode : Mode.values()){
            logger.info("Using scale factor of " + tripScalingFactors.get(mode) + " for mode " + mode);
        }



    }

    @Override
    public void run() {
        scaleTrips();
    }


    private void scaleTrips() {
        dataSet.emptyTripSubsample();
        dataSet.getTrips().values().forEach(trip -> {
            if (trip.getTripMode() != null) {
                if (MitoUtil.getRandomObject().nextDouble() < tripScalingFactors.get(trip.getTripMode())) {
                    dataSet.addTripToSubsample(trip);
                }
            }
        });

        logger.info("Trips scaled down. The sub-sample of trips contains " + dataSet.getTripSubsample().size() + " trips.");

    }
}
