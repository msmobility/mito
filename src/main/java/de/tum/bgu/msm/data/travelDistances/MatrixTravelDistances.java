package de.tum.bgu.msm.data.travelDistances;

import com.pb.common.matrix.Matrix;

public class MatrixTravelDistances implements TravelDistances{

    private final Matrix matrix;

    public MatrixTravelDistances(Matrix matrix) {
        this.matrix = matrix;
    }

    @Override
    public double getTravelDistanceFromTo(int origin, int destination) {
        return matrix.getValueAt(origin, destination);
    }
}
