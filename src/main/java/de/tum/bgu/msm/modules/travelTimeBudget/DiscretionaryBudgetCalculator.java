package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.concurrent.Callable;

public class DiscretionaryBudgetCalculator implements Callable<Void> {

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
    public Void call() {
        households.forEach(hh -> {
            double budget = travelTimeCalc.calculateBudget(hh, purpose.name());
            hh.setTravelTimeBudgetByPurpose(purpose, budget);
        });
        return null;
    }
}
