package de.tum.bgu.msm.data.travelTimes;

import com.pb.common.matrix.Matrix;

public class MatrixTravelTimes implements TravelTimes {
    private final Matrix matrix;

    public MatrixTravelTimes(Matrix matrix) {
        this.matrix = matrix;
    }

    @Override
    public double getTravelTime(int origin, int destination) {
        return matrix.getValueAt(origin, destination);
    }
}