package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.impedances.Impedance;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.io.input.readers.DestinationChoiceCoefficientReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class DestinationUtilityByPurposeGenerator implements Callable<Tuple<Purpose, IndexedDoubleMatrix2D>> {

    private final static Logger logger = Logger.getLogger(DestinationUtilityByPurposeGenerator.class);

    private final DestinationUtilityCalculator calculator;
    private final Purpose purpose;
    private final Map<Integer, MitoZone> zones;
    private final TravelDistances travelDistances;
    private final Map<String, Impedance> impedances;


    /**
     * The constructor for calibration
     * @param purpose
     * @param dataSet
     * @param factory
     * @param travelDistanceCalibrationK
     * @param impendanceCalibrationK
     */
    DestinationUtilityByPurposeGenerator(Purpose purpose, DataSet dataSet,
                                         DestinationUtilityCalculatorFactory factory,
                                         double travelDistanceCalibrationK,
                                         double impendanceCalibrationK) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelDistances = dataSet.getTravelDistancesNMT();
        this.impedances = dataSet.getImpedances();
        Map<String, Double> coefficients = new DestinationChoiceCoefficientReader(dataSet, purpose, Resources.instance.getDestinationChoiceCoefficients()).readCoefficientsForThisPurpose();
        coefficients.put(ExplanatoryVariable.calibrationFactorAlphaDistance, travelDistanceCalibrationK);
        coefficients.put(ExplanatoryVariable.calibrationFactorBetaExpDistance, impendanceCalibrationK);
        calculator = factory.createDestinationUtilityCalculator(purpose,coefficients);
    }

    /**
     * The constructor for models already calibrated
     * @param purpose
     * @param dataSet
     * @param factory
     */
    DestinationUtilityByPurposeGenerator(Purpose purpose, DataSet dataSet,
                                         DestinationUtilityCalculatorFactory factory) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelDistances = dataSet.getTravelDistancesNMT();
        this.impedances = dataSet.getImpedances();
        Map<String, Double> coefficients = new DestinationChoiceCoefficientReader(dataSet, purpose, Resources.instance.getDestinationChoiceCoefficients()).readCoefficientsForThisPurpose();
        calculator = factory.createDestinationUtilityCalculator(purpose,coefficients);
    }


    @Override
    public Tuple<Purpose, IndexedDoubleMatrix2D> call() {
        final IndexedDoubleMatrix2D utilityMatrix = new IndexedDoubleMatrix2D(zones.values(), zones.values());
        long counter = 0;
        for (MitoZone origin : zones.values()) {
            for (MitoZone destination : zones.values()) {
                Map<String, Double> variables  = new HashMap<>();
                variables.put(ExplanatoryVariable.logAttraction, destination.getTripAttraction(purpose));
                variables.put(ExplanatoryVariable.distance_km, travelDistances.getTravelDistance(origin.getId(), destination.getId()));
                variables.put(ExplanatoryVariable.tomTomOdIntensity, impedances.get(ExplanatoryVariable.tomTomOdIntensity).getTravelTime(origin, destination, 0, null));

                Map<String, Double> openDataExplanatoryVariables = destination.getOpenDataExplanatoryVariables();
                double numberOfTweets = openDataExplanatoryVariables.get(ExplanatoryVariable.numberOfTweets);
                double numberOfTweetsPerArea = openDataExplanatoryVariables.get(ExplanatoryVariable.numberOfTweetsPerArea);
                variables.put(ExplanatoryVariable.numberOfTweets, numberOfTweets);
                variables.put(ExplanatoryVariable.numberOfTweetsPerArea, numberOfTweetsPerArea);

                final double utility =  calculator.calculateExpUtility(variables);
                if (Double.isInfinite(utility) || Double.isNaN(utility)) {
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
