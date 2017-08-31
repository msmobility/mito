package de.tum.bgu.msm.modules.travelTimeBudget;

import com.pb.common.calculator2.UtilityExpressionCalculator;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.resources.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class TravelTimeBudgetCalculator {

    private static final Logger logger = Logger.getLogger(TravelTimeBudgetCalculator.class);

    private final DataSet dataSet;
    private final UtilityExpressionCalculator calculator;
    private final boolean log;
    private final String purpose;

    private final TravelTimeBudgetDMU ttbDMU;

    public TravelTimeBudgetCalculator(boolean log, String purpose, DataSet dataSet, int totalTtbSheetNumber) {
        this.log = log;
        this.purpose = purpose;
        this.dataSet = dataSet;
        this.ttbDMU = new TravelTimeBudgetDMU();
        String uecFileName = Resources.INSTANCE.getString(Properties.TRAVEL_TIME_BUDGET_UEC_FILE);
        int dataSheetNumber = Resources.INSTANCE.getInt(Properties.TRAVEL_TIME_BUDGET_UEC_DATA_SHEET);
        this.calculator = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, totalTtbSheetNumber, dataSheetNumber, ttbDMU);
    }

    public double calculateTTB(MitoHousehold hh, int[] totalTtbAvail) {
        setupDMU(hh);
        double util[] = calculator.solve(ttbDMU.getDmuIndexValues(), ttbDMU, totalTtbAvail);
        if (log) {
            log(hh);
        }
        return util[0];
    }

    private void setupDMU(MitoHousehold hh) {
        // set DMU attributes
        ttbDMU.setHouseholdSize(hh.getHhSize());
        ttbDMU.setFemales(MitoUtil.getFemalesForHousehold(hh));
        ttbDMU.setChildren(MitoUtil.getChildrenForHousehold(hh));
        ttbDMU.setYoungAdults(MitoUtil.getYoungAdultsForHousehold(hh));
        ttbDMU.setRetirees(MitoUtil.getRetireesForHousehold(hh));
        ttbDMU.setWorkers(MitoUtil.getNumberOfWorkersForHousehold(hh));
        ttbDMU.setStudents(MitoUtil.getStudentsForHousehold(hh));
        ttbDMU.setLicenseHolders(MitoUtil.getLicenseHoldersForHousehold(hh));
        ttbDMU.setCars(hh.getAutos());
        ttbDMU.setIncome(hh.getIncome());
        ttbDMU.setAreaType(hh.getHomeZone().getRegion());
        Map<Purpose, Integer> tripCounter = new HashMap<>();
        for (Purpose purpose : Purpose.values()) {
            tripCounter.put(purpose, hh.getTripsForPurpose(purpose).size());
        }
        ttbDMU.setTrips(tripCounter, dataSet);
    }

    private void log(MitoHousehold hh) {
        logger.info("Household " + hh.getHhId() + " with " + hh.getHhSize() + " persons living in area type " +
                hh.getHomeZone().getRegion());
        calculator.logAnswersArray(logger, purpose + " Travel Time Budget");
    }

    public int getNumberOfAlternatives() {
        return calculator.getNumberOfAlternatives();
    }


}
