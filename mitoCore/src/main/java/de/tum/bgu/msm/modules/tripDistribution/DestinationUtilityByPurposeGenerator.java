package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Callable;

public class DestinationUtilityByPurposeGenerator implements Callable<Triple<Purpose,Integer, IndexedDoubleMatrix2D>> {

    private final static Logger logger = Logger.getLogger(DestinationUtilityByPurposeGenerator.class);

    private final AbstractDestinationUtilityCalculator calculator;
    private final Purpose purpose;
    private final Map<Integer, MitoZone> zones;
    private final TravelDistances travelDistances;
    private final int categoryIndex;


    DestinationUtilityByPurposeGenerator(Purpose purpose, DataSet dataSet, AbstractDestinationUtilityCalculator calculator, int categoryIndex) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelDistances = dataSet.getTravelDistancesNMT();
        this.categoryIndex = categoryIndex;
        this.calculator = calculator;
    }

    @Override
    public ImmutableTriple<Purpose, Integer, IndexedDoubleMatrix2D> call() {
        final IndexedDoubleMatrix2D utilityMatrix = new IndexedDoubleMatrix2D(zones.values(), zones.values());
        for (MitoZone origin : zones.values()) {
            for (MitoZone destination : zones.values()) {
                final double utility =  calculator.calculateUtility(destination.getTripAttraction(purpose),
                        travelDistances.getTravelDistance(origin.getId(), destination.getId()),categoryIndex);
                if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                    throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                            " Origin: " + origin + " | Destination: " + destination + " | Distance: "
                            + travelDistances.getTravelDistance(origin.getId(), destination.getId()) +
                            " | Purpose: " + purpose + " | attraction rate: " + destination.getTripAttraction(purpose));
                }
                utilityMatrix.setIndexed(origin.getId(), destination.getId(), utility);
            }
        }
        logger.info("Utility matrix for purpose " + purpose + " category " + categoryIndex + " done.");
        return new ImmutableTriple<>(purpose, categoryIndex, utilityMatrix);
    }
}
