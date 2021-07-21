package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

import java.util.Map;

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

    DestinationUtilityCalculatorImplGermany(Purpose purpose, Map<String, Double> coefficients) {
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
