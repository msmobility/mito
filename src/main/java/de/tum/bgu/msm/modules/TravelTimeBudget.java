package de.tum.bgu.msm.modules;

import com.pb.common.calculator.UtilityExpressionCalculator;
import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.MitoData;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.TripDataManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Runs calculation of travel time budget for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Apr 2, 2017 in Mannheim, Germany
 *
 */

public class TravelTimeBudget {

    protected static final String PROPERTIES_TRAVEL_TIME_BUDGET_UEC_FILE          = "travel.time.budget.UEC.File";
    protected static final String PROPERTIES_TRAVEL_TIME_BUDGET_UEC_DATA_SHEET    = "ttb.UEC.DataSheetNumber";
    protected static final String PROPERTIES_Total_Travel_Time_Budget_UEC_UTILITY = "total.ttb.UEC.Utility";
    protected static final String PROPERTIES_LOG_UTILITY_CALCULATION_TOTAL_TTB    = "log.util.total.ttb";

    private static Logger logger = Logger.getLogger(TravelTimeBudget.class);
    private ResourceBundle rb;
    private MitoData mitoData;
    private TripDataManager tripDataManager;
    private boolean logCalculation;
    private TravelTimeBudgetDMU totalTravelTimeBudgetDMU;
    private UtilityExpressionCalculator totalTtbUtility;
    private int numAltsTravelBudget;
    private int[] totalTtbAvail;


    public TravelTimeBudget (ResourceBundle rb, MitoData td, TripDataManager tripDataManager) {
        this.rb = rb;
        this.mitoData = td;
        this.tripDataManager = tripDataManager;
        setupTravelTimeBudgetModel();
    }


    private void setupTravelTimeBudgetModel() {
        // set up utility expression calculator for calculation of the travel time budget

        logger.info("  Creating Utility Expression Calculator for microscopic travel time budget calculation.");
        logCalculation  = ResourceUtil.getBooleanProperty(rb, PROPERTIES_LOG_UTILITY_CALCULATION_TOTAL_TTB);
        String uecFileName     = rb.getString(PROPERTIES_TRAVEL_TIME_BUDGET_UEC_FILE);
        int totalTtbSheetNumber = ResourceUtil.getIntegerProperty(rb, PROPERTIES_Total_Travel_Time_Budget_UEC_UTILITY);
        int dataSheetNumber = ResourceUtil.getIntegerProperty(rb, PROPERTIES_TRAVEL_TIME_BUDGET_UEC_DATA_SHEET);
        totalTtbUtility = new UtilityExpressionCalculator(new File(uecFileName),
                totalTtbSheetNumber,
                dataSheetNumber,
                ResourceUtil.changeResourceBundleIntoHashMap(rb),
                TravelTimeBudgetDMU.class);
        totalTravelTimeBudgetDMU = new TravelTimeBudgetDMU();

        // everything is available
        numAltsTravelBudget = totalTtbUtility.getNumberOfAlternatives();

        totalTtbAvail = new int[numAltsTravelBudget + 1];
        for (int i = 1; i < totalTtbAvail.length; i++) {
            totalTtbAvail[i] = 1;
        }
}


    public void calculateTravelTimeBudget () {
        // main method to calculate the travel time budget for every household
        logger.info("  Started microscopic travel time budget calculation.");
        calculateBudgetsForEachHousehold();
        logger.info("  Completed microscopic travel time budget calculation.");
    }


    private void calculateBudgetsForEachHousehold() {
        // loop over every household and calculate travel time budget by purpose
        for (MitoHousehold hh: MitoHousehold.getHouseholdArray()) {
            // calculate total travel time budget
            double totalTravelTimeBudget = calculateTotalTravelTimeBudget (hh);
            // calculate travel time budget for each discretionary trip purpose
            for (String purpose: mitoData.getPurposes()) {
                if (purpose.equals("HBW") || purpose.equals("HBE")) continue;  // work and school trips are given by work place and school place locations, no budget to be calculated


            }
        }
    }


    private double calculateTotalTravelTimeBudget (MitoHousehold hh) {
        // calculate total travel time budget for MitoHousehold hh

        // set DMU attributes
        totalTravelTimeBudgetDMU.setHouseholdSize(hh.getHhSize());
        totalTravelTimeBudgetDMU.setFemales(hh.getFemales());
        totalTravelTimeBudgetDMU.setChildren(hh.getChildren());
        totalTravelTimeBudgetDMU.setYoungAdults(hh.getYoungAdults());
        totalTravelTimeBudgetDMU.setRetirees(hh.getRetirees());
        totalTravelTimeBudgetDMU.setWorkers(hh.getNumberOfWorkers());
        totalTravelTimeBudgetDMU.setStudents(hh.getStudents());
        totalTravelTimeBudgetDMU.setCars(hh.getAutos());
        totalTravelTimeBudgetDMU.setLicenseHolders(hh.getLicenseHolders());
        totalTravelTimeBudgetDMU.setIncome(hh.getIncome());
        totalTravelTimeBudgetDMU.setAreaType(mitoData.getRegionOfZone(hh.getHomeZone()));  // todo: Ana, how is area type defined?

        int[] tripCounter = new int[mitoData.getPurposes().length];
        for(MitoTrip trip: hh.getTrips()) tripCounter[trip.getTripPurpose()]++;

        totalTravelTimeBudgetDMU.setTrips(tripCounter, mitoData);
//        double util[] = totalTtbUtility.solve(totalTravelTimeBudgetDMU.getDmuIndexValues(), totalTravelTimeBudgetDMU, totalTtbAvail);
        if (logCalculation) {
            // log UEC values for each person type
            logger.info("Household " + hh.getHhId() + " with " + hh.getHhSize() + " persons living in area type " +
                    mitoData.getRegionOfZone(hh.getHomeZone()));
            totalTtbUtility.logAnswersArray(logger,"Total Travel Time Budget");
        }
//        return util[0];
        return 0;
    }
}
