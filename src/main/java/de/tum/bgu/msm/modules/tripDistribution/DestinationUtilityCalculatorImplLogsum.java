package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorImplLogsum implements DestinationUtilityCalculator {

    private final static double LOGSUM_PARAM_HBW = 1;
    private final static double DISTANCE_PARAM_HBW = -0.5*0.123006211921797;

    private final static double LOGSUM_PARAM_HBE = 1;
    private final static double DISTANCE_PARAM_HBE = -0.5*0.466859588673074;

    private final static double LOGSUM_PARAM_HBS = 1;
    private final static double DISTANCE_PARAM_HBS = -0.5*0.707099605214351;
    private final static double LOGSUM_PARAM_HBO = 1;
    private final static double DISTANCE_PARAM_HBO = -0.5*0.288048089619584;
    private final static double LOGSUM_PARAM_HBR = 1;
    private final static double DISTANCE_PARAM_HBR = -0.5*0.286911794688365;
    private final static double logsumParamNhbw = 1;
    private final static double DISTANCE_PARAM_NHBW = -0.5*0.141392969613424;
    private final static double logsumParamNhbo = 1;
    private final static double DISTANCE_PARAM_NHBO = -0.5*0.205053811138059;
    private final static double ALPHA_PARAM =  1;
    private double attractionParam;
    private double logsumParam;
    private double distanceParam;

    DestinationUtilityCalculatorImplLogsum(Purpose purpose, double logsumCalibrationK, double distanceCalibrationK) {
        switch (purpose) {
            case HBW:
                logsumParam = LOGSUM_PARAM_HBW;
                distanceParam = DISTANCE_PARAM_HBW;
                break;
            case HBE:
                logsumParam = LOGSUM_PARAM_HBE;
                distanceParam = DISTANCE_PARAM_HBE;
                break;
            case HBS:
                logsumParam = LOGSUM_PARAM_HBS;
                distanceParam = DISTANCE_PARAM_HBS;
                break;
            case HBO:
                logsumParam = LOGSUM_PARAM_HBO;
                distanceParam = DISTANCE_PARAM_HBO;
                break;
            case HBR:
                logsumParam = LOGSUM_PARAM_HBR;
                distanceParam = DISTANCE_PARAM_HBR;
                break;
            case NHBW:
                logsumParam = logsumParamNhbw;
                distanceParam = DISTANCE_PARAM_NHBW;
                break;
            case NHBO:
                logsumParam = logsumParamNhbo;
                distanceParam = DISTANCE_PARAM_NHBO;
                break;
            case AIRPORT:
            default:
                throw new RuntimeException("not implemented!");
        }

        //logsumParam = logsumParam * logsumCalibrationK;

        distanceParam = distanceParam * distanceCalibrationK;

        attractionParam = ALPHA_PARAM;

        //attractionParam = attractionParam * attractionCalibrationK;

    }

    @Override
    public double calculateExpUtility(double attraction, double logsum) {
        if(attraction == 0) {
            return 0.;
        }
        //check whether attraction is divided by zone area
        double utility = logsumParam *logsum + Math.log(attraction);

        return Math.exp(utility);
    }

    public double calculateExpUtility2(double attraction, double logsum, double travelDistance) {
        if(attraction == 0) {
            return 0.;
        }
        //check whether attraction is divided by zone area
        double utility = logsumParam *logsum + Math.log(attraction) + distanceParam*travelDistance;

        return Math.exp(utility);
    }
}
