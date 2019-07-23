package de.tum.bgu.msm.data.waitingTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;

/**
 * Waiting times uniform for all trips and all locations of the study area
 */
public class UniformWaitingTimes implements WaitingTimes {

    @Override
    public double getWaitingTime(MitoTrip trip, Location origin, Location destination, String mode, double timeOfDay_s) {
        if (mode.equalsIgnoreCase(Mode.uam.toString())){
            return Resources.INSTANCE.getDouble(Properties.UAM_BOARDINGTIME, 0);
        } else {
            return 0;
        }
    }
}
