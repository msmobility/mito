package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
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
                MitoUtil.getFemalesForHousehold(household),
                MitoUtil.getChildrenForHousehold(household),
                MitoUtil.getYoungAdultsForHousehold(household),
                MitoUtil.getRetireesForHousehold(household),
                MitoUtil.getNumberOfWorkersForHousehold(household),
                MitoUtil.getStudentsForHousehold(household),
                MitoUtil.getLicenseHoldersForHousehold(household),
                household.getAutos(),
                household.getIncome(),
                household.getHhSize(),
                household.getHhId(),
                household.getTripsForPurpose(Purpose.HBW).size(),
                household.getTripsForPurpose(Purpose.HBE).size(),
                household.getTripsForPurpose(Purpose.HBS).size(),
                household.getTripsForPurpose(Purpose.HBO).size(),
                household.getTripsForPurpose(Purpose.NHBW).size(),
                household.getTripsForPurpose(Purpose.NHBO).size());
    }
}
