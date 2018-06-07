package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

class TravelTimeBudgetJSCalculator extends JavaScriptCalculator<Double> {

    TravelTimeBudgetJSCalculator(Reader reader) {
        super(reader);
    }

    Double calculateBudget(MitoHousehold household, String purpose) {
        return super.calculate("calculate",
                purpose,
                household.getHomeZone().getAreaTypeSG().code(),
                DataSet.getRetireesForHousehold(household),
                DataSet.getFemalesForHousehold(household),
                DataSet.getYoungAdultsForHousehold(household),
                DataSet.getNumberOfWorkersForHousehold(household),
                household.getAutos(),
                household.getHhSize(),
                household.getTripsForPurpose(Purpose.HBW).size(),
                household.getTripsForPurpose(Purpose.HBE).size(),
                household.getTripsForPurpose(Purpose.HBS).size(),
                household.getTripsForPurpose(Purpose.HBO).size(),
                household.getTripsForPurpose(Purpose.NHBW).size(),
                household.getTripsForPurpose(Purpose.NHBO).size(),
                household.getEconomicStatus());
    }
}
