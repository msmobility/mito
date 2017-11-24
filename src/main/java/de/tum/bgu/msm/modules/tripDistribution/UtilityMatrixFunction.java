package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunction;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public class UtilityMatrixFunction implements ConcurrentFunction {

    private final static Logger logger = Logger.getLogger(UtilityMatrixFunction.class);

    private final TripDistributionJSCalculator calculator;
    private final Purpose purpose;
    private final Map<Integer, Zone> zones;
    private final TravelTimes travelTimes;
    private final Map<Purpose, Table<Integer, Integer, Double>> utilityMatrices;

    UtilityMatrixFunction(Purpose purpose, DataSet dataSet, Map<Purpose, Table<Integer, Integer, Double>> utilityMatrices) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelTimes = dataSet.getTravelTimes("car");
        this.utilityMatrices = utilityMatrices;
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TripDistribution"));
        calculator = new TripDistributionJSCalculator(reader);
    }

    @Override
    public void execute() {
        Table utilityMatrix = ArrayTable.create(zones.keySet(), zones.keySet());
        long counter = 0;
        for (Zone origin : zones.values()) {
            for (Zone destination : zones.values()) {
                final double travelTimeFromTo = travelTimes.getTravelTimeFromTo(origin.getZoneId(), destination.getZoneId());
                double utility = calculator.calculateUtility(destination, travelTimeFromTo, purpose);
                if (Double.isInfinite(utility)) {
                    throw new RuntimeException("Infinite utility calculated! Please check calculation!" +
                            " Origin: " + origin + " | Destination: " + destination +
                            " | Purpose: " + purpose);
                }
                utilityMatrix.put(origin.getZoneId(), destination.getZoneId(), /*Math.exp(*/utility);

                boolean log = Math.log10(counter) / Math.log10(2.) % 1 == 0;
                if (log) {
                    logger.info(counter + " OD pairs done for purpose " + purpose);
                }
                counter++;
            }
        }
        utilityMatrices.put(purpose, utilityMatrix);
        logger.info("Utility matrix for purpose " + purpose + " done.");
    }
}
