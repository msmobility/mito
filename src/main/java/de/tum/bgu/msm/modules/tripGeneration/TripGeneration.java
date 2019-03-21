package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.TravelDemandGenerator;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

/**
 * Runs trip generation for the Transport in Microsimulation Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TripGeneration extends Module {

    private static final Logger logger = Logger.getLogger(TravelDemandGenerator.class);
    private final boolean addAirportDemand;

    public TripGeneration(DataSet dataSet) {
        super(dataSet);
        addAirportDemand = Resources.INSTANCE.getBoolean(Properties.ADD_AIRPORT_DEMAND, false);
    }

    @Override
    public void run() {
        logger.info("  Started microscopic trip generation model.");
        generateRawTrips();
        if (addAirportDemand){
            generateAirportTrips();
        }
        calculateAttractions();
        balanceTrips();
        logger.info("  Completed microscopic trip generation model.");
    }

    private void generateRawTrips() {
        RawTripGenerator rawTripGenerator = new RawTripGenerator(dataSet);
        rawTripGenerator.run();
    }

    private void generateAirportTrips() {
        AirportTripGeneration airportTripGeneration = new AirportTripGeneration(dataSet);
        airportTripGeneration.run();
    }

    private void calculateAttractions() {
        AttractionCalculator calculator = new AttractionCalculator(dataSet);
        calculator.run();
    }

    private void balanceTrips() {
        TripBalancer tripBalancer = new TripBalancer(dataSet);
        tripBalancer.run();
    }

}
