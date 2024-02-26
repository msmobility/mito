package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.Module;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Runs calculation of travel time budget for the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Apr 2, 2017 in Mannheim, Germany
 */
public class TravelTimeBudgetModule extends Module {

    private static final Logger logger = Logger.getLogger(TravelTimeBudgetModule.class);

    private EnumSet<Purpose> discretionaryPurposes = EnumSet.of(Purpose.HBS, Purpose.HBO, Purpose.NHBW, Purpose.NHBO);
    private final TravelTimeBudgetCalculatorImpl travelTimeCalc;

    public TravelTimeBudgetModule(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet, purposes);
        travelTimeCalc = new TravelTimeBudgetCalculatorImpl();
    }

    @Override
    public void run() {
        calculateTravelTimeBudgets();
    }


    private void calculateTravelTimeBudgets() {
        logger.info("Started microscopic travel time budget calculation.");
        final ExecutorService service = Executors.newFixedThreadPool(Purpose.values().length);
        List<Future<?>> results = new ArrayList<>();
        for (Purpose purpose : purposes){
            if (Purpose.getDiscretionaryPurposes().contains(purpose)){
                results.add(service.submit(new DiscretionaryBudgetCalculator(purpose, dataSet.getHouseholds().values())));
            } else if (Purpose.getMandatoryPurposes().contains(purpose)){
                results.add(service.submit(new MandatoryBudgetCalculator(dataSet.getHouseholds().values(), purpose, dataSet.getTravelTimes(), dataSet.getPeakHour())));
                //results.add(service.submit((new MandatoryBudgetCalculator(dataSet.getHouseholds().values(), Purpose.HBE, dataSet.getTravelTimes(), dataSet.getPeakHour()))));
            }
        }
        service.shutdown();
        results.forEach(r -> {
            try {
                r.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                service.shutdownNow();
            }
        });

        logger.info("  Adjusting travel time budgets.");
        //adjustDiscretionaryPurposeBudgets();
        logger.info("  Finished microscopic travel time budget calculation.");

    }

    public void adjustDiscretionaryPurposeBudgets() {

        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            try {
                double totalTravelTimeBudget = travelTimeCalc.calculateBudget(household, "Total");
                double discretionaryTTB = totalTravelTimeBudget - household.getTravelTimeBudgetForPurpose(Purpose.HBW) -
                        household.getTravelTimeBudgetForPurpose(Purpose.HBE);
                discretionaryTTB = Math.max(discretionaryTTB, 0);

                double calcDiscretionaryTTB = 0;
                for (Purpose purpose : purposes) {
                    if (Purpose.getDiscretionaryPurposes().contains(purpose)) {
                        calcDiscretionaryTTB += household.getTravelTimeBudgetForPurpose(purpose);
                    }
                }
                for (Purpose purpose : purposes) {
                    if (Purpose.getDiscretionaryPurposes().contains(purpose)) {
                        double budget = household.getTravelTimeBudgetForPurpose(purpose);
                        if (budget != 0) {
                            budget = budget * discretionaryTTB / calcDiscretionaryTTB;
                            household.setTravelTimeBudgetByPurpose(purpose, budget);
                        }
                    }
                }

            } catch (NullPointerException e) {
                System.out.println("upps");
            }
        }
    }
}
