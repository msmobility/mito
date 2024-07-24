package uk.cam.mrc.phm.calculators;

import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoPerson7days;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.tripDistribution.AbstractDestinationUtilityCalculator;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DestinationUtilityCalculatorRrtMCR extends AbstractDestinationUtilityCalculator {

    private final static double[] DISTANCE_PARAMS_RRT_noModeSet = {-0.3695279713696964, -0.11012406018149026, -0.11420908279680453};
    private final static double IMPEDANCE_PARAM_RRT_noModeSet = 20;

    private final static double[] DISTANCE_PARAMS_RRT_withModeSet = {-0.42720955240418723, -0.11091417841660255, -0.11442498509125135}; //TODO: calibrate
    private final static double IMPEDANCE_PARAM_RRT_withModeSet = 20;

    public DestinationUtilityCalculatorRrtMCR(Purpose purpose) {
        if (purpose != Purpose.RRT) {
            throw new RuntimeException("This calculator is for RRT trips only!");
        }

        if(Resources.instance.getBoolean(Properties.RUN_MODESET,false)){
            distanceParams = DISTANCE_PARAMS_RRT_withModeSet;
            impedanceParam = IMPEDANCE_PARAM_RRT_withModeSet;
        }else {
            distanceParams = DISTANCE_PARAMS_RRT_noModeSet;
            impedanceParam = IMPEDANCE_PARAM_RRT_noModeSet;
        }

    }

    @Override
    public List<Predicate<MitoPerson>> getCategories() {
        List<Predicate<MitoPerson>> filters = new ArrayList<>();

        if(Resources.instance.getBoolean(Properties.RUN_MODESET,false)){
            filters.add(0,p -> (((MitoPerson7days)p).getModeSet().getModes().contains(Mode.walk) && !((MitoPerson7days)p).getModeSet().getModes().contains(Mode.bicycle)));
            filters.add(1,p -> (((MitoPerson7days)p).getModeSet().getModes().contains(Mode.walk) && ((MitoPerson7days)p).getModeSet().getModes().contains(Mode.bicycle)));
            filters.add(2,p -> (!((MitoPerson7days)p).getModeSet().getModes().contains(Mode.walk) && ((MitoPerson7days)p).getModeSet().getModes().contains(Mode.bicycle)));
        }else{
            filters.add(0,p -> p.getHousehold().getAutos() == 0);
            filters.add(1,p -> p.getHousehold().getAutos() == 1);
            filters.add(2,p -> p.getHousehold().getAutos() > 1);
        }

        return filters;
    }
}
