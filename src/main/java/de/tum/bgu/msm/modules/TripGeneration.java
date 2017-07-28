package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.TravelDemandGenerator;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.output.SummarizeData;
import de.tum.bgu.msm.io.output.TripGenerationWriter;
import de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator;
import de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator;
import de.tum.bgu.msm.modules.tripGeneration.TripBalancer;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Runs trip generation for the Transport in Microsimulation Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TripGeneration extends Module{

    private static final Logger logger = Logger.getLogger(TravelDemandGenerator.class);

    private Map<Integer, Map<String, Float>> tripAttr;

    public TripGeneration(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        logger.info("  Started microscopic trip generation model.");
        generateRawTrips();
        calculateAttractions();
        balanceTrips();
        writeResults();

        logger.info("  Completed microscopic trip generation model.");
    }

    private void generateRawTrips() {
        RawTripGenerator rawTripGenerator = new RawTripGenerator(dataSet);
        rawTripGenerator.run();
    }

    private void calculateAttractions() {
        AttractionCalculator calculator = new AttractionCalculator(dataSet);
        tripAttr =  calculator.run();
    }

    private void balanceTrips() {
        TripBalancer tripBalancer = new TripBalancer(dataSet, tripAttr);
        tripBalancer.run();
    }

    private void writeResults() {
        TripGenerationWriter.writeTripsByPurposeAndZone(dataSet, tripAttr);
        SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet);
    }

}
