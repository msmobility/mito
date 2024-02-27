package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.readers.TripGenerationHurdleCoefficientReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.*;

import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.TRIP_ID_COUNTER;

public class TripGeneratorPersonBasedHurdlePolr extends RandomizableConcurrentFunction<Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>>> implements TripGenerator {

    private static final Logger logger = Logger.getLogger(TripGeneratorPersonBasedHurdlePolr.class);
    private Map<MitoHousehold, List<MitoTrip>> tripsByHH = new HashMap<>();

    private final DataSet dataSet;
    private final Purpose purpose;

    private double scaleFactorForGeneration;

    private final TripGenPredictor tripGenerationCalculator;

    private Map<String, Double> binLogCoef;
    private Map<String, Double> polrCoef;

    protected TripGeneratorPersonBasedHurdlePolr(DataSet dataSet, Purpose purpose, double scaleFactorForGeneration, TripGenPredictor tripGenerationCalculator) {
        super(MitoUtil.getRandomObject().nextLong());
        this.dataSet = dataSet;
        this.purpose = purpose;
        this.scaleFactorForGeneration = scaleFactorForGeneration;
        this.tripGenerationCalculator = tripGenerationCalculator;
        this.binLogCoef =
                new TripGenerationHurdleCoefficientReader(dataSet, purpose,
                        Resources.instance.getTripGenerationCoefficientsHurdleBinaryLogit()).readCoefficientsForThisPurpose();
        this.polrCoef =
                new TripGenerationHurdleCoefficientReader(dataSet, purpose,
                        Resources.instance.getTripGenerationCoefficientsHurdleOrderedLogit()).readCoefficientsForThisPurpose();

    }

    @Override
    public Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>> call() throws Exception {
        logger.info("  Generating trips with purpose " + purpose + " (multi-threaded)");
        logger.info("Created trip frequency distributions for " + purpose);
        logger.info("Started assignment of trips for hh, purpose: " + purpose);
        final Iterator<MitoHousehold> iterator = dataSet.getHouseholds().values().iterator();
        for (; iterator.hasNext(); ) {
            MitoHousehold next = iterator.next();
            if (MitoUtil.getRandomObject().nextDouble() < scaleFactorForGeneration) {
                generateTripsForHousehold(next);
            }
        }

        return new Tuple<>(purpose, tripsByHH);
    }

    private void generateTripsForHousehold(MitoHousehold hh) {
        for (MitoPerson person : hh.getPersons().values()) {
            int numberOfTrips = polrEstimateTrips(person);

            tripsByHH.putIfAbsent(hh, new ArrayList<>());
            List<MitoTrip> currentTrips = tripsByHH.get(hh);
            for (int i = 0; i < numberOfTrips; i++) {
                MitoTrip trip = new MitoTrip(TRIP_ID_COUNTER.incrementAndGet(), purpose);
                trip.setPerson(person);
                if (trip != null) {
                    currentTrips.add(trip);
                }
            }

            tripsByHH.put(hh, currentTrips);
        }
    }
    private int polrEstimateTrips (MitoPerson pp) {
        double randomNumber = random.nextDouble();
        double binaryUtility = tripGenerationCalculator.getPredictor(pp.getHousehold(),pp, binLogCoef);
        double phi = Math.exp(binaryUtility) / (1 + Math.exp(binaryUtility));
        double mu = tripGenerationCalculator.getPredictor(pp.getHousehold(),pp, polrCoef);

        double[] intercepts = new double[6];
        intercepts[0] = polrCoef.get("1|2");
        intercepts[1] = polrCoef.get("2|3");
        intercepts[2] = polrCoef.get("3|4");
        intercepts[3] = polrCoef.get("4|5");
        intercepts[4] = polrCoef.get("5|6");
        intercepts[5] = polrCoef.get("6|7");

        int i = 0;
        double cumProb = 0;
        double prob = 1 - phi;
        cumProb += prob;

        while(randomNumber > cumProb) {
            i++;
            if(i < 7) {
                prob = Math.exp(intercepts[i-1] - mu) / (1 + Math.exp(intercepts[i-1] - mu));
            } else {
                prob = 1;
            }
            if(i > 1) {
                prob -= Math.exp(intercepts[i-2] - mu) / (1 + Math.exp(intercepts[i-2] - mu));
            }
            cumProb += phi * prob;
        }
        return i;
    }
}
