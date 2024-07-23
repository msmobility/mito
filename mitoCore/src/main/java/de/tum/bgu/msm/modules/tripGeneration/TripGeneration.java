package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTripFactory;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.tripGeneration.airport.AirportTripGeneration;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Runs trip generation for the Transport in Microsimulation Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TripGeneration extends Module {

    private static final Logger logger = Logger.getLogger(TripGeneration.class);
    private final boolean addAirportDemand;
    private final Map<Purpose, TripGenerator> tripGeneratorByPurpose = new EnumMap<>(Purpose.class);
    private final Map<Purpose, AttractionCalculator> attractionCalculatorByPurpose = new EnumMap<>(Purpose.class);

    public TripGeneration(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet, purposes);
        //TODO: move to airport scenario
        addAirportDemand = Resources.instance.getBoolean(Properties.ADD_AIRPORT_DEMAND, false);
    }


    public void registerTripGenerator(Purpose purpose, MitoTripFactory mitoTripFactory, TripGeneratorType tripGeneratorType, TripGenPredictor tripGenerationCalculator, AttractionCalculator attractionCalculator) {
        TripGenerator tripsByPurposeGenerator;

        switch (tripGeneratorType){
            case SampleEnumeration:
                tripsByPurposeGenerator = new TripGeneratorSampleEnumeration(dataSet, purpose, mitoTripFactory);
                break;
            case HouseholdBasedHurdleNegBin:
                tripsByPurposeGenerator = new TripGeneratorHouseholdBasedHurdleNegBin(dataSet, purpose, mitoTripFactory, tripGenerationCalculator);
                break;
            case PersonBasedHurdleNegBin:
                tripsByPurposeGenerator = new TripGeneratorPersonBasedHurdleNegBin(dataSet, purpose, mitoTripFactory, tripGenerationCalculator);
                break;
            case PersonBasedHurdlePolr:
                tripsByPurposeGenerator = new TripGeneratorPersonBasedHurdlePolr(dataSet, purpose, mitoTripFactory, tripGenerationCalculator);
                break;
            default:
                logger.warn("Trip generator type is not given. The default generator: " + TripGeneratorPersonBasedHurdleNegBin.class.getName() + " will be applied.");
                tripsByPurposeGenerator = new TripGeneratorPersonBasedHurdleNegBin(dataSet, purpose, mitoTripFactory, tripGenerationCalculator);

        }

        final AttractionCalculator previous = attractionCalculatorByPurpose.put(purpose, attractionCalculator);
        if (previous != null) {
            logger.info("Overwrote attraction calculator for purpose " + purpose + " with " + attractionCalculator.getClass());
        }

        final TripGenerator prev = tripGeneratorByPurpose.put(purpose, tripsByPurposeGenerator);
        if (prev != null) {
            logger.info("Overwrote trip generator for purpose " + purpose + " with " + tripsByPurposeGenerator.getClass());
        }
    }

    @Override
    public void run() {
        logger.info("  Started microscopic trip generation model.");
        generateRawTrips();
        //TODO: move airport related codes to useCases/munich/scenarios
//        if (addAirportDemand){
//            generateAirportTrips(scaleFactorForTripGeneration);
//        }
        calculateAttractions();
        balanceTrips();
        logger.info("  Completed microscopic trip generation model.");
    }

    private void generateRawTrips() {
        RawTripGenerator rawTripGenerator = new RawTripGenerator(dataSet, tripGeneratorByPurpose, purposes);
        rawTripGenerator.run();
    }

    private void generateAirportTrips(double scaleFactorForTripGeneration) {
        AirportTripGeneration airportTripGeneration = new AirportTripGeneration(dataSet);
        airportTripGeneration.run(scaleFactorForTripGeneration);
    }

    private void calculateAttractions() {
        for (Purpose purpose : purposes){
            attractionCalculatorByPurpose.get(purpose).run();
        }
    }

    private void balanceTrips() {
        TripBalancer tripBalancer = new TripBalancer(dataSet, purposes);
        tripBalancer.run();
    }

}
