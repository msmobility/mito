package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.TravelDemandGenerator;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoAggregatePersona;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.tripGeneration.airport.AirportTripGeneration;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Runs trip generation for the Transport in Microsimulation Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TripGenerationAggregate extends Module {

    private static final Logger logger = Logger.getLogger(TravelDemandGenerator.class);
    private final boolean addAirportDemand;

    private double scaleFactorForTripGeneration;
    private final TripsByPurposeGeneratorFactoryAggregate tripsByPurposeGeneratorFactoryAggregate;

    private final MitoAggregatePersona persona;

    public TripGenerationAggregate(DataSet dataSet, TripsByPurposeGeneratorFactoryAggregate tripsByPurposeGeneratorFactoryAggregate, List<Purpose> purposes
            , MitoAggregatePersona persona) {
        super(dataSet, purposes);
        this.persona = persona;
        addAirportDemand = Resources.instance.getBoolean(Properties.ADD_AIRPORT_DEMAND, false);
        scaleFactorForTripGeneration = Resources.instance.getDouble(Properties.SCALE_FACTOR_FOR_TRIP_GENERATION, 1.0);
        this.tripsByPurposeGeneratorFactoryAggregate = tripsByPurposeGeneratorFactoryAggregate;
    }

    @Override
    public void run() {
        logger.info("  Started aggregate trip generation model.");
        generateRawTrips();
        calculateAttractions();
        balanceTrips();
        logger.info("  Completed aggregate trip generation model.");
    }

    private void generateRawTrips() {
        RawTripGeneratorAggregate rawTripGenerator = new RawTripGeneratorAggregate(dataSet, tripsByPurposeGeneratorFactoryAggregate, purposes, persona);
        rawTripGenerator.run(scaleFactorForTripGeneration, persona);
    }

    private void generateAirportTrips(double scaleFactorForTripGeneration) {
        AirportTripGeneration airportTripGeneration = new AirportTripGeneration(dataSet);
        airportTripGeneration.run(scaleFactorForTripGeneration);
    }

    private void calculateAttractions() {
        AttractionCalculator calculator = new AttractionCalculator(dataSet, purposes);
        calculator.run();
    }

    private void balanceTrips() {
        TripBalancer tripBalancer = new TripBalancer(dataSet, purposes);
        tripBalancer.run();
    }

}
