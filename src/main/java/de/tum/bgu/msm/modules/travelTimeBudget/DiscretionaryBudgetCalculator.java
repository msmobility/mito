package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;

import java.util.Collection;

public class DiscretionaryBudgetCalculator implements Runnable {

    private final Purpose activityPurpose;
    private final Collection<MitoHousehold> households;
    private TravelTimeBudgetCalculator travelTimeCalc;

    public DiscretionaryBudgetCalculator(Purpose activityPurpose, Collection<MitoHousehold> households) {
        this(activityPurpose, households, new TravelTimeBudgetCalculatorImpl());
    }

    public DiscretionaryBudgetCalculator(Purpose activityPurpose, Collection<MitoHousehold> households,
                                         TravelTimeBudgetCalculator travelTimeBudgetCalculator) {
        this.activityPurpose = activityPurpose;
        this.households = households;
        travelTimeCalc = travelTimeBudgetCalculator;
    }

    @Override
    public void run() {
        households.forEach(hh -> {
            double budget = travelTimeCalc.calculateBudget(hh, activityPurpose.name());
            hh.setTravelTimeBudgetByPurpose(activityPurpose, budget);
        });
    }
}
