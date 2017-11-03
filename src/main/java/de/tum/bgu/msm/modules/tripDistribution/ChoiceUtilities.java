package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.collect.Table;
import de.tum.bgu.msm.data.AverageBudget;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunctionExecutor;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

final class ChoiceUtilities {

    private final static Logger logger = Logger.getLogger(ChoiceUtilities.class);
    private final TravelTimes travelTimes;

    Map<Purpose, Table<Integer, Integer, Double>> utilityMatrices = Collections.synchronizedMap(new EnumMap<>(Purpose.class));
    Map<Purpose, AverageBudget> currentAverageTTB = Collections.synchronizedMap(new EnumMap<>(Purpose.class));

    private DataSet dataSet;

    public ChoiceUtilities(DataSet dataSet) {
        this.dataSet = dataSet;
        travelTimes = dataSet.getTravelTimes("car");
        logger.info("Creating Utility Expression Calculators for microscopic trip distribution.");
        buildMatrices();
        initializeAverageTTB();
    }

    private void initializeAverageTTB() {
        for (Purpose purpose : Purpose.values()) {
            currentAverageTTB.put(purpose, new AverageBudget(purpose.getAverageBudgetPerHousehold()));
        }
    }

    Map<Purpose, Table<Integer, Integer, Double>> getUtilityMatrices() {
        return utilityMatrices;
    }

    void buildMatrices() {
        logger.info("Building initial utility matrices for Purposes...");
        ConcurrentFunctionExecutor executor = new ConcurrentFunctionExecutor();
        for (Purpose purpose : Purpose.values()) {
            executor.addFunction(new UtilityMatrixFunction(purpose, dataSet, utilityMatrices));
        }
        executor.execute();
    }

    /**
     * Adjusts the utility of every destination for given origin
     * and purpose to push the current average travel time budget
     * towards the results from the
     * {@link de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule}
     * by scaling each utility with a value >=0.
     * If the current average distributed budget is lower than the expected
     * budget, destinations with lower travel times should get a lower utility
     * and destinations requiring higher travel times should get an increase in
     * utility.
     * If the expected budget is lower, updates should be made the other way
     * round accordingly.
    */
    void updateUtilitiesForOriginAndPurpose(int originId, Purpose purpose) {
        double targetValue = purpose.getAverageBudgetPerHousehold();
        double actualValue = currentAverageTTB.get(purpose).getBudget();
        double signum = Math.signum(targetValue - actualValue);
        utilityMatrices.get(purpose).row(originId).replaceAll((key, value) -> {
            double travelTime = travelTimes.getTravelTimeFromTo(originId, key);
            double scale = Math.pow((travelTime +1) / (targetValue + 1), 0.1 * signum);
            return value * scale;
        });
    }


    public void addSelectedRelationForPurpose(Zone origin, Zone destination, Purpose purpose) {
        currentAverageTTB.get(purpose).addBudgetAndUpdate(travelTimes.getTravelTimeFromTo(origin.getZoneId(), destination.getZoneId()));
    }
}
