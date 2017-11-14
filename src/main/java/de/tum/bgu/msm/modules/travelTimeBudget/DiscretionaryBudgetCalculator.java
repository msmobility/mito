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
        travelTimeCalc = new TravelTimeBudgetJSCalculator(reader, purpose.name());
    }

    @Override
    public void execute() {
        households.forEach(hh -> {
            travelTimeCalc.bindHousehold(hh);
            hh.setTravelTimeBudgetByPurpose(purpose, travelTimeCalc.calculate());
        });

    }
}
