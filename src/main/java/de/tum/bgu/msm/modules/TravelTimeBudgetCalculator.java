package de.tum.bgu.msm.modules;

import com.pb.common.calculator2.UtilityExpressionCalculator;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import org.apache.log4j.Logger;

public class TravelTimeBudgetCalculator {

    private static final Logger logger = Logger.getLogger(TravelTimeBudgetCalculator.class);

    private final DataSet dataSet;
    private final UtilityExpressionCalculator calculator;
    private final boolean log;
    private final String purpose;

    public TravelTimeBudgetCalculator(UtilityExpressionCalculator calculator, boolean log, String purpose, DataSet dataSet) {
        this.calculator = calculator;
        this.log = log;
        this.purpose = purpose;
        this.dataSet = dataSet;
    }


    public double calculateTTB(MitoHousehold hh, TravelTimeBudgetDMU ttbDMU, int[] totalTtbAvail) {

        setupDMU(hh, ttbDMU);

        int[] tripCounter = new int[dataSet.getPurposes().length];

        for (int i = 0; i < dataSet.getPurposes().length; i++) {
            if (hh.getTripsByPurpose().containsKey(i)) {
                tripCounter[i] = hh.getTripsByPurpose().get(i).size();
            } else {
                tripCounter[i] = 0;
            }
        }

        ttbDMU.setTrips(tripCounter, dataSet);
        double util[] = calculator.solve(ttbDMU.getDmuIndexValues(), ttbDMU, totalTtbAvail);
        if (log) {
            // log UEC values for each person type
            logger.info("Household " + hh.getHhId() + " with " + hh.getHhSize() + " persons living in area type " +
                    dataSet.getZones().get(hh.getHomeZone()).getRegion());
            calculator.logAnswersArray(logger, purpose + " Travel Time Budget");
        }
        return util[0];
    }


    private void setupDMU(MitoHousehold hh, TravelTimeBudgetDMU ttbDMU) {
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
    }
}
