package de.tum.bgu.msm.modules;

import com.pb.common.calculator.UtilityExpressionCalculator;
import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.MitoData;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
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
    protected static final String PROPERTIES_HBS_Travel_Time_Budget_UEC_UTILITY   = "hbs.ttb.UEC.Utility";
    protected static final String PROPERTIES_HBO_Travel_Time_Budget_UEC_UTILITY   = "hbo.ttb.UEC.Utility";
    protected static final String PROPERTIES_NHBW_Travel_Time_Budget_UEC_UTILITY  = "nhbw.ttb.UEC.Utility";
    protected static final String PROPERTIES_NHBO_Travel_Time_Budget_UEC_UTILITY  = "nhbo.ttb.UEC.Utility";
    protected static final String PROPERTIES_LOG_UTILITY_CALCULATION_TOTAL_TTB    = "log.util.total.ttb";
    protected static final String PROPERTIES_LOG_UTILITY_CALCULATION_HBS_TTB      = "log.util.hbs.ttb";
    protected static final String PROPERTIES_LOG_UTILITY_CALCULATION_HBO_TTB      = "log.util.hbo.ttb";
    protected static final String PROPERTIES_LOG_UTILITY_CALCULATION_NHBW_TTB     = "log.util.nhbw.ttb";
    protected static final String PROPERTIES_LOG_UTILITY_CALCULATION_NHBO_TTB     = "log.util.nhbo.ttb";

    private static Logger logger = Logger.getLogger(TravelTimeBudget.class);
    private ResourceBundle rb;
    private MitoData mitoData;
    private TripDataManager tripDataManager;
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


    public TravelTimeBudget (ResourceBundle rb, MitoData td, TripDataManager tripDataManager) {
        this.rb = rb;
        this.mitoData = td;
        this.tripDataManager = tripDataManager;
        setupTravelTimeBudgetModel();
    }


    private void setupTravelTimeBudgetModel() {
        // set up utility expression calculator for calculation of the travel time budget

        logger.info("  Creating Utility Expression Calculator for microscopic travel time budget calculation.");
        logCalculationTotalTtb = ResourceUtil.getBooleanProperty(rb, PROPERTIES_LOG_UTILITY_CALCULATION_TOTAL_TTB);
        logCalculationHbsTtb   = ResourceUtil.getBooleanProperty(rb, PROPERTIES_LOG_UTILITY_CALCULATION_HBS_TTB);
        logCalculationHboTtb   = ResourceUtil.getBooleanProperty(rb, PROPERTIES_LOG_UTILITY_CALCULATION_HBO_TTB);
        logCalculationNhbwTtb  = ResourceUtil.getBooleanProperty(rb, PROPERTIES_LOG_UTILITY_CALCULATION_NHBW_TTB);
        logCalculationNhboTtb  = ResourceUtil.getBooleanProperty(rb, PROPERTIES_LOG_UTILITY_CALCULATION_NHBO_TTB);
        String uecFileName     = rb.getString(PROPERTIES_TRAVEL_TIME_BUDGET_UEC_FILE);
        int dataSheetNumber = ResourceUtil.getIntegerProperty(rb, PROPERTIES_TRAVEL_TIME_BUDGET_UEC_DATA_SHEET);
        int totalTtbSheetNumber = ResourceUtil.getIntegerProperty(rb, PROPERTIES_Total_Travel_Time_Budget_UEC_UTILITY);
        totalTtbUtility = new UtilityExpressionCalculator(new File(uecFileName), totalTtbSheetNumber,
                dataSheetNumber, rb, TravelTimeBudgetDMU.class);
        totalTravelTimeBudgetDMU = new TravelTimeBudgetDMU();
        int hbsTtbSheetNumber = ResourceUtil.getIntegerProperty(rb, PROPERTIES_HBS_Travel_Time_Budget_UEC_UTILITY);
        hbsTtbUtility = new UtilityExpressionCalculator(new File(uecFileName), hbsTtbSheetNumber,
                dataSheetNumber, rb, TravelTimeBudgetDMU.class);
        hbsTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();
        int hboTtbSheetNumber = ResourceUtil.getIntegerProperty(rb, PROPERTIES_HBO_Travel_Time_Budget_UEC_UTILITY);
        hboTtbUtility = new UtilityExpressionCalculator(new File(uecFileName), hboTtbSheetNumber,
                dataSheetNumber, rb, TravelTimeBudgetDMU.class);
        hboTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();
        int nhbwTtbSheetNumber = ResourceUtil.getIntegerProperty(rb, PROPERTIES_NHBW_Travel_Time_Budget_UEC_UTILITY);
        nhbwTtbUtility = new UtilityExpressionCalculator(new File(uecFileName), nhbwTtbSheetNumber,
                dataSheetNumber, rb, TravelTimeBudgetDMU.class);
        nhbwTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();
        int nhboTtbSheetNumber = ResourceUtil.getIntegerProperty(rb, PROPERTIES_NHBO_Travel_Time_Budget_UEC_UTILITY);
        nhboTtbUtility = new UtilityExpressionCalculator(new File(uecFileName), nhboTtbSheetNumber,
                dataSheetNumber, rb, TravelTimeBudgetDMU.class);
        nhboTravelTimeBudgetDMU   = new TravelTimeBudgetDMU();

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
        logger.info("  Finished microscopic travel time budget calculation.");
    }


    private void calculateBudgetsForEachHousehold() {
        // loop over every household and calculate travel time budget by purpose
        for (MitoHousehold hh: MitoHousehold.getHouseholdArray()) {
            // calculate total travel time budget
            double[] travelTimeBudgetByPurp = new double[mitoData.getPurposes().length];
            double totalTravelTimeBudget = calculateTTB ("Total", hh, totalTravelTimeBudgetDMU, totalTtbUtility, logCalculationTotalTtb);
            // calculate travel time budget for each discretionary trip purpose
            travelTimeBudgetByPurp[mitoData.getPurposeIndex("HBS")]  = calculateTTB ("HBS", hh, hbsTravelTimeBudgetDMU, hbsTtbUtility, logCalculationHbsTtb);
            travelTimeBudgetByPurp[mitoData.getPurposeIndex("HBO")]  = calculateTTB ("HBO", hh, hboTravelTimeBudgetDMU, hboTtbUtility, logCalculationHboTtb);
            travelTimeBudgetByPurp[mitoData.getPurposeIndex("NHBW")] = calculateTTB ("NBHW", hh, nhbwTravelTimeBudgetDMU, nhbwTtbUtility, logCalculationNhbwTtb);
            travelTimeBudgetByPurp[mitoData.getPurposeIndex("NHBO")] = calculateTTB ("NHBO", hh, nhboTravelTimeBudgetDMU, nhboTtbUtility, logCalculationNhboTtb);
            // work and school trips are given by work place and school place locations, no budget to be calculated
            // todo: sum up work and school trips of all household members to calculate those travel budgets
            for (MitoPerson pp: hh.getPersons()) {
//                if (pp.getOccupation() == 1) travelTimeBudgetByPurp[mitoData.getPurposeIndex("HBW")] +=
            }
            double discretionaryTTB = totalTravelTimeBudget - travelTimeBudgetByPurp[mitoData.getPurposeIndex("HBW")] -
                    travelTimeBudgetByPurp[mitoData.getPurposeIndex("HBE")];
            discretionaryTTB = Math.max(discretionaryTTB, 0);
            String[] discretionaryPurposes = {"HBS", "HBO", "NHBW", "NHBO"};
            double calcDiscretionaryTTB = 0;
            for (String purp: discretionaryPurposes) calcDiscretionaryTTB += travelTimeBudgetByPurp[mitoData.getPurposeIndex(purp)];
            for (String purp: discretionaryPurposes) travelTimeBudgetByPurp[mitoData.getPurposeIndex(purp)] =
                    travelTimeBudgetByPurp[mitoData.getPurposeIndex(purp)] * discretionaryTTB / calcDiscretionaryTTB;
            hh.setTravelTimeBudgetByPurpose(travelTimeBudgetByPurp);
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
        ttbDMU.setAreaType(mitoData.getRegionOfZone(hh.getHomeZone()));  // todo: Ana, how is area type defined?

        int[] tripCounter = new int[mitoData.getPurposes().length];
        for(MitoTrip trip: hh.getTrips()) tripCounter[trip.getTripPurpose()]++;

        ttbDMU.setTrips(tripCounter, mitoData);
        double util[] = ttbUtility.solve(ttbDMU.getDmuIndexValues(), ttbDMU, totalTtbAvail);
        if (logCalculationTtb) {
            // log UEC values for each person type
            logger.info("Household " + hh.getHhId() + " with " + hh.getHhSize() + " persons living in area type " +
                    mitoData.getRegionOfZone(hh.getHomeZone()));
            ttbUtility.logAnswersArray(logger,purpose + " Travel Time Budget");
        }
        return util[0];
    }

}
