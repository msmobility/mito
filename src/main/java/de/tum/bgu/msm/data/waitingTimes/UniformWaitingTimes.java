package de.tum.bgu.msm.data.waitingTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;

public class UniformWaitingTimes implements WaitingTimes {


    /**
     * returns the sum of waiting time and processing time
     * @param origin
     * @param destination
     * @param mode
     * @return
     */
    @Override
    public double getWaitingTime(Location origin, Location destination, String mode) {
        if (mode.equalsIgnoreCase(Mode.uam.toString())){
            return Resources.INSTANCE.getDouble(Properties.UAM_BOARDINGTIME, 0);
        } else {
            return 0;
        }
    }

}
