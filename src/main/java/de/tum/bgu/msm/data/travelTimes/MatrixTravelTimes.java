package de.tum.bgu.msm.data.travelTimes;

import org.apache.log4j.Logger;

import com.pb.common.matrix.Matrix;

public class MatrixTravelTimes implements TravelTimes {

    private static final Logger logger = Logger.getLogger(MatrixTravelTimes.class);

    private final Matrix matrix;

    public MatrixTravelTimes(Matrix matrix) {
        this.matrix = matrix;
    }

    @Override
    public double getTravelTimeFromTo(int origin, int destination) {
        return matrix.getValueAt(origin, destination);
    }
}