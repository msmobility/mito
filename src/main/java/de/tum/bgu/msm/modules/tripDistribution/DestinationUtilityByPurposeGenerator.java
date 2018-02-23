package de.tum.bgu.msm.modules.tripDistribution;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunction;
import de.tum.bgu.msm.util.matrices.Matrices;
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
    private final TravelDistances travelDistances;
    private final Map<Purpose, DoubleMatrix2D> utilityMatrices;

    DestinationUtilityByPurposeGenerator(Purpose purpose, DataSet dataSet, Map<Purpose, DoubleMatrix2D> utilityMatrices) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelDistances = dataSet.getTravelDistancesAuto();
        this.utilityMatrices = utilityMatrices;

        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TripDistribution"));
        calculator = new DestinationUtilityJSCalculator(reader);
    }

    @Override
    public void execute() {

        DoubleMatrix2D utilityMatrix = Matrices.doubleMatrix2D(zones.values(), zones.values());
        long counter = 0;

        for (MitoZone origin : zones.values()) {
            for (MitoZone destination : zones.values()) {
                final double travelTime = travelDistances.getTravelDistance(origin.getId(), destination.getId());
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

    private double getUtility(MitoZone destination, double travelDistance) {
        switch (purpose) {
            case HBW:
                return calculator.calculateHbwUtility(destination, travelDistance);
            case HBE:
                return calculator.calculateHbeUtility(destination, travelDistance);
            case HBS:
                return calculator.calculateHbsUtility(destination, travelDistance);
            case HBO:
                return calculator.calculateHboUtility(destination, travelDistance);
            case NHBW:
                return calculator.calculateNhbwUtility(destination, travelDistance);
            case NHBO:
                return calculator.calculateNhboUtility(destination, travelDistance);
            default:
                throw new IllegalStateException();
        }
    }
}
