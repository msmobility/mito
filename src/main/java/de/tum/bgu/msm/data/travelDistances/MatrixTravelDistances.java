package de.tum.bgu.msm.data.travelDistances;

import cern.colt.matrix.tfloat.FloatMatrix2D;

public class MatrixTravelDistances implements TravelDistances{

    private final FloatMatrix2D matrix;

    public MatrixTravelDistances(FloatMatrix2D matrix) {
        this.matrix = matrix;
    }

    @Override
    public double getTravelDistance(int origin, int destination) {
        return matrix.get(origin, destination);
    }
}
