package de.tum.bgu.msm.modules;

import com.pb.common.calculator.UtilityExpressionCalculator;
import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Runs calculation of travel time budget for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Apr 2, 2017 in Mannheim, Germany
 *
 */

public class TravelTimeBudget extends Module {



    private static Logger logger = Logger.getLogger(TravelTimeBudget.class);

    private ResourceBundle resources;

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


    public TravelTimeBudget(DataSet dataSet, ResourceBundle resources) {
        super(dataSet);
        this.resources = resources;
        setupTravelTimeBudgetModel();
    }

    @Override
    public void run() {
        calculateTravelTimeBudget();
    }


    private void setupTravelTimeBudgetModel() {
        // set up utility expression calculator for calculation of the travel time budget

        logger.info("  Creating Utility Expression Calculator for microscopic travel time budget calculation.");
        logCalculationTotalTtb = Properties.getBoolean(Properties.LOG_UTILITY_CALCULATION_TOTAL_TTB);
        logCalculationHbsTtb   = Properties.getBoolean(Properties.LOG_UTILITY_CALCULATION_HBS_TTB);
        logCalculationHboTtb   = Properties.getBoolean(Properties.LOG_UTILITY_CALCULATION_HBO_TTB);
        logCalculationNhbwTtb  = Properties.getBoolean(Properties.LOG_UTILITY_CALCULATION_NHBW_TTB);
        logCalculationNhboTtb  = Properties.getBoolean(Properties.LOG_UTILITY_CALCULATION_NHBO_TTB);
        String uecFileName     = Properties.getString(Properties.TRAVEL_TIME_BUDGET_UEC_FILE);
        int dataSheetNumber = Properties.getInt(Properties.TRAVEL_TIME_BUDGET_UEC_DATA_SHEET);
        int totalTtbSheetNumber = Properties.getInt(Properties.TOTAL_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        totalTtbUtility = new UtilityExpressionCalculator(new File(uecFileName), totalTtbSheetNumber,
                dataSheetNumber, resources, TravelTimeBudgetDMU.class);
        totalTravelTimeBudgetDMU = new TravelTimeBudgetDMU();
        int hbsTtbSheetNumber = Properties.getInt(Properties.HBS_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        hbsTtbUtility = new UtilityExpressionCalculator(new File(uecFileName), hbsTtbSheetNumber,
                dataSheetNumber, resources, TravelTimeBudgetDMU.class);
        hbsTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();
        int hboTtbSheetNumber = Properties.getInt(Properties.HBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        hboTtbUtility = new UtilityExpressionCalculator(new File(uecFileName), hboTtbSheetNumber,
                dataSheetNumber, resources, TravelTimeBudgetDMU.class);
        hboTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();
        int nhbwTtbSheetNumber = Properties.getInt(Properties.NHBW_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        nhbwTtbUtility = new UtilityExpressionCalculator(new File(uecFileName), nhbwTtbSheetNumber,
                dataSheetNumber, resources, TravelTimeBudgetDMU.class);
        nhbwTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();
        int nhboTtbSheetNumber = Properties.getInt(Properties.NHBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        nhboTtbUtility = new UtilityExpressionCalculator(new File(uecFileName), nhboTtbSheetNumber,
                dataSheetNumber, resources, TravelTimeBudgetDMU.class);
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
