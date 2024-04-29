package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorImpl3 implements DestinationUtilityCalculator {

    private final static double LOGSUM_PARAM_HBW = -0.01 * 1.87668768839999;

    private final static double LOGSUM_PARAM_HBE = -0.01 * 1.09993274026271;

    private final static double LOGSUM_PARAM_HBS = -0.01 * 3.88461641985529;

    private final static double LOGSUM_PARAM_HBO = -0.01 * 0.36606039220205;

    private final static double LOGSUM_PARAM_HBR = -0.01 * 0.36606039220205;

    private final static double logsumParamNhbw = -0.01 * 0.874028408112042;

    private final static double logsumParamNhbo = -0.01 * 0.1314828354307;

    private double logsumParam;

    DestinationUtilityCalculatorImpl3(Purpose purpose, double travelDistanceCalibrationK, double impendanceCalibrationK) {
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

    }

    @Override
    public double calculateUtility(double attraction, double logsum) {
        if(attraction == 0) {
            return 0.;
        }
        double utility = logsumParam *logsum + Math.log(attraction);

        return utility;
    }
}
