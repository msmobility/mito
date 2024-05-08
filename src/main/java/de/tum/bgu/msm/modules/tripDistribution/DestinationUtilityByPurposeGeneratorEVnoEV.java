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

public class DestinationUtilityByPurposeGeneratorEVnoEV implements Callable<Tuple<Purpose, Tuple<IndexedDoubleMatrix2D, IndexedDoubleMatrix2D>>> {

    private final static Logger logger = Logger.getLogger(DestinationUtilityByPurposeGeneratorEVnoEV.class);

    private final DestinationUtilityCalculator calculator;
    private final Purpose purpose;
    private final Map<Integer, MitoZone> zones;
    private final TravelDistances travelDistances;
    private final DataSet dataSet;
    private final EnumMap<Purpose, TravelDistances> logsum_EV;
    private final EnumMap<Purpose, TravelDistances> logsum_NoEV;


    DestinationUtilityByPurposeGeneratorEVnoEV(Purpose purpose, DataSet dataSet,
                                               DestinationUtilityCalculatorFactory factory,
                                               double travelDistanceCalibrationK,
                                               double distanceCalibrationK) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelDistances = dataSet.getTravelDistancesNMT();
        this.logsum_EV = dataSet.getLogsumByPurpose_EV();
        this.logsum_NoEV = dataSet.getLogsumByPurpose_NoEV();
        this.dataSet = dataSet;
        calculator = factory.createDestinationUtilityCalculator(purpose,travelDistanceCalibrationK, distanceCalibrationK);
    }

    @Override
    public Tuple<Purpose, Tuple<IndexedDoubleMatrix2D, IndexedDoubleMatrix2D>> call() {
        IndexedDoubleMatrix2D utilityMatrixEV = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        IndexedDoubleMatrix2D utilityMatrixNoEV = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        long counter = 0;
        for (MitoZone origin : zones.values()) {
            for (MitoZone destination : zones.values()) {

                double utilityEV = calculator.calculateExpUtility2(destination.getTripAttraction(purpose),
                        dataSet.getLogsumByPurpose_EV().get(purpose).getTravelDistance(origin.getId(), destination.getId()),
                        travelDistances.getTravelDistance(origin.getId(), destination.getId()));
                double utilityNoEV = calculator.calculateExpUtility2(destination.getTripAttraction(purpose),
                        dataSet.getLogsumByPurpose_NoEV().get(purpose).getTravelDistance(origin.getId(), destination.getId()),
                        travelDistances.getTravelDistance(origin.getId(), destination.getId()));
                if (Double.isInfinite(utilityEV) || Double.isNaN(utilityEV)) {
                    throw new RuntimeException(utilityEV + " utility calculated! Please check calculation!" +
                            " Origin: " + origin + " | Destination: " + destination + " | Logsum EV: "
                            + logsum_EV.get(purpose).getTravelDistance(origin.getId(), destination.getId()) +
                            " | Purpose: " + purpose + " | attraction rate: " + destination.getTripAttraction(purpose));
                }
                if (Double.isInfinite(utilityNoEV) || Double.isNaN(utilityNoEV)) {
                    throw new RuntimeException(utilityEV + " utility calculated! Please check calculation!" +
                            " Origin: " + origin + " | Destination: " + destination + " | Logsum no EV: "
                            + logsum_EV.get(purpose).getTravelDistance(origin.getId(), destination.getId()) +
                            " | Purpose: " + purpose + " | attraction rate: " + destination.getTripAttraction(purpose));
                }
                utilityMatrixEV.setIndexed(origin.getId(), destination.getId(), utilityEV);
                utilityMatrixNoEV.setIndexed(origin.getId(), destination.getId(), utilityNoEV);


                if (LongMath.isPowerOfTwo(counter)) {
                    logger.info(counter + " OD pairs done for purpose " + purpose);
                }
                counter++;
            }
        }
        logger.info("Utility matrix for purpose " + purpose + " done.");
        return new Tuple<>(purpose, new Tuple<>(utilityMatrixEV, utilityMatrixNoEV));
    }
}
