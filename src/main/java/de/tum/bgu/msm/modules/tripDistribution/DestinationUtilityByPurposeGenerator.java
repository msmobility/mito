package de.tum.bgu.msm.modules.tripDistribution;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.matrices.Matrices;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunction;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.NavigableMap;

public class DestinationUtilityByPurposeGenerator implements ConcurrentFunction {

    private final static Logger logger = Logger.getLogger(DestinationUtilityByPurposeGenerator.class);

    private final DestinationUtilityJSCalculator calculator;
    private final Purpose purpose;
    private final NavigableMap<Integer, MitoZone> zones;
    private final TravelTimes travelTimes;
    private final Map<Purpose, DoubleMatrix2D> utilityMatrices;
    private final double timeOfDay;

    DestinationUtilityByPurposeGenerator(Purpose purpose, DataSet dataSet, Map<Purpose, DoubleMatrix2D> utilityMatrices, double timeOfDay) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelTimes = dataSet.getTravelTimes("car");
        this.utilityMatrices = utilityMatrices;
        this.timeOfDay = timeOfDay;

        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TripDistribution"));
        calculator = new DestinationUtilityJSCalculator(reader);
    }

    @Override
    public void execute() {

        DoubleMatrix2D utilityMatrix = Matrices.doubleMatrix2D(zones.values(), zones.values());
        long counter = 0;

        for (MitoZone origin : zones.values()) {
            for (MitoZone destination : zones.values()) {
                final double travelTime = travelTimes.getTravelTime(origin.getId(), destination.getId(), timeOfDay);
                final double utility = getUtility(destination, travelTime);
                if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                    throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                            " Origin: " + origin + " | Destination: " + destination + " | Travel Time: " + travelTime +
                            " | Purpose: " + purpose);
                }
                utilityMatrix.set(origin.getId(), destination.getId(), utility);
                if (LongMath.isPowerOfTwo(counter)) {
                    logger.info(counter + " OD pairs done for purpose " + purpose);
                }
                counter++;
            }
        }
        utilityMatrices.put(purpose, utilityMatrix);
        logger.info("Utility matrix for purpose " + purpose + " done.");
    }

    private double getUtility(MitoZone destination, double travelTimeFromTo) {
        switch (purpose) {
            case HBW:
                return calculator.calculateHbwUtility(destination, travelTimeFromTo);
            case HBE:
                return calculator.calculateHbeUtility(destination, travelTimeFromTo);
            case HBS:
                return calculator.calculateHbsUtility(destination, travelTimeFromTo);
            case HBO:
                return calculator.calculateHboUtility(destination, travelTimeFromTo);
            case NHBW:
                return calculator.calculateNhbwUtility(destination, travelTimeFromTo);
            case NHBO:
                return calculator.calculateNhboUtility(destination, travelTimeFromTo);
            default:
                throw new IllegalStateException();
        }
    }
}
