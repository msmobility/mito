package de.tum.bgu.msm.data.travelTimes;

import cern.colt.matrix.tdouble.DoubleMatrix2D;

public class SkimTravelTimes implements TravelTimes {
    private final DoubleMatrix2D matrix;

    public SkimTravelTimes(DoubleMatrix2D matrix) {
        this.matrix = matrix;
    }

    @Override
    public double getTravelTime(int origin, int destination, double timeOfDay_s, double factor) {
        // Currently, the time of day is not used here, but it could. E.g. if there are multiple matrices for
        // different "time-of-day slices" the argument could be used to select the correct matrix, nk/dz, jan'18
        return matrix.getQuick(origin, destination)*factor;
    }

    public DoubleMatrix2D getPeakTravelTimeMatrix() {
        return matrix;
    }
}