package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.tripDistribution.DestinationUtilityCalculator;

public class DestinationUtilityCalculatorImplGermany implements DestinationUtilityCalculator {

    private final static double TRAVEL_DISTANCE_PARAM_HBW = -0.01 * 0.545653257377378;
    private final static double IMPEDANCE_PARAM_HBW = 20;

    private final static double TRAVEL_DISTANCE_PARAM_HBE = -0.01 * 1.09211334287783;
    private final static double IMPEDANCE_PARAM_HBE = 20;

    private final static double TRAVEL_DISTANCE_PARAM_HBS = -0.01 * 1.382831732;
    private final static double IMPEDANCE_PARAM_HBS = 20;

    private final static double TRAVEL_DISTANCE_PARAM_HBO = -0.01 * 1.02679034779653;
    private final static double IMPEDANCE_PARAM_HBO = 20;

    private final static double TRAVEL_DISTANCE_PARAM_HBR = -0.01 * 0.874195571671594;
    private final static double IMPEDANCE_PARAM_HBR = 20;

    private final static double travelDistanceParamNhbw = -0.01 * 0.733731103853844;
    private final static double impedanceParamNhbw = 20;

    private final static double travelDistanceParamNhbo = -0.01 * 0.993491317833469;
    private final static double impedanceParamNhbo = 20;

    private double distanceParam;
    private double impedanceParam;
    private double maxDistance_km;
    private double attractionParam = 1.;

    public DestinationUtilityCalculatorImplGermany(Purpose purpose, double travelDistanceCalibrationK, double impendanceCalibrationK) {
        switch (purpose) {
            case HBW:
                distanceParam = TRAVEL_DISTANCE_PARAM_HBW;
                impedanceParam = IMPEDANCE_PARAM_HBW;
                //maxDistance_km = 200.0;
                break;
            case HBE:
                distanceParam = TRAVEL_DISTANCE_PARAM_HBE;
                impedanceParam = IMPEDANCE_PARAM_HBE;
                //maxDistance_km = 200.0;
                break;
            case HBS:
                distanceParam = TRAVEL_DISTANCE_PARAM_HBS;
                impedanceParam = IMPEDANCE_PARAM_HBS;
                //maxDistance_km = 40.;
                break;
            case HBO:
                distanceParam = TRAVEL_DISTANCE_PARAM_HBO;
                impedanceParam = IMPEDANCE_PARAM_HBO;
                //maxDistance_km = 40.;
                break;
            case HBR:
                distanceParam = TRAVEL_DISTANCE_PARAM_HBR;
                impedanceParam = IMPEDANCE_PARAM_HBR;
                //maxDistance_km = 40.;
                break;
            case NHBW:
                distanceParam = travelDistanceParamNhbw;
                impedanceParam = impedanceParamNhbw;
                //maxDistance_km = 40.;
                break;
            case NHBO:
                distanceParam = travelDistanceParamNhbo;
                impedanceParam = impedanceParamNhbo;
                //maxDistance_km = 40.;
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
        //if(travelDistance > maxDistance_km){
        //    return 0.;
        //}
        double impedance = impedanceParam * Math.exp(distanceParam * travelDistance);
        return Math.exp(impedance) * Math.pow(attraction, attractionParam);
    }
}
