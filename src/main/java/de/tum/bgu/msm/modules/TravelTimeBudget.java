package de.tum.bgu.msm.modules;

import com.pb.common.calculator2.UtilityExpressionCalculator;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
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

    private boolean logCalculationTotalTtb;
    private boolean logCalculationHbsTtb;
    private boolean logCalculationHboTtb;
    private boolean logCalculationNhbwTtb;
    private boolean logCalculationNhboTtb;
    private TravelTimeBudgetDMU totalTravelTimeBudgetDMU;
    private TravelTimeBudgetDMU hbsTravelTimeBudgetDMU;
    private TravelTimeBudgetDMU hboTravelTimeBudgetDMU;
    private TravelTimeBudgetDMU nhbwTravelTimeBudgetDMU;
    private TravelTimeBudgetDMU nhboTravelTimeBudgetDMU;
    private UtilityExpressionCalculator totalTtbUtility;
    private UtilityExpressionCalculator hbsTtbUtility;
    private UtilityExpressionCalculator hboTtbUtility;
    private UtilityExpressionCalculator nhbwTtbUtility;
    private UtilityExpressionCalculator nhboTtbUtility;
    private int[] totalTtbAvail;


    public TravelTimeBudget(DataSet dataSet) {
        super(dataSet);
        setupTravelTimeBudgetModel();
    }

    @Override
    public void run() {
        calculateTravelTimeBudget();
    }


    private void setupTravelTimeBudgetModel() {
        // set up utility expression calculator for calculation of the travel time budget

        logger.info("  Creating Utility Expression Calculator for microscopic travel time budget calculation.");

        logCalculationTotalTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_TOTAL_TTB);
        logCalculationHbsTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_HBS_TTB);
        logCalculationHboTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_HBO_TTB);
        logCalculationNhbwTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_NHBW_TTB);
        logCalculationNhboTtb = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_NHBO_TTB);
        String uecFileName = Resources.INSTANCE.getString(Properties.TRAVEL_TIME_BUDGET_UEC_FILE);
        int dataSheetNumber = Resources.INSTANCE.getInt(Properties.TRAVEL_TIME_BUDGET_UEC_DATA_SHEET);
        int totalTtbSheetNumber = Resources.INSTANCE.getInt(Properties.TOTAL_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        totalTravelTimeBudgetDMU = new TravelTimeBudgetDMU();
        totalTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, totalTtbSheetNumber, dataSheetNumber, totalTravelTimeBudgetDMU);
//        totalTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator1(uecFileName, totalTtbSheetNumber, dataSheetNumber, TravelTimeBudgetDMU.class);
        int hbsTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBS_TRAVEL_TIME_BUDGET_UEC_UTILITY);

        hbsTravelTimeBudgetDMU = new TravelTimeBudgetDMU();
        hbsTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, hbsTtbSheetNumber, dataSheetNumber, hbsTravelTimeBudgetDMU);
//        hbsTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator1(uecFileName, hbsTtbSheetNumber, dataSheetNumber, TravelTimeBudgetDMU.class);
        int hboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        hboTravelTimeBudgetDMU = new TravelTimeBudgetDMU();
        hboTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, hboTtbSheetNumber, dataSheetNumber, hboTravelTimeBudgetDMU);
//        hboTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator1(uecFileName, hboTtbSheetNumber, dataSheetNumber, TravelTimeBudgetDMU.class);
        int nhbwTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBW_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        nhbwTravelTimeBudgetDMU = new TravelTimeBudgetDMU();
        nhbwTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, nhbwTtbSheetNumber, dataSheetNumber, nhbwTravelTimeBudgetDMU);
//        nhbwTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator1(uecFileName, nhbwTtbSheetNumber, dataSheetNumber, TravelTimeBudgetDMU.class);
        int nhboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        nhboTravelTimeBudgetDMU = new TravelTimeBudgetDMU();
        nhboTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, nhboTtbSheetNumber, dataSheetNumber, nhboTravelTimeBudgetDMU);
//        nhboTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator1(uecFileName, nhboTtbSheetNumber, dataSheetNumber, TravelTimeBudgetDMU.class);
//
        // everything is available
        int numAltsTravelBudget = totalTtbUtility.getNumberOfAlternatives();

        totalTtbAvail = new int[numAltsTravelBudget + 1];
        for (int i = 1; i < totalTtbAvail.length; i++) {
            totalTtbAvail[i] = 1;
        }
    }


    private void calculateTravelTimeBudget() {
        // main method to calculate the travel time budget for every household
        logger.info("  Started microscopic travel time budget calculation.");
        calculateBudgetsForEachHousehold();
        logger.info("  Finished microscopic travel time budget calculation.");
    }


    private void calculateBudgetsForEachHousehold() {

        TravelTimeBudgetCalculator totalTravelTimeCalc = new TravelTimeBudgetCalculator(totalTtbUtility, true, "Total", dataSet);
        TravelTimeBudgetCalculator hbsTravelTimeCalc = new TravelTimeBudgetCalculator(hbsTtbUtility, true, "HBS", dataSet);
        TravelTimeBudgetCalculator hboTravelTimeCalc = new TravelTimeBudgetCalculator(hboTtbUtility, true, "HBO", dataSet);
        TravelTimeBudgetCalculator nhbwTravelTimeCalc = new TravelTimeBudgetCalculator(nhbwTtbUtility, true, "NHBW", dataSet);
        TravelTimeBudgetCalculator nhboTravelTimeCalc = new TravelTimeBudgetCalculator(nhboTtbUtility, true, "NHBO", dataSet);

        // loop over every household and calculate travel time budget by purpose
        for (MitoHousehold household : dataSet.getHouseholds().values()) {

            // calculate total travel time budget
            double totalTravelTimeBudget = totalTravelTimeCalc.calculateTTB( household, totalTravelTimeBudgetDMU, totalTtbAvail);

            // calculate travel time budget for each discretionary trip purpose
            household.setTravelTimeBudgetByPurpose("HBS", hbsTravelTimeCalc.calculateTTB(household, hbsTravelTimeBudgetDMU, totalTtbAvail));
            household.setTravelTimeBudgetByPurpose("HBO", hboTravelTimeCalc.calculateTTB(household, hboTravelTimeBudgetDMU, totalTtbAvail));
            household.setTravelTimeBudgetByPurpose("NHBW", nhbwTravelTimeCalc.calculateTTB(household, nhbwTravelTimeBudgetDMU, totalTtbAvail));
            household.setTravelTimeBudgetByPurpose("NHBO", nhboTravelTimeCalc.calculateTTB(household, nhboTravelTimeBudgetDMU, totalTtbAvail));

            // work and school trips are given by work place and school place locations, no budget to be calculated
            // todo: sum up work and school trips of all household members to calculate those travel budgets
            for (MitoPerson person : household.getPersons()) {
                if (person.getOccupation() == 1) {
                    household.setTravelTimeBudgetByPurpose("HBW", dataSet.getAutoTravelTimeFromTo(household.getHomeZone(), person.getWorkzone()));
                }
            }

            double discretionaryTTB = totalTravelTimeBudget - household.getTravelTimeBudgetForPurpose("HBW") -
                    household.getTravelTimeBudgetForPurpose("HBE");

            discretionaryTTB = Math.max(discretionaryTTB, 0);

            String[] discretionaryPurposes = {"HBS", "HBO", "NHBW", "NHBO"};
            double calcDiscretionaryTTB = 0;
            for (String purp : discretionaryPurposes) {
                calcDiscretionaryTTB += household.getTravelTimeBudgetForPurpose(purp);
            }
            for (String purp : discretionaryPurposes) {
                household.setTravelTimeBudgetByPurpose(purp, household.getTravelTimeBudgetForPurpose(purp) * discretionaryTTB / calcDiscretionaryTTB);
            }
        }
    }

}
