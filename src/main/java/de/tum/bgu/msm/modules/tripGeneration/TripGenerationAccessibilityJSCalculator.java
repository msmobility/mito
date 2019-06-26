package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;
import ncsa.hdf.object.Dataset;

import java.io.Reader;

public class TripGenerationAccessibilityJSCalculator extends JavaScriptCalculator<Double>{

    private final String function;

    protected TripGenerationAccessibilityJSCalculator(Reader reader, Purpose purpose) {
        super(reader);
        function = "calculate"+purpose+"Probabilities";
    }

    public double calculateProbabilities(MitoHousehold household, MitoZone origin){
        return super.calculate(function,
                household.getHhSize(),
                DataSet.getFemalesForHousehold(household),
                DataSet.getYoungAdultsForHousehold(household),
                DataSet.getRetireesForHousehold(household),
                DataSet.getNumberOfWorkersForHousehold(household),
                DataSet.getRestrictedMobility(household),
                household.getAutos(),
                origin.getAccessibility().get(Purpose.HBW),
                origin.getAccessibility().get(Purpose.HBS),
                origin.getAccessibility().get(Purpose.HBO),
                origin.getAccessibility().get(Purpose.NHBW),
                origin.getAccessibility().get(Purpose.NHBO),
                origin.getDistanceToNearestRailStop(),
                household.getEconomicStatus());
    }
}
