package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

import java.util.Map;

public class DestinationUtilityCalculatorImpl implements DestinationUtilityCalculator {

    private final static double TRAVEL_DISTANCE_PARAM_HBW =  -0.07;
    private final static double IMPEDANCE_PARAM_HBW = 9;

    private final static double TRAVEL_DISTANCE_PARAM_HBE =  -0.149;
    private final static double IMPEDANCE_PARAM_HBE = 28.3;

    private final static double TRAVEL_DISTANCE_PARAM_HBS = -0.045;
    private final static double IMPEDANCE_PARAM_HBS = 14.5;

    private final static double TRAVEL_DISTANCE_PARAM_HBO = -0.062;
    private final static double IMPEDANCE_PARAM_HBO = 53;

    private final static double TRAVEL_DISTANCE_PARAM_HBR = -0.062;
    private final static double IMPEDANCE_PARAM_HBR = 53;

    private final static double travelDistanceParamNhbw = -0.03;
    private final static double impedanceParamNhbw = 15.1;

    private final static double travelDistanceParamNhbo = -0.004;
    private final static double impedanceParamNhbo = 110;

    private double distanceParam;
    private double impedanceParam;

    DestinationUtilityCalculatorImpl(Purpose purpose, Map<String, Double> coefficients) {
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
            case HBR:
                distanceParam = TRAVEL_DISTANCE_PARAM_HBR;
                impedanceParam = IMPEDANCE_PARAM_HBR;
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
        double travelDistanceCalibrationK = coefficients.get(ExplanatoryVariable.calibrationFactorAlphaDistance);
        double impendanceCalibrationK = coefficients.get(ExplanatoryVariable.calibrationFactorBetaExpDistance);

        distanceParam = distanceParam * travelDistanceCalibrationK;
        impedanceParam = impedanceParam * impendanceCalibrationK;


    }


    @Override
    public double calculateExpUtility(Map<String, Double> variables) {



        double attraction = variables.get(ExplanatoryVariable.logAttraction);
        double travelDistance = variables.get(ExplanatoryVariable.distance_km);
        if(attraction == 0) {
            return 0.;
        }
        double impedance = impedanceParam * Math.exp(distanceParam * travelDistance);
        return Math.exp(impedance) * attraction;
    }
}
