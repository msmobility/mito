package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.data.Occupation;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunctionExecutor;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.EnumSet;

/**
 * Runs calculation of travel time budget for the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Apr 2, 2017 in Mannheim, Germany
 */

public class TravelTimeBudgetModule extends Module {

    private static final Logger logger = Logger.getLogger(TravelTimeBudgetModule.class);

    private EnumSet<Purpose> discretionaryPurposes = EnumSet.of(Purpose.HBS, Purpose.HBO, Purpose.NHBW, Purpose.NHBO);
    private final TravelTimeBudgetJSCalculator travelTimeCalc;

    public TravelTimeBudgetModule(DataSet dataSet) {
        super(dataSet);
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TravelTimeBudgetCalc"));
        travelTimeCalc = new TravelTimeBudgetJSCalculator(reader);
    }

    @Override
    public void run() {
        calculateTravelTimeBudgets();
    }


    private void calculateTravelTimeBudgets() {
        logger.info("  Started microscopic travel time budget calculation.");
        ConcurrentFunctionExecutor executor = new ConcurrentFunctionExecutor();
        for(Purpose purpose: discretionaryPurposes) {
            executor.addFunction(new DiscretionaryBudgetCalculator(purpose, dataSet.getHouseholds().values()));
        }
        executor.addFunction(new MandatoryBudgetCalculator(dataSet.getHouseholds().values(), Purpose.HBW, dataSet.getTravelTimes("car"), dataSet.getPeakHour()));
        executor.addFunction(new MandatoryBudgetCalculator(dataSet.getHouseholds().values(), Purpose.HBE, dataSet.getTravelTimes("car"), dataSet.getPeakHour()));
        executor.execute();
        logger.info("  Adjusting travel time budgets.");
        adjustDiscretionaryPurposeBudgets();
        logger.info("  Finished microscopic travel time budget calculation.");

    }

    private void adjustDiscretionaryPurposeBudgets() {
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            double totalTravelTimeBudget = travelTimeCalc.calculateBudget(household, "Total");
            double discretionaryTTB = totalTravelTimeBudget - household.getTravelTimeBudgetForPurpose(Purpose.HBW) -
                    household.getTravelTimeBudgetForPurpose(Purpose.HBE);
            discretionaryTTB = Math.max(discretionaryTTB, 0);

            double calcDiscretionaryTTB = 0;
            for (Purpose purpose : discretionaryPurposes) {
                calcDiscretionaryTTB += household.getTravelTimeBudgetForPurpose(purpose);
            }
            for (Purpose purpose : discretionaryPurposes) {
                double budget = household.getTravelTimeBudgetForPurpose(purpose);
                if (budget != 0) {
                    budget = budget * discretionaryTTB / calcDiscretionaryTTB;
                    household.setTravelTimeBudgetByPurpose(purpose, budget);
                }
            }
        }
    }
}
