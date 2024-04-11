package de.tum.bgu.msm.scenarios.mito7days.calculators;

import de.tum.bgu.msm.data.MitoPerson7days;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.tripDistribution.AbstractDestinationUtilityCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DestinationUtilityCalculatorImplRrt7days extends AbstractDestinationUtilityCalculator {

    private final static double[] DISTANCE_PARAMS_RRT = {-0.05204,-0.05204,-0.05204};
    private final static double IMPEDANCE_PARAM_RRT = 12;

    public DestinationUtilityCalculatorImplRrt7days(Purpose purpose) {
        if (purpose != Purpose.RRT) {
            throw new RuntimeException("This calculator is for RRT trips only!");
        }
        distanceParams = DISTANCE_PARAMS_RRT;
        impedanceParam = IMPEDANCE_PARAM_RRT;
    }

    @Override
    public List<Predicate<MitoPerson>> getCategories() {
        List<Predicate<MitoPerson>> filters = new ArrayList<>();
        filters.add(0,p -> (((MitoPerson7days)p).getModeSet().getModes().contains(Mode.walk) && !((MitoPerson7days)p).getModeSet().getModes().contains(Mode.bicycle)));
        filters.add(1,p -> (((MitoPerson7days)p).getModeSet().getModes().contains(Mode.walk) && ((MitoPerson7days)p).getModeSet().getModes().contains(Mode.bicycle)));
        filters.add(2,p -> (!((MitoPerson7days)p).getModeSet().getModes().contains(Mode.walk) && ((MitoPerson7days)p).getModeSet().getModes().contains(Mode.bicycle)));
        return filters;
    }
}
