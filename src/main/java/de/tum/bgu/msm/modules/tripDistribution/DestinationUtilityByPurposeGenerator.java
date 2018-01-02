package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.math.LongMath;
import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunction;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public class DestinationUtilityByPurposeGenerator implements ConcurrentFunction {

    private final static Logger logger = Logger.getLogger(DestinationUtilityByPurposeGenerator.class);

    private final DestinationUtilityJSCalculator calculator;
    private final Purpose purpose;
    private final Map<Integer, MitoZone> zones;
    private final TravelTimes travelTimes;
    private final Map<Purpose, Matrix> utilityMatrices;

    DestinationUtilityByPurposeGenerator(Purpose purpose, DataSet dataSet, Map<Purpose, Matrix> utilityMatrices) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelTimes = dataSet.getTravelTimes("car");
        this.utilityMatrices = utilityMatrices;
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TripDistribution"));
        calculator = new DestinationUtilityJSCalculator(reader);
    }

    @Override
    public void execute() {

        Matrix utilityMatrix = new Matrix(zones.keySet().size(), zones.keySet().size());
        long counter = 0;
        int[] numbering = new int[zones.size()+1];
        int i = 1;
        for(int id: zones.keySet()) {
            numbering[i] = id;
            i++;
        }
        utilityMatrix.setExternalNumbers(numbering);
        for (MitoZone origin : zones.values()) {
            for (MitoZone destination : zones.values()) {
                final double travelTime = travelTimes.getTravelTime(origin.getZoneId(), destination.getZoneId());
                final float utility = (float) getUtility(destination, travelTime);
                if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                    throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                            " Origin: " + origin + " | Destination: " + destination + " | Travel Time: " + travelTime +
                            " | Purpose: " + purpose);
                }
                utilityMatrix.setValueAt(origin.getZoneId(), destination.getZoneId(), utility);
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
