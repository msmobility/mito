package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.Reader;

public class TravelTimeBudgetJSCalculator extends JavaScriptCalculator<Double> {

    /**
    JavascriptCalculator implementation for calculating travel time budgets of households.
     */
    public TravelTimeBudgetJSCalculator(Reader reader, String initialPurpose) throws ScriptException, FileNotFoundException {
        super(reader);
        bindings.put("purpose", initialPurpose);
    }


    public void setPurpose(String purpose) {
        bindings.put("purpose", purpose);
    }


    public void bindHousehold(MitoHousehold household) {
        bindings.put("areaType", household.getHomeZone().getRegion());
        bindings.put("females", MitoUtil.getFemalesForHousehold(household));
        bindings.put("children", MitoUtil.getChildrenForHousehold(household));
        bindings.put("youngAdults", MitoUtil.getYoungAdultsForHousehold(household));
        bindings.put("retirees", MitoUtil.getRetireesForHousehold(household));
        bindings.put("workers", MitoUtil.getNumberOfWorkersForHousehold(household));
        bindings.put("students", MitoUtil.getStudentsForHousehold(household));
        bindings.put("cars", household.getAutos());
        bindings.put("licenses", MitoUtil.getLicenseHoldersForHousehold(household));
        bindings.put("income", household.getIncome());
        bindings.put("householdSize", household.getHhSize());
        bindings.put("hhId", household.getHhId());

        for(Purpose purpose: Purpose.values()) {
            bindings.put(purpose.name(), household.getTripsForPurpose(purpose).size());
        }
    }
}
