package de.tum.bgu.msm.data.waitingTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;

/**
 * Waiting times uniform for all trips and all locations of the study area
 */
public class UniformTotalHandlingTimes implements TotalHandlingTimes {

    private double PENALTY_FACTOR = Resources.INSTANCE.getDouble(Properties.WAITING_TIME_PENALTY, 1.);


    @Override
    public double getWaitingTime(MitoTrip trip, Location origin, Location destination, String mode) {
        if (mode.equalsIgnoreCase(Mode.uam.toString())){
            return Resources.INSTANCE.getDouble(Properties.UAM_BOARDINGTIME, 0) * PENALTY_FACTOR;
        } else {
            return 0;
        }
    }
}
