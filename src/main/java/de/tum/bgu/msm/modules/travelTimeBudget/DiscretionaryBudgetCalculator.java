package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;

import java.util.Collection;

public class DiscretionaryBudgetCalculator implements Runnable {

    private final Purpose purpose;
    private final Collection<MitoHousehold> households;
    private TravelTimeBudgetCalculator travelTimeCalc;

    public DiscretionaryBudgetCalculator(Purpose purpose, Collection<MitoHousehold> households) {
        this(purpose, households, new TravelTimeBudgetCalculatorImpl());
    }

    public DiscretionaryBudgetCalculator(Purpose purpose, Collection<MitoHousehold> households,
                                         TravelTimeBudgetCalculator travelTimeBudgetCalculator) {
        this.purpose = purpose;
        this.households = households;
        travelTimeCalc = travelTimeBudgetCalculator;
    }

    @Override
    public void run() {
        households.forEach(hh -> {
            double budget = travelTimeCalc.calculateBudget(hh, purpose.name());
            hh.setTravelTimeBudgetByPurpose(purpose, budget);
        });
    }
}
