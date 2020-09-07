package de.tum.bgu.msm.modules.tripGeneration;

import cern.jet.random.tdouble.NegativeBinomial;
import cern.jet.random.tdouble.engine.DoubleRandomEngine;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;
import umontreal.ssj.probdist.NegativeBinomialDist;

import java.util.*;

import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.DROPPED_TRIPS_AT_BORDER_COUNTER;
import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.TRIP_ID_COUNTER;

public class TripsByPurposeGeneratorHurdleModel extends RandomizableConcurrentFunction<Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>>> implements TripsByPurposeGenerator {

    private static final Logger logger = Logger.getLogger(TripsByPurposeGeneratorHurdleModel.class);
    private Map<MitoHousehold, List<MitoTrip>> tripsByHH = new HashMap<>();

    private final DataSet dataSet;
    private final Purpose purpose;

    private double scaleFactorForGeneration;
    private HouseholdTypeManager householdTypeManager;


    protected TripsByPurposeGeneratorHurdleModel(DataSet dataSet, Purpose purpose, double scaleFactorForGeneration) {
        super(MitoUtil.getRandomObject().nextLong());
        this.dataSet = dataSet;
        this.purpose = purpose;
        this.scaleFactorForGeneration = scaleFactorForGeneration;
        this.householdTypeManager = new HouseholdTypeManager(purpose);
    }

    @Override
    public Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>> call() throws Exception {
        logger.info("  Generating trips with purpose " + purpose + " (multi-threaded)");
        logger.info("Created trip frequency distributions for " + purpose);
        logger.info("Started assignment of trips for hh, purpose: " + purpose);
        final Iterator<MitoHousehold> iterator = dataSet.getHouseholds().values().iterator();
        for (; iterator.hasNext(); ) {
            MitoHousehold next = iterator.next();
            generateTripsForHousehold(next, scaleFactorForGeneration);
        }
        return new Tuple<>(purpose, tripsByHH);
    }

    private void generateTripsForHousehold(MitoHousehold hh, double scaleFactorForGeneration) {
        double utilityTravel = -5;

        utilityTravel += hh.getHhSize();
        int economicStatus = hh.getEconomicStatus();
        switch (economicStatus){
            case 2:
                utilityTravel += 0.18143;
            case 3:
                utilityTravel += 0.58207;
            case 4:
                utilityTravel += 0.66194;
            case 5:
                utilityTravel += 0.72070;
        }

        utilityTravel += 0.15782 * hh.getAutos() / hh.getHhSize();

        if (random.nextDouble() < Math.exp(utilityTravel)/ (1 + Math.exp(utilityTravel))){
            estimateNumberOfTrips(hh);
        }
    }

    private void estimateNumberOfTrips(MitoHousehold hh) {
        double estimation = 0.;
        estimation += 0.4 * hh.getHhSize();
        int economicStatus = hh.getEconomicStatus();
        switch (economicStatus){
            case 2:
                estimation += 0.5;
            case 3:
                estimation += 0.56;
            case 4:
                estimation += 0.63;
            case 5:
                estimation += 0.73;
        }

        double theta = 14.58130;

        double variance = estimation + 1 / theta * Math.pow(estimation, 2);

        double p = (variance - estimation) / variance;

        int numberOfTrips = NegativeBinomialDist.inverseF(theta, 1 - p, random.nextDouble());

        generateTripsForHousehold(hh, scaleFactorForGeneration, numberOfTrips);

    }

    private void generateTripsForHousehold(MitoHousehold hh, double scaleFactorForGeneration, int numberOfTrips) {
        HouseholdType hhType = householdTypeManager.determineHouseholdType(hh);
        if (hhType == null) {
            logger.error("Could not create trips for Household " + hh.getId() + " for Purpose " + purpose + ": No Household Type applicable");
            return;
        }
        Integer[] tripFrequencies = householdTypeManager.getTripFrequenciesForHouseholdType(hhType);
        if (tripFrequencies == null) {
            logger.error("Could not find trip frequencies for this hhType/Purpose: " + hhType.getId() + "/" + purpose);
            return;
        }
        if (MitoUtil.getSum(tripFrequencies) == 0) {
            logger.info("No trips for this hhType/Purpose: " + hhType.getId() + "/" + purpose);
            return;
        }

        List<MitoTrip> trips = new ArrayList<>();
        for (int i = 0; i < numberOfTrips; i++) {
            if (MitoUtil.getRandomObject().nextDouble() < scaleFactorForGeneration){
                MitoTrip trip =  new MitoTrip(TRIP_ID_COUNTER.incrementAndGet(), purpose);;
                if (trip != null) {
                    trips.add(trip);
                }
            }

        }
        tripsByHH.put(hh, trips);
    }





}
