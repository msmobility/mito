package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class DestinationUtilityByPurposeGeneratorAggregate implements Callable<Tuple<Integer, IndexedDoubleMatrix2D>> {

    private final static Logger logger = Logger.getLogger(DestinationUtilityByPurposeGeneratorAggregate.class);

    private final DestinationUtilityCalculatorAggregate calculator;
    private final Purpose purpose;
    private final Map<Integer, MitoZone> zones;

    private final Map<Integer, MitoZone> origins;
    private final TravelDistances travelDistances;
    private final DataSet dataSet;
    private final EnumMap<Purpose, TravelDistances> logsum_EV;


    DestinationUtilityByPurposeGeneratorAggregate(MitoZone origins, Purpose purpose, DataSet dataSet,
                                                  DestinationUtilityCalculatorFactoryAggregate factory,
                                                  double travelDistanceCalibrationK,
                                                  double impendanceCalibrationK) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelDistances = dataSet.getTravelDistancesNMT();
        this.logsum_EV = dataSet.getLogsumByPurpose_EV();
        this.dataSet = dataSet;
        calculator = factory.createDestinationUtilityCalculator(purpose,travelDistanceCalibrationK, impendanceCalibrationK);
        this.origins = new HashMap<>();
        this.origins.put(1, origins);
    }

    @Override
    public Tuple<Integer, IndexedDoubleMatrix2D> call() {
        final IndexedDoubleMatrix2D probMatrix = new IndexedDoubleMatrix2D(origins.values(), dataSet.getZones().values());
        long counter = 0;
        int i = 0;
        double expLog = 0;
        //for (MitoZone origin : zones.values()) {
            for (MitoZone destination : zones.values()) {
                //Using Logsum
                final double expUtility =  calculator.calculateExpUtility(destination.getTripAttraction(purpose),
                        logsum_EV.get(purpose).getTravelDistance(origins.get(1).getId(), destination.getId()));

                if (Double.isInfinite(expUtility) || Double.isNaN(expUtility)) {
                    throw new RuntimeException(expUtility + " utility calculated! Please check calculation!" +
                            " Origin: " + origins.get(1).getId() + " | Destination: " + destination + " | Logsum: "
                            + logsum_EV.get(purpose).getTravelDistance(origins.get(1).getId(), destination.getId()) +
                            " | Purpose: " + purpose + " | attraction rate: " + destination.getTripAttraction(purpose));
                }
                //double probability = Math.exp(exp);
                probMatrix.setIndexed(origins.get(1).getId(), destination.getId(), expUtility);
                expLog += expUtility;
                //Using distance
/*
                final double utility =  calculator.calculateExpUtility(destination.getTripAttraction(purpose),
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
*/
                i++;


                if (LongMath.isPowerOfTwo(counter)) {
                    //logger.info(counter + " OD pairs done for purpose " + purpose);
                }
                counter++;
            }

        for (MitoZone destination : dataSet.getZones().values()) {
            probMatrix.setIndexed(origins.get(1).getId(), destination.getId(),
                    probMatrix.getIndexed(origins.get(1).getId(), destination.getId()) / expLog);
        }
        //logger.info("Utility matrix for purpose " + purpose + " done.");
        return new Tuple<>(origins.get(1).getId(), probMatrix);
    }
}
