package de.tum.bgu.msm.scenarios.mito7days.calculators;

import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.tripDistribution.AbstractDestinationUtilityCalculator;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DestinationUtilityCalculatorImpl7days extends AbstractDestinationUtilityCalculator {

    private final static double[] TRAVEL_DISTANCE_PARAM_HBW = {-0.025800133507709294, -0.021167624669922537, -0.014055080245173423};
    private final static double IMPEDANCE_PARAM_HBW = 9;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBE = {-0.003554915803471327, -0.006447797093297055, -0.006810388633786645};
    private final static double IMPEDANCE_PARAM_HBE = 28.3;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBS = {-0.04593, -0.03493, -0.03075};
    private final static double IMPEDANCE_PARAM_HBS = 50;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBR = {-0.009944686933173689, -0.009241843507574019, -0.008736433116592727};
    private final static double IMPEDANCE_PARAM_HBR = 20;

    private final static double[] TRAVEL_DISTANCE_PARAM_HBO = {-0.004000351053144032, -0.0033042359235793046, -0.003359528360854156};
    private final static double IMPEDANCE_PARAM_HBO = 53;

    //private final static double[] TRAVEL_DISTANCE_PARAM_RRT = {-0.058139, -0.025304, -0.010405};
    //private final static double IMPEDANCE_PARAM_RRT = 50;

    private final static double[] TRAVEL_DISTANCE_PARAM_NHBW = {-0.01437656108488677, -0.011044223273121899, -0.008043508731979848}; // -0.012747;
    private final static double IMPEDANCE_PARAM_NHBW = 15.1;

    private final static double[] TRAVEL_DISTANCE_PARAM_NHBO = {-0.018460905261276462, -0.012236997775383089, -0.010813321037005713}; // -0.0130997;
    private final static double IMPEDANCE_PARAM_NHBO = 20;


    public DestinationUtilityCalculatorImpl7days(Purpose purpose) {

         switch (purpose) {
            case HBW:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBW;
                impedanceParam = IMPEDANCE_PARAM_HBW;
                break;
            case HBE:
                distanceParams = TRAVEL_DISTANCE_PARAM_HBE;
                impedanceParam = IMPEDANCE_PARAM_HBE;
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
                //distanceParams = TRAVEL_DISTANCE_PARAM_RRT;
                //impedanceParam = IMPEDANCE_PARAM_RRT;
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

    public List<Predicate<MitoPerson>> getCategories() {
        List<Predicate<MitoPerson>> filters = new ArrayList<>();
        filters.add(0,p -> p.getHousehold().getAutos() == 0);
        filters.add(1,p -> p.getHousehold().getAutosPerAdult() > 0 && p.getHousehold().getAutosPerAdult() < 1);
        filters.add(2,p -> p.getHousehold().getAutosPerAdult() >= 1);
        return filters;
    }
}
