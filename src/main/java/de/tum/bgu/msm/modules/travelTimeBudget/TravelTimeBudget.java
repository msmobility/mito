package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Occupation;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import javax.script.ScriptException;
import java.io.*;
import java.util.EnumSet;

/**
 * Runs calculation of travel time budget for the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Apr 2, 2017 in Mannheim, Germany
 */

public class TravelTimeBudget extends Module {

    private static final Logger logger = Logger.getLogger(TravelTimeBudget.class);

    private TravelTimeBudgetJSCalculator travelTimeCalc;

    private final boolean logCalculationTotalTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_TOTAL_TTB);
    private final boolean logCalculationHbsTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_HBS_TTB);
    private final boolean logCalculationHboTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_HBO_TTB);
    private final boolean logCalculationNhbwTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_NHBW_TTB);
    private final boolean logCalculationNhboTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_NHBO_TTB);

    private EnumSet<Purpose> discretionaryPurposes = EnumSet.of(Purpose.HBS, Purpose.HBO, Purpose.NHBW, Purpose.NHBO);

    public TravelTimeBudget(DataSet dataSet) {
        super(dataSet);
        setupTravelTimeBudgetModel();
    }

    @Override
    public void run() {
        calculateTravelTimeBudget();
    }


    private void setupTravelTimeBudgetModel() {
        logger.info("  Creating Utility Expression Calculators for microscopic travel time budget calculation.");
        try {
            Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TravelTimeBudgetCalc"));
            travelTimeCalc = new TravelTimeBudgetJSCalculator(reader, "Total");
        } catch (ScriptException e) {
            logger.fatal("Error in input script!", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.fatal("Travel time budget script not found (property: \"ttb.js\")!", e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            logger.fatal("Travel time budget script not found (property: \"ttb.js\")!", e);
            e.printStackTrace();
        }
    }

    private void calculateTravelTimeBudget() {
        // main method to calculate the travel time budget for every household
        logger.info("  Started microscopic travel time budget calculation.");
        // loop over every household and calculate travel time budget by purpose
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            travelTimeCalc.setPurpose("Total");
            travelTimeCalc.bindHousehold(household);
            double totalTravelTimeBudget = travelTimeCalc.calculate(logCalculationTotalTtb);
            calculateDiscretionaryPurposeBudgets(household);
            calculateHBWBudgets(household);
            calculateHBEBudgets(household);
            adjustDiscretionaryPurposeBudget(household, totalTravelTimeBudget);
        }
        logger.info("  Finished microscopic travel time budget calculation.");
    }

    private void calculateHBWBudgets(MitoHousehold household) {
        double hbwBudget = 0;
        for (MitoPerson person : household.getPersons().values()) {
            if (person.getOccupation().equals(Occupation.WORKER)) {
                if(person.getWorkzone() == null) {
                    logger.warn("Worker with workzone null will not be considered for travel time budget.");
                    continue;
                }
                hbwBudget += dataSet.getAutoTravelTimes().getTravelTimeFromTo(household.getHomeZone(), person.getWorkzone());
            }
        }
        household.setTravelTimeBudgetByPurpose(Purpose.HBW, hbwBudget);
    }

    private void calculateHBEBudgets(MitoHousehold household) {
        double hbeBudget = 0;
        for (MitoPerson person : household.getPersons().values()) {
            if (person.getOccupation().equals(Occupation.STUDENT)) {
                if(person.getWorkzone() == null) {
                    logger.warn("Student with workzone null will not be considered for travel time budget.");
                    continue;
                }
                hbeBudget += dataSet.getAutoTravelTimes().getTravelTimeFromTo(household.getHomeZone(), person.getWorkzone());
            }
        }
        household.setTravelTimeBudgetByPurpose(Purpose.HBE, hbeBudget);
    }

    private void calculateDiscretionaryPurposeBudgets(MitoHousehold household) {
        travelTimeCalc.setPurpose(Purpose.HBS.name());
        travelTimeCalc.bindHousehold(household);
        household.setTravelTimeBudgetByPurpose(Purpose.HBS, travelTimeCalc.calculate(logCalculationHbsTtb));
        travelTimeCalc.setPurpose(Purpose.HBO.name());
        household.setTravelTimeBudgetByPurpose(Purpose.HBO, travelTimeCalc.calculate(logCalculationHboTtb));
        travelTimeCalc.setPurpose(Purpose.NHBW.name());
        household.setTravelTimeBudgetByPurpose(Purpose.NHBW, travelTimeCalc.calculate(logCalculationNhbwTtb));
        travelTimeCalc.setPurpose(Purpose.NHBO.name());
        household.setTravelTimeBudgetByPurpose(Purpose.NHBO, travelTimeCalc.calculate(logCalculationNhboTtb));
    }


    private void adjustDiscretionaryPurposeBudget(MitoHousehold household, double totalTravelTimeBudget) {
        double discretionaryTTB = totalTravelTimeBudget - household.getTravelTimeBudgetForPurpose(Purpose.HBW) -
                household.getTravelTimeBudgetForPurpose(Purpose.HBE);

        discretionaryTTB = Math.max(discretionaryTTB, 0);

        double calcDiscretionaryTTB = 0;
        for (Purpose purpose : discretionaryPurposes) {
            calcDiscretionaryTTB += household.getTravelTimeBudgetForPurpose(purpose);
        }
        for (Purpose purpose : discretionaryPurposes) {
            household.setTravelTimeBudgetByPurpose(purpose, household.getTravelTimeBudgetForPurpose(purpose) * discretionaryTTB / calcDiscretionaryTTB);
        }
    }
}
