package de.tum.bgu.msm.data.travelTimes;

import com.pb.common.matrix.Matrix;

public class MatrixTravelTimes implements TravelTimes {
    private final Matrix matrix;

    public MatrixTravelTimes(Matrix matrix) {
        this.matrix = matrix;
    }

    @Override
    public double getTravelTime(int origin, int destination, double timeOfDay_s) {
        // Currently, the time of day is not used here, but it could. E.g. if there are multiple matrices for
        // different "time-of-day slices" the argument could be used to select the correct matrix, nk/dz, jan'18
        return matrix.getValueAt(origin, destination);
    }
}