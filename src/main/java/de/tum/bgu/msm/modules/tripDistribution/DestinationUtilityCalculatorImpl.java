package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorImpl implements DestinationUtilityCalculator {

    private final static double TRAVEL_DISTANCE_PARAM_HBW = -0.01 * 1.87668768839999;
    private final static double IMPEDANCE_PARAM_HBW = 9;

    private final static double TRAVEL_DISTANCE_PARAM_HBE = -0.01 * 1.09993274026271;
    private final static double IMPEDANCE_PARAM_HBE = 28.3;

    private final static double TRAVEL_DISTANCE_PARAM_HBS = -0.01 * 3.88461641985529;
    private final static double IMPEDANCE_PARAM_HBS = 14.5;

    private final static double TRAVEL_DISTANCE_PARAM_HBO = -0.01 * 0.36606039220205;
    private final static double IMPEDANCE_PARAM_HBO = 53;

    private final static double travelDistanceParamNhbw = -0.01 * 0.874028408112042;
    private final static double impedanceParamNhbw = 15.1;

    private final static double travelDistanceParamNhbo = -0.01 * 0.1314828354307;
    private final static double impedanceParamNhbo = 110;

    private double distanceParam;
    private double impedanceParam;

    DestinationUtilityCalculatorImpl(Purpose purpose, double travelDistanceCalibrationK, double impendanceCalibrationK) {
        switch (purpose) {
            case HBW:
                distanceParam = TRAVEL_DISTANCE_PARAM_HBW;
                impedanceParam = IMPEDANCE_PARAM_HBW;
                break;
            case HBE:
                distanceParam = TRAVEL_DISTANCE_PARAM_HBE;
                impedanceParam = IMPEDANCE_PARAM_HBE;
                break;
            case HBS:
                distanceParam = TRAVEL_DISTANCE_PARAM_HBS;
                impedanceParam = IMPEDANCE_PARAM_HBS;
                break;
            case HBO:
                distanceParam = TRAVEL_DISTANCE_PARAM_HBO;
                impedanceParam = IMPEDANCE_PARAM_HBO;
                break;
            case NHBW:
                distanceParam = travelDistanceParamNhbw;
                impedanceParam = impedanceParamNhbw;
                break;
            case NHBO:
                distanceParam = travelDistanceParamNhbo;
                impedanceParam = impedanceParamNhbo;
                break;
            case AIRPORT:
            default:
                throw new RuntimeException("not implemented!");
        }

        distanceParam = distanceParam * travelDistanceCalibrationK;
        impedanceParam = impedanceParam * impendanceCalibrationK;

    }

    @Override
    public double calculateUtility(double attraction, double travelDistance) {
        if(attraction == 0) {
            return 0.;
        }
        double impedance = impedanceParam * Math.exp(distanceParam * travelDistance);
        return Math.exp(impedance) * attraction;
    }
}
