package de.tum.bgu.msm.data.travelTimes;

import cern.colt.matrix.tfloat.FloatMatrix2D;

public class MatrixTravelTimes implements TravelTimes {
    private final FloatMatrix2D matrix;

    public MatrixTravelTimes(FloatMatrix2D matrix) {
        this.matrix = matrix;
    }

    @Override
    public double getTravelTime(int origin, int destination, double timeOfDay_s) {
        // Currently, the time of day is not used here, but it could. E.g. if there are multiple matrices for
        // different "time-of-day slices" the argument could be used to select the correct matrix, nk/dz, jan'18
        return matrix.get(origin, destination);
    }
}