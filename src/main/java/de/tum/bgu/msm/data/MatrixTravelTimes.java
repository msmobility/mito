package de.tum.bgu.msm.data;

import com.pb.common.matrix.Matrix;
import org.apache.log4j.Logger;

public class MatrixTravelTimes implements TravelTimes{

    private static final Logger logger = Logger.getLogger(MatrixTravelTimes.class);

    private final Matrix matrix;

    public MatrixTravelTimes(Matrix matrix) {
        this.matrix = matrix;
    }

    @Override
    public double getTravelTimeFromTo(Zone origin, Zone destination) {
        if(origin == null || destination == null) {
           logger.warn("Origin or Destination is null. Returning 0 Travel time.");
           return 0;
        }
        return matrix.getValueAt(origin.getZoneId(), destination.getZoneId());
    }
}
