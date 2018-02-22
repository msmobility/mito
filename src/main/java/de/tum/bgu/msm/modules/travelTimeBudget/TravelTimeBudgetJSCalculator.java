package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class TravelTimeBudgetJSCalculator extends JavaScriptCalculator<Double> {

    public TravelTimeBudgetJSCalculator(Reader reader) {
        super(reader);
    }

    public Double calculateBudget(MitoHousehold household, String purpose) {
        return super.calculate("calculate",
                purpose,
                household.getHomeZone().getAreaType().ordinal() + 1,
                DataSet.getFemalesForHousehold(household),
                DataSet.getChildrenForHousehold(household),
                DataSet.getYoungAdultsForHousehold(household),
                DataSet.getRetireesForHousehold(household),
                DataSet.getNumberOfWorkersForHousehold(household),
                DataSet.getStudentsForHousehold(household),
                DataSet.getLicenseHoldersForHousehold(household),
                household.getAutos(),
                household.getIncome(),
                household.getHhSize(),
                household.getId(),
                household.getTripsForPurpose(Purpose.HBW).size(),
                household.getTripsForPurpose(Purpose.HBE).size(),
                household.getTripsForPurpose(Purpose.HBS).size(),
                household.getTripsForPurpose(Purpose.HBO).size(),
                household.getTripsForPurpose(Purpose.NHBW).size(),
                household.getTripsForPurpose(Purpose.NHBO).size());
    }
}
