package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class DestinationUtilityByPurposeGenerator implements Callable<Tuple<Purpose, IndexedDoubleMatrix2D>> {

    private final static Logger logger = Logger.getLogger(DestinationUtilityByPurposeGenerator.class);

    private final DestinationUtilityCalculator calculator;
    private final Purpose purpose;
    private final Map<Integer, MitoZone> zones;
    private final TravelDistances travelDistances;
    private final DataSet dataSet;
    private final EnumMap<Purpose, TravelDistances> logsum;


    DestinationUtilityByPurposeGenerator(Purpose purpose, DataSet dataSet,
                                         DestinationUtilityCalculatorFactory factory,
                                         double travelDistanceCalibrationK,
                                         double impendanceCalibrationK) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelDistances = dataSet.getTravelDistancesNMT();
        this.logsum = dataSet.getLogsumByPurpose();
        this.dataSet = dataSet;
        calculator = factory.createDestinationUtilityCalculator(purpose,travelDistanceCalibrationK, impendanceCalibrationK);
    }

    @Override
    public Tuple<Purpose, IndexedDoubleMatrix2D> call() {
        final IndexedDoubleMatrix2D utilityMatrix = new IndexedDoubleMatrix2D(zones.values(), zones.values());
        long counter = 0;
        for (MitoZone origin : zones.values()) {
            for (MitoZone destination : zones.values()) {
                //Using Logsum

/*                final double utility =  calculator.calculateUtility(destination.getTripAttraction(purpose),
                        logsum.get(purpose).getTravelDistance(origin.getId(), destination.getId()));

                if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                    throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                            " Origin: " + origin + " | Destination: " + destination + " | Logsum: "
                            + logsum.get(purpose).getTravelDistance(origin.getId(), destination.getId()) +
                            " | Purpose: " + purpose + " | attraction rate: " + destination.getTripAttraction(purpose));
                }
                utilityMatrix.setIndexed(origin.getId(), destination.getId(), utility);*/

                //Using distance
                final double utility =  calculator.calculateUtility(destination.getTripAttraction(purpose),
                        travelDistances.getTravelDistance(origin.getId(), destination.getId()));
                if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                    System.out.println("Destination zone: " + destination + "for Purpose: " + purpose +
                            "has trip attraction of: " + destination.getTripAttraction(purpose));
                    throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                            " Origin: " + origin + " | Destination: " + destination + " | Distance: "
                            + travelDistances.getTravelDistance(origin.getId(), destination.getId()) +
                            " | Purpose: " + purpose + " | attraction rate: " + destination.getTripAttraction(purpose));
                }
                utilityMatrix.setIndexed(origin.getId(), destination.getId(), utility);



                if (LongMath.isPowerOfTwo(counter)) {
                    logger.info(counter + " OD pairs done for purpose " + purpose);
                }
                counter++;
            }
        }
        logger.info("Utility matrix for purpose " + purpose + " done.");
        return new Tuple<>(purpose, utilityMatrix);
    }
}
