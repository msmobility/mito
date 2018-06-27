package de.tum.bgu.msm.modules.scaling;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

public class TripScaling extends Module {

    private double tripScalingFactor;
    private static final Logger logger = Logger.getLogger(TripScaling.class);

    public TripScaling(DataSet dataSet) {
        super(dataSet);
        tripScalingFactor = Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR));
    }

    @Override
    public void run() {

        scaleTrips();
    }

    private void scaleTrips() {

        dataSet.getTrips().values().forEach(trip -> {
            if (MitoUtil.getRandomObject().nextDouble() < tripScalingFactor) {
                dataSet.addTripToSubsample(trip);
            }
        });

        logger.info("Trips scaled down. The sub-sample of trips contains " + dataSet.getTripSubsample().size() + " trips.");

    }
}
