package de.tum.bgu.msm.modules;

import com.pb.common.calculator.UtilityExpressionCalculator;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

/**
 * Runs calculation of travel time budget for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Apr 2, 2017 in Mannheim, Germany
 *
 */

public class TravelTimeBudget extends Module {



    private static Logger logger = Logger.getLogger(TravelTimeBudget.class);

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
    private int numAltsTravelBudget;
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
        logCalculationHbsTtb   = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_HBS_TTB);
        logCalculationHboTtb   = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_HBO_TTB);
        logCalculationNhbwTtb  = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_NHBW_TTB);
        logCalculationNhboTtb  = Resources.INSTANCE.getBoolean(Properties.LOG_UTILITY_CALCULATION_NHBO_TTB);
        String uecFileName     = Resources.INSTANCE.getString(Properties.TRAVEL_TIME_BUDGET_UEC_FILE);
        int dataSheetNumber = Resources.INSTANCE.getInt(Properties.TRAVEL_TIME_BUDGET_UEC_DATA_SHEET);
        int totalTtbSheetNumber = Resources.INSTANCE.getInt(Properties.TOTAL_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        totalTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator(uecFileName, totalTtbSheetNumber, dataSheetNumber, TravelTimeBudgetDMU.class);
        totalTravelTimeBudgetDMU = new TravelTimeBudgetDMU();
        int hbsTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBS_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        hbsTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator(uecFileName, hbsTtbSheetNumber, dataSheetNumber, TravelTimeBudgetDMU.class);
        hbsTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();
        int hboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        hboTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator(uecFileName, hboTtbSheetNumber, dataSheetNumber, TravelTimeBudgetDMU.class);
        hboTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();
        int nhbwTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBW_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        nhbwTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator(uecFileName, nhbwTtbSheetNumber, dataSheetNumber, TravelTimeBudgetDMU.class);
        nhbwTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();
        int nhboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        nhboTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator(uecFileName, nhboTtbSheetNumber, dataSheetNumber, TravelTimeBudgetDMU.class);
        nhboTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();

        // everything is available
        numAltsTravelBudget = totalTtbUtility.getNumberOfAlternatives();

        totalTtbAvail = new int[numAltsTravelBudget + 1];
        for (int i = 1; i < totalTtbAvail.length; i++) {
            totalTtbAvail[i] = 1;
        }
    }


    private void calculateTravelTimeBudget () {
        // main method to calculate the travel time budget for every household
        logger.info("  Started microscopic travel time budget calculation.");
        calculateBudgetsForEachHousehold();
        logger.info("  Finished microscopic travel time budget calculation.");
    }


    private void calculateBudgetsForEachHousehold() {
        // loop over every household and calculate travel time budget by purpose
        for (MitoHousehold household: dataSet.getHouseholds().values()) {

            // calculate total travel time budget
//            double[] travelTimeBudgetByPurp = new double[mitoData.getPurposes().length];
            double totalTravelTimeBudget = calculateTTB ("Total", household, totalTravelTimeBudgetDMU, totalTtbUtility, logCalculationTotalTtb);

            // calculate travel time budget for each discretionary trip purpose
            household.setTravelTimeBudgetByPurpose("HBS", calculateTTB ("HBS", household, hbsTravelTimeBudgetDMU, hbsTtbUtility, logCalculationHbsTtb));
            household.setTravelTimeBudgetByPurpose("HBO", calculateTTB ("HBO", household, hboTravelTimeBudgetDMU, hboTtbUtility, logCalculationHboTtb));
            household.setTravelTimeBudgetByPurpose("NHBW", calculateTTB ("NBHW", household, nhbwTravelTimeBudgetDMU, nhbwTtbUtility, logCalculationNhbwTtb));
            household.setTravelTimeBudgetByPurpose("NHBO", calculateTTB ("NHBO", household, nhboTravelTimeBudgetDMU, nhboTtbUtility, logCalculationNhboTtb));

            // work and school trips are given by work place and school place locations, no budget to be calculated
            // todo: sum up work and school trips of all household members to calculate those travel budgets
            for (MitoPerson person: household.getPersons()) {
                if (person.getOccupation() == 1) {
                    household.setTravelTimeBudgetByPurpose("HBW", dataSet.getAutoTravelTimeFromTo(household.getHomeZone(), person.getWorkzone()));
                }
            }

            double discretionaryTTB = totalTravelTimeBudget - household.getTravelTimeBudgetForPurpose("HBW") -
                    household.getTravelTimeBudgetForPurpose("HBE");

            discretionaryTTB = Math.max(discretionaryTTB, 0);

            String[] discretionaryPurposes = {"HBS", "HBO", "NHBW", "NHBO"};
            double calcDiscretionaryTTB = 0;
            for (String purp: discretionaryPurposes) {
                calcDiscretionaryTTB += household.getTravelTimeBudgetForPurpose(purp);
            }
            for (String purp: discretionaryPurposes) {
                household.setTravelTimeBudgetByPurpose(purp, household.getTravelTimeBudgetForPurpose(purp) * discretionaryTTB / calcDiscretionaryTTB);
            }
        }
    }

    private double calculateTTB (String purpose, MitoHousehold hh, TravelTimeBudgetDMU ttbDMU,
                                 UtilityExpressionCalculator ttbUtility, boolean logCalculationTtb) {
        // calculate  travel time budget for MitoHousehold hh

        // set DMU attributes
        ttbDMU.setHouseholdSize(hh.getHhSize());
        ttbDMU.setFemales(hh.getFemales());
        ttbDMU.setChildren(hh.getChildren());
        ttbDMU.setYoungAdults(hh.getYoungAdults());
        ttbDMU.setRetirees(hh.getRetirees());
        ttbDMU.setWorkers(hh.getNumberOfWorkers());
        ttbDMU.setStudents(hh.getStudents());
        ttbDMU.setCars(hh.getAutos());
        ttbDMU.setLicenseHolders(hh.getLicenseHolders());
        ttbDMU.setIncome(hh.getIncome());
        ttbDMU.setAreaType(dataSet.getZones().get(hh.getHomeZone()).getRegion());  // todo: Ana, how is area type defined?

        int[] tripCounter = new int[dataSet.getPurposes().length];
        for(MitoTrip trip: hh.getTrips()) {
            tripCounter[trip.getTripPurpose()]++;
        }

        ttbDMU.setTrips(tripCounter, dataSet);
        double util[] = ttbUtility.solve(ttbDMU.getDmuIndexValues(), ttbDMU, totalTtbAvail);
        if (logCalculationTtb) {
            // log UEC values for each person type
            logger.info("Household " + hh.getHhId() + " with " + hh.getHhSize() + " persons living in area type " +
                    dataSet.getZones().get(hh.getHomeZone()));
            ttbUtility.logAnswersArray(logger,purpose + " Travel Time Budget");
        }
        return util[0];
    }
}
