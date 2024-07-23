package uk.cam.mrc.phm.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.AbstractDestinationUtilityCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DestinationUtilityCalculatorMCR extends AbstractDestinationUtilityCalculator {

    private final static double[] TRAVEL_DISTANCE_PARAM_HBW = {-0.06675115682225344, -0.02785889823285924, -0.018805823704140592};
    private final static double IMPEDANCE_PARAM_HBW = 9;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBE = {-0.03393849928010303, -0.024919982277472854, -0.034643147626042946};
    private final static double IMPEDANCE_PARAM_HBE = 28.3;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBA = {-0.005791, -0.005791, -0.005791};//TODO: calibrate
    private final static double IMPEDANCE_PARAM_HBA = 28.3;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBS = {-0.08049319544639906, -0.04706601235281219, -0.04016499605940564};
    private final static double IMPEDANCE_PARAM_HBS = 14.5;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBR = {-0.035133409019885625, -0.020448704125188105, -0.017057865888686893};
    private final static double IMPEDANCE_PARAM_HBR = 20;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBO = {-0.011583033011118737, -0.007146545236671167, -0.005528662719291222};
    private final static double IMPEDANCE_PARAM_HBO = 53;

    private final static double[] TRAVEL_DISTANCE_PARAM_RRT = {-0.058139, -0.025304, -0.010405}; //redundant?
    private final static double IMPEDANCE_PARAM_RRT = 20; //redundant?

    private final static double[] TRAVEL_DISTANCE_PARAM_NHBW = {-0.11848876664542038, -0.028941405563475096, -0.016543094786904675};
    private final static double IMPEDANCE_PARAM_NHBW = 15.1;

    private final static double[] TRAVEL_DISTANCE_PARAM_NHBO = {-0.08629743236858595, -0.027817901602037946, -0.023372889296758886};
    private final static double IMPEDANCE_PARAM_NHBO = 20;


    public DestinationUtilityCalculatorMCR(Purpose purpose) {

         switch (purpose) {
            case HBW:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBW;
                impedanceParam = IMPEDANCE_PARAM_HBW;
                break;
            case HBE:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBE;
                impedanceParam = IMPEDANCE_PARAM_HBE;
                break;
             case HBA:
                 distanceParams = TRAVEL_DISTANCE_PARAM_HBA;
                 impedanceParam = IMPEDANCE_PARAM_HBA;
                 break;
            case HBS:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBS;
                impedanceParam = IMPEDANCE_PARAM_HBS;
                break;
            case HBR:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBR;
                impedanceParam = IMPEDANCE_PARAM_HBR;
                break;
            case HBO:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBO;
                impedanceParam = IMPEDANCE_PARAM_HBO;
                break;
            case RRT:
                distanceParams = TRAVEL_DISTANCE_PARAM_RRT;
                impedanceParam = IMPEDANCE_PARAM_RRT;
                break;
            case NHBW:
                distanceParams = TRAVEL_DISTANCE_PARAM_NHBW;
                impedanceParam = IMPEDANCE_PARAM_NHBW;
                break;
            case NHBO:
                distanceParams = TRAVEL_DISTANCE_PARAM_NHBO;
                impedanceParam = IMPEDANCE_PARAM_NHBO;
                break;
            case AIRPORT:
            default:
                throw new RuntimeException("not implemented!");
        }
    }

    @Override
    public List<Predicate<MitoPerson>> getCategories() {
        List<Predicate<MitoPerson>> filters = new ArrayList<>();
        filters.add(0,p -> p.getHousehold().getAutos() == 0);
        filters.add(1,p -> p.getHousehold().getAutosPerAdult() > 0 && p.getHousehold().getAutosPerAdult() < 1);
        filters.add(2,p -> p.getHousehold().getAutosPerAdult() >= 1);
        return filters;
    }
}
