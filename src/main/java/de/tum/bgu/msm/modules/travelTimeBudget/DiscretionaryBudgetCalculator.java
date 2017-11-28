package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunction;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;

public class DiscretionaryBudgetCalculator implements ConcurrentFunction {

    private final Purpose purpose;
    private final Collection<MitoHousehold> households;
    private TravelTimeBudgetJSCalculator travelTimeCalc;

    public DiscretionaryBudgetCalculator(Purpose purpose, Collection<MitoHousehold> households) {
        this.purpose = purpose;
        this.households = households;
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TravelTimeBudgetCalc"));
        travelTimeCalc = new TravelTimeBudgetJSCalculator(reader);
    }

    @Override
    public void execute() {
        households.forEach(hh -> {
            double budget = travelTimeCalc.calculateBudget(hh, purpose.name());
            hh.setTravelTimeBudgetByPurpose(purpose, budget);
        });
    }
}
