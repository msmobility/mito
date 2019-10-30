package de.tum.bgu.msm.data.waitingTimes;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import org.apache.log4j.Logger;

/**
 * Waiting times uniform for all trips and all locations of the study area
 */
public class UniformTotalHandlingTimes implements TotalHandlingTimes {

    private static Logger logger = Logger.getLogger(UniformTotalHandlingTimes.class);
    private double PENALTY_FACTOR = Resources.INSTANCE.getDouble(Properties.WAITING_TIME_PENALTY, 1.);
    private boolean OVERWRITE_HANDLING_TIME = Resources.INSTANCE.getBoolean(Properties.UAM_OVERWRITE_BOARDING_TIME, false);
    private double OVERWRITTEN_HANDLING_TIME = Resources.INSTANCE.getDouble(Properties.UAM_BOARDINGTIME, 20);
    private double uniqueHandlingTime_min;


    public UniformTotalHandlingTimes(DataSet dataSet) {

        if (OVERWRITE_HANDLING_TIME){
            uniqueHandlingTime_min = OVERWRITTEN_HANDLING_TIME * PENALTY_FACTOR;
        } else {
            UAMStation firstStation = dataSet.getStationToZoneMap().keySet().iterator().next();
            uniqueHandlingTime_min = (firstStation.getPostFlightTime() +
                    firstStation.getPreFlightTime() +
                    firstStation.getDefaultWaitTime()) * PENALTY_FACTOR / 60;
            logger.warn("MITO is reading the UAM waiting time, pre-flight time and post-flight from the first station. This will" +
                    "be wrong if different stations have different times. The selected waiting time (including penalty factor) is " +
                    uniqueHandlingTime_min);
        }


    }

    @Override
    public double getWaitingTime(MitoTrip trip, Location origin, Location destination, String mode) {
        if (mode.equalsIgnoreCase(Mode.uam.toString())){
            return uniqueHandlingTime_min;
        } else {
            return 0;
        }
    }
}
