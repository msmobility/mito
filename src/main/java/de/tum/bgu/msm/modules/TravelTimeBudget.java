package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetCalculator;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

/**
 * Runs calculation of travel time budget for the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Apr 2, 2017 in Mannheim, Germany
 */

public class TravelTimeBudget extends Module {

    private static final Logger logger = Logger.getLogger(TravelTimeBudget.class);

    private TravelTimeBudgetCalculator totalTravelTimeCalc;
    private TravelTimeBudgetCalculator hbsTravelTimeCalc;
    private TravelTimeBudgetCalculator hboTravelTimeCalc;
    private TravelTimeBudgetCalculator nhbwTravelTimeCalc;
    private TravelTimeBudgetCalculator nhboTravelTimeCalc;

    private final boolean logCalculationTotalTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_TOTAL_TTB);
    private final boolean logCalculationHbsTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_HBS_TTB);
    private final boolean logCalculationHboTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_HBO_TTB);
    private final boolean logCalculationNhbwTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_NHBW_TTB);
    private final boolean logCalculationNhboTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_NHBO_TTB);

    private int[] totalTtbAvail;
    private final Purpose[] discretionaryPurposes = {Purpose.HBS, Purpose.HBO, Purpose.NHBW, Purpose.NHBO};

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
        int totalTtbSheetNumber = Resources.INSTANCE.getInt(Properties.TOTAL_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        totalTravelTimeCalc = new TravelTimeBudgetCalculator(logCalculationTotalTtb, Purpose.Total, dataSet, totalTtbSheetNumber);

        int hbsTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBS_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        hbsTravelTimeCalc = new TravelTimeBudgetCalculator(logCalculationHbsTtb, Purpose.HBS, dataSet, hbsTtbSheetNumber);

        int hboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        hboTravelTimeCalc = new TravelTimeBudgetCalculator(logCalculationHboTtb, Purpose.HBO, dataSet, hboTtbSheetNumber);

        int nhbwTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBW_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        nhbwTravelTimeCalc = new TravelTimeBudgetCalculator(logCalculationNhbwTtb, Purpose.NHBW, dataSet, nhbwTtbSheetNumber);

        int nhboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        nhboTravelTimeCalc = new TravelTimeBudgetCalculator(logCalculationNhboTtb, Purpose.NHBO, dataSet, nhboTtbSheetNumber);

        int numAltsTravelBudget = totalTravelTimeCalc.getNumberOfAlternatives();
        totalTtbAvail = new int[numAltsTravelBudget + 1];
        for (int i = 1; i < totalTtbAvail.length; i++) {
            totalTtbAvail[i] = 1;
        }
    }


    private void calculateTravelTimeBudget() {
        // main method to calculate the travel time budget for every household
        logger.info("  Started microscopic travel time budget calculation.");
        // loop over every household and calculate travel time budget by purpose
        for (MitoHousehold household : dataSet.getHouseholds().values()) {

            double totalTravelTimeBudget = totalTravelTimeCalc.calculateTTB(household, totalTtbAvail);
            calculateDiscretionaryPurposeBudgets(household);

            // work and school trips are given by work place and school place locations, no budget to be calculated
            // todo: sum up work and school trips of all household members to calculate those travel budgets
            for (MitoPerson person : household.getPersons()) {
                if (person.getOccupation() == 1) {
                    household.setTravelTimeBudgetByPurpose(Purpose.HBW, dataSet.getAutoTravelTimes().getTravelTimeFromTo(household.getHomeZone(), person.getWorkzone()));
                }
            }
            adjustDiscretionaryPurposeBudget(household, totalTravelTimeBudget);
        }
        logger.info("  Finished microscopic travel time budget calculation.");
    }

    private void calculateDiscretionaryPurposeBudgets(MitoHousehold household) {
        household.setTravelTimeBudgetByPurpose(Purpose.HBS, hbsTravelTimeCalc.calculateTTB(household, totalTtbAvail));
        household.setTravelTimeBudgetByPurpose(Purpose.HBO, hboTravelTimeCalc.calculateTTB(household, totalTtbAvail));
        household.setTravelTimeBudgetByPurpose(Purpose.NHBW, nhbwTravelTimeCalc.calculateTTB(household, totalTtbAvail));
        household.setTravelTimeBudgetByPurpose(Purpose.NHBO, nhboTravelTimeCalc.calculateTTB(household, totalTtbAvail));
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
