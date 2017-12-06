package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.collect.Table;
import de.tum.bgu.msm.data.AverageBudget;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunctionExecutor;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

final class ChoiceUtilities {

    private final static Logger logger = Logger.getLogger(ChoiceUtilities.class);

    Map<Purpose, Table<Integer, Integer, Double>> utilityMatrices = Collections.synchronizedMap(new EnumMap<>(Purpose.class));
    Map<Purpose, AverageBudget> currentAverageTTB = Collections.synchronizedMap(new EnumMap<>(Purpose.class));

    private DataSet dataSet;

    public ChoiceUtilities(DataSet dataSet) {
        this.dataSet = dataSet;
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

    public void addBudgetForPurpose(Purpose purpose, double budget) {
        currentAverageTTB.get(purpose).addBudgetAndUpdate(budget);
    }
}
