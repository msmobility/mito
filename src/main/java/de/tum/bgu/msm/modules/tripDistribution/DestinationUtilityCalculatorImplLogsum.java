package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorImplLogsum implements DestinationUtilityCalculator {

    private final static double LOGSUM_PARAM_HBW = 1;
    private final static double DISTANCE_PARAM_HBW = -1*0.0369764538967331;
    //private final static double DISTANCE_PARAM_HBW = -0.5*0.0777520131749877;
    private final static double LOGSUM_PARAM_HBE = 1;
    private final static double DISTANCE_PARAM_HBE = -1*0.212167410859099;
    //private final static double DISTANCE_PARAM_HBE = -0.5*0.330875626394118;
    private final static double LOGSUM_PARAM_HBS = 1;
    private final static double DISTANCE_PARAM_HBS = -1*0.269417097299754;
    //private final static double DISTANCE_PARAM_HBS = -0.5*0.518760210754947;
    private final static double LOGSUM_PARAM_HBO = 1;
    private final static double DISTANCE_PARAM_HBO = -1*0.104658086500562;
    //private final static double DISTANCE_PARAM_HBO = -0.5*0.191541297072671;
    private final static double LOGSUM_PARAM_HBR = 1;
    private final static double DISTANCE_PARAM_HBR = -1*0.0947263077747207;
    //private final static double DISTANCE_PARAM_HBR = -0.5*0.190380485614451;
    private final static double logsumParamNhbw = 1;
    private final static double DISTANCE_PARAM_NHBW = -1*0.0551109867566214;
    //private final static double DISTANCE_PARAM_NHBW = -0.5*0.0883518244383502;
    private final static double logsumParamNhbo = 1;
    private final static double DISTANCE_PARAM_NHBO = -1*0.0743148890997462;
    //private final static double DISTANCE_PARAM_NHBO = -0.5*0.127675456535262;
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
