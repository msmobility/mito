package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.data.AverageBudget;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunctionExecutor;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

final class ChoiceUtilities {

    private final static Logger logger = Logger.getLogger(ChoiceUtilities.class);
    private final TravelTimes travelTimes;

    Map<Purpose, Table<Integer, Integer, Double>> utilityMatrices = Collections.synchronizedMap(new EnumMap<>(Purpose.class));
    Map<Purpose, AverageBudget> currentAverageTTB = Collections.synchronizedMap(new EnumMap<>(Purpose.class));


    private TripDistributionJSCalculator tripDistributionCalc;
    private DataSet dataSet;

    public ChoiceUtilities(DataSet dataSet) {
        this.dataSet = dataSet;
        travelTimes = dataSet.getTravelTimes("car");
        setupModel();
        buildMatrices();
        initializeAverageTTB();
    }

    private void initializeAverageTTB() {
        for (Purpose purpose : Purpose.values()) {
            currentAverageTTB.put(purpose, new AverageBudget(purpose.getAverageBudgetPerHousehold()));
        }
    }

    double getUtilityforRelationAndPurpose(int originId, int destinationId, Purpose purpose) {
        return utilityMatrices.get(purpose).get(originId, destinationId);
    }

    void setupModel() {
        logger.info("Creating Utility Expression Calculators for microscopic trip distribution.");
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TripDistribution"));
        tripDistributionCalc = new TripDistributionJSCalculator(reader);
    }

    void buildMatrices() {
        logger.info("Building initial utility matrices for Purposes...");
        ConcurrentFunctionExecutor executor = new ConcurrentFunctionExecutor();
        for (Purpose purpose : Purpose.values()) {
            executor.addFunction(() -> {
                Table utilityMatrix = HashBasedTable.create();
                for (Zone origin : dataSet.getZones().values()) {
                    for (Zone destination : dataSet.getZones().values()) {
                        double utility = calculateUtility(purpose, origin, destination);
                        utilityMatrix.put(origin.getZoneId(), destination.getZoneId(), utility);
                    }
                }
                utilityMatrices.put(purpose, utilityMatrix);
                logger.info("Utility matrix for purpose " + purpose + " done.");
            });
        }
        executor.execute();
    }

    private double calculateUtility(Purpose purpose, Zone origin, Zone destination) {
        tripDistributionCalc.setBaseZone(origin);
        final double travelTimeFromTo = travelTimes.getTravelTimeFromTo(origin.getZoneId(), destination.getZoneId());
        tripDistributionCalc.setTargetZone(destination, travelTimeFromTo);
        tripDistributionCalc.setPurpose(purpose);
        return tripDistributionCalc.calculate();
    }

    void updateUtilitiesForOriginAndPurpose(int originId, Purpose purpose) {
        double targetValue = purpose.getAverageBudgetPerHousehold();
        double actualValue = currentAverageTTB.get(purpose).getBudget();
        double multiplier =targetValue / actualValue;
        utilityMatrices.get(purpose).row(originId).replaceAll((key, value) -> value * multiplier);
    }

    public void addSelectedRelationForPurpose(Zone origin, Zone destination, Purpose purpose) {
        currentAverageTTB.get(purpose).addBudgetAndUpdate(travelTimes.getTravelTimeFromTo(origin.getZoneId(), destination.getZoneId()));
    }
}
