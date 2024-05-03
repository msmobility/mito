package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorImplLogsum implements DestinationUtilityCalculator {

    private final static double LOGSUM_PARAM_HBW = 0.5*6.75918471643653;

    private final static double LOGSUM_PARAM_HBE = 0.5*16.4313941966351;

    private final static double LOGSUM_PARAM_HBS = 0.5*21.37783557176;

    private final static double LOGSUM_PARAM_HBO = 0.5*11.9924433009633;

    private final static double LOGSUM_PARAM_HBR = 0.5*11.9685601413591;

    private final static double logsumParamNhbw = 0.5*7.10808298720081;

    private final static double logsumParamNhbo = 0.5*7.40653414348498;

    private final static double ALPHA_PARAM =  1;
    private double attractionParam;
    private double logsumParam;

    DestinationUtilityCalculatorImplLogsum(Purpose purpose, double travelDistanceCalibrationK, double attractionCalibrationK) {
        switch (purpose) {
            case HBW:
                logsumParam = LOGSUM_PARAM_HBW;
                break;
            case HBE:
                logsumParam = LOGSUM_PARAM_HBE;
                break;
            case HBS:
                logsumParam = LOGSUM_PARAM_HBS;
                break;
            case HBO:
                logsumParam = LOGSUM_PARAM_HBO;
                break;
            case HBR:
                logsumParam = LOGSUM_PARAM_HBR;
                break;
            case NHBW:
                logsumParam = logsumParamNhbw;
                break;
            case NHBO:
                logsumParam = logsumParamNhbo;
                break;
            case AIRPORT:
            default:
                throw new RuntimeException("not implemented!");
        }

        logsumParam = logsumParam * travelDistanceCalibrationK;

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
}
