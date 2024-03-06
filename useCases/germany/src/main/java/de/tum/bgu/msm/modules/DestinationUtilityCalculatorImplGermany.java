package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.tripDistribution.AbstractDestinationUtilityCalculator;

public class DestinationUtilityCalculatorImplGermany extends AbstractDestinationUtilityCalculator {

    private final static double[] TRAVEL_DISTANCE_PARAM_HBW = {-0.01 * 0.545653257377378};
    private final static double IMPEDANCE_PARAM_HBW = 20;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBE = {-0.01 * 1.09211334287783};
    private final static double IMPEDANCE_PARAM_HBE = 20;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBS = {-0.01 * 1.382831732};
    private final static double IMPEDANCE_PARAM_HBS = 20;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBO = {-0.01 * 1.02679034779653};
    private final static double IMPEDANCE_PARAM_HBO = 20;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBR = {-0.01 * 0.874195571671594};
    private final static double IMPEDANCE_PARAM_HBR = 20;

    private final static double[] TRAVEL_DISTANCE_PARAM_NHBW = {-0.01 * 0.733731103853844};
    private final static double IMPEDANCE_PARAM_NHBW = 20;

    private final static double[] TRAVEL_DISTANCE_PARAM_NHBO = {-0.01 * 0.993491317833469};
    private final static double IMPEDANCE_PARAM_NHBO = 20;

    public DestinationUtilityCalculatorImplGermany(Purpose purpose) {
        switch (purpose) {
            case HBW:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBW;
                impedanceParam = IMPEDANCE_PARAM_HBW;
                //maxDistance_km = 200.0;
                break;
            case HBE:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBE;
                impedanceParam = IMPEDANCE_PARAM_HBE;
                //maxDistance_km = 200.0;
                break;
            case HBS:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBS;
                impedanceParam = IMPEDANCE_PARAM_HBS;
                //maxDistance_km = 40.;
                break;
            case HBO:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBO;
                impedanceParam = IMPEDANCE_PARAM_HBO;
                //maxDistance_km = 40.;
                break;
            case HBR:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBR;
                impedanceParam = IMPEDANCE_PARAM_HBR;
                //maxDistance_km = 40.;
                break;
            case NHBW:
                distanceParams = TRAVEL_DISTANCE_PARAM_NHBW;
                impedanceParam = IMPEDANCE_PARAM_NHBW;
                //maxDistance_km = 40.;
                break;
            case NHBO:
                distanceParams = TRAVEL_DISTANCE_PARAM_NHBO;
                impedanceParam = IMPEDANCE_PARAM_NHBO;
                //maxDistance_km = 40.;
                break;
            case AIRPORT:
            default:
                throw new RuntimeException("not implemented!");
        }
    }
}
