package de.tum.bgu.msm.modules.travelTimeBudget;

import com.pb.common.calculator2.UtilityExpressionCalculator;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class TravelTimeBudgetCalculator {

    private static final Logger logger = Logger.getLogger(TravelTimeBudgetCalculator.class);

    private final DataSet dataSet;
    private final UtilityExpressionCalculator calculator;
    private final boolean log;
    private final Purpose purpose;

    private final TravelTimeBudgetDMU ttbDMU;

    public TravelTimeBudgetCalculator(boolean log, Purpose purpose, DataSet dataSet, int totalTtbSheetNumber) {
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
        Map<Purpose, Integer> tripCounter = new HashMap();
        for (Purpose purpose: Purpose.values()) {
            if (hh.getTripsByPurpose().containsKey(purpose)) {
                tripCounter.put(purpose, hh.getTripsByPurpose().get(purpose).size());
            } else {
                tripCounter.put(purpose,0);
            }
        }
        ttbDMU.setTrips(tripCounter, dataSet);
    }

    private void log(MitoHousehold hh) {
        logger.info("Household " + hh.getHhId() + " with " + hh.getHhSize() + " persons living in area type " +
                dataSet.getZones().get(hh.getHomeZone()).getRegion());
        calculator.logAnswersArray(logger, purpose + " Travel Time Budget");
    }

    public int getNumberOfAlternatives() {
        return calculator.getNumberOfAlternatives();
    }
}
