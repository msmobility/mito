package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunctionExecutor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tum.bgu.msm.data.Occupation.STUDENT;
import static de.tum.bgu.msm.data.Occupation.WORKER;
import static de.tum.bgu.msm.data.Purpose.*;


public class TripDistribution extends Module {

    final static AtomicInteger DISTRIBUTED_TRIPS_COUNTER = new AtomicInteger(0);
    final static AtomicInteger FAILED_TRIPS_COUNTER = new AtomicInteger(0);

    private AtomicInteger randomHbwDestinationTrips = new AtomicInteger(0);
    private AtomicInteger randomHbeDestinationTrips = new AtomicInteger(0);

    private AtomicInteger completelyRandomNhbWTrips = new AtomicInteger(0);
    private AtomicInteger completelyRandomNhboTrips = new AtomicInteger(0);

    private final ChoiceUtilities choiceUtilities;

    private final static Logger logger = Logger.getLogger(TripDistribution.class);

    public TripDistribution(DataSet dataSet) {
        super(dataSet);
        choiceUtilities = new ChoiceUtilities(dataSet);
    }

    @Override
    public void run() {
        logger.info("Assigning trips for households...");
        ConcurrentFunctionExecutor executor = new ConcurrentFunctionExecutor();
        executor.addFunction(new DiscretionaryTripDistributor(HBS, dataSet, choiceUtilities.getUtilityMatrices().get(HBS)));
        executor.addFunction(new DiscretionaryTripDistributor(HBO, dataSet, choiceUtilities.getUtilityMatrices().get(HBO)));
        executor.addFunction(new MandatoryTripDistributor(HBW, dataSet, choiceUtilities.getUtilityMatrices().get(HBW), WORKER));
        executor.addFunction(new MandatoryTripDistributor(HBE, dataSet, choiceUtilities.getUtilityMatrices().get(HBE), STUDENT));
        executor.execute();


        logger.info("Distributed: " + DISTRIBUTED_TRIPS_COUNTER + ", failed: " + FAILED_TRIPS_COUNTER);
        for (Purpose purpose : Purpose.values()) {
            logger.info("Average Budget for " + purpose + " reference: " + purpose.getAverageBudgetPerHousehold() + ", calculated: " + choiceUtilities.currentAverageTTB.get(purpose).getBudget());
        }
        logger.info("There have been " + randomHbwDestinationTrips + " HBW trips not done by a worker and " + randomHbeDestinationTrips
                + " HBE trips not done by a student. Picked a destination by random utility instead.");
        logger.info("There have been " + completelyRandomNhboTrips + " NHBO trips and " + completelyRandomNhbWTrips
                + " NHBW trips by persons who don't have a matching home based trip. Assumed a destination for a suitable home based"
                + " trip as either origin or destination for the non-home-based trip.");
    }

    private void distributeNHBW(MitoHousehold household) {
        List<MitoTrip> trips = household.getTripsForPurpose(NHBW);
        double usedBudget = 0;
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            Zone baseZone = null;
            for (MitoTrip hbwTrip : household.getTripsForPurpose(HBW)) {
                if (hbwTrip.getPerson().equals(trip.getPerson())) {
                    baseZone = hbwTrip.getTripDestination();
                    break;
                }
            }
            if (baseZone == null) {
                logger.trace("Could not find a previous home based work trip destination for nhbw trip. Picking by random utility.");
                completelyRandomNhbWTrips.incrementAndGet();
                Map<Zone, Double> probabilitiesAlt = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    double utility = choiceUtilities.getUtilityMatrices().get(HBW).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                    if (utility > 0) {
                        probabilitiesAlt.put(zone, utility);
                    }
                }
                baseZone = MitoUtil.select(probabilitiesAlt);
            }

            for (Zone zone : dataSet.getZones().values()) {
                double utility = choiceUtilities.getUtilityMatrices().get(NHBW).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                if (utility > 0) {
                    probabilities.put(zone, utility);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("No zone could be assigned by random utility");
                FAILED_TRIPS_COUNTER.incrementAndGet();
                continue;
            }
            Zone secondZone = MitoUtil.select(probabilities);
            DISTRIBUTED_TRIPS_COUNTER.incrementAndGet();
            if (MitoUtil.getRandomFloat() > 0.5f) {
                trip.setTripDestination(baseZone);
                trip.setTripOrigin(secondZone);
                usedBudget += dataSet.getTravelTimes("car").getTravelTime(baseZone.getZoneId(), secondZone.getZoneId());
            } else {
                trip.setTripOrigin(baseZone);
                trip.setTripDestination(secondZone);
                usedBudget += dataSet.getTravelTimes("car").getTravelTime(secondZone.getZoneId(), baseZone.getZoneId());
            }
        }
        if (usedBudget > 0) {
            choiceUtilities.addBudgetForPurpose(NHBW, usedBudget);
        }
    }

    private void distributeNHBO(MitoHousehold household) {
        List<MitoTrip> trips = household.getTripsForPurpose(NHBO);
        double usedBudget = 0;
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            Zone baseZone = null;
            List<Zone> possibleBaseZones = new ArrayList<>();
            for (MitoTrip hboTrip : household.getTripsForPurpose(HBO)) {
                if (hboTrip.getPerson().equals(trip.getPerson())) {
                    possibleBaseZones.add(hboTrip.getTripDestination());
                }
            }
            for (MitoTrip hbsTrip : household.getTripsForPurpose(HBS)) {
                if (hbsTrip.getPerson().equals(trip.getPerson())) {
                    possibleBaseZones.add(hbsTrip.getTripDestination());
                }
            }
            for (MitoTrip hbeTrip : household.getTripsForPurpose(HBE)) {
                if (hbeTrip.getPerson().equals(trip.getPerson())) {
                    possibleBaseZones.add(hbeTrip.getTripDestination());
                }
            }

            if (!possibleBaseZones.isEmpty()) {
                baseZone = possibleBaseZones.get((int) (MitoUtil.getRandomFloat() * (possibleBaseZones.size() - 1)));
            }

            if (baseZone == null) {
                logger.trace("Could not find a previous home based trip destination for nhbo trip. Picking by random utility.");
                completelyRandomNhboTrips.incrementAndGet();
                Purpose[] possibleHomeBasedPurposes = {HBE, HBO, HBS};
                Purpose selectedPurpose = possibleHomeBasedPurposes[MitoUtil.select(new double[]{1., 1., 1.})];

                Map<Zone, Double> probabilitiesAlt = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    double utility = choiceUtilities.getUtilityMatrices().get(selectedPurpose).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                    probabilitiesAlt.put(zone, utility);

                }
                baseZone = MitoUtil.select(probabilitiesAlt);
            }

            for (Zone zone : dataSet.getZones().values()) {
                double utility = choiceUtilities.getUtilityMatrices().get(NHBO).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                if (utility > 0) {
                    probabilities.put(zone, utility);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("No zone could be assigned by random utility");
                FAILED_TRIPS_COUNTER.incrementAndGet();
                continue;
            }
            Zone secondZone = MitoUtil.select(probabilities);
            DISTRIBUTED_TRIPS_COUNTER.incrementAndGet();
            if (MitoUtil.getRandomFloat() > 0.5f) {
                trip.setTripDestination(baseZone);
                trip.setTripOrigin(secondZone);
                usedBudget += dataSet.getTravelTimes("car").getTravelTime(secondZone.getZoneId(), baseZone.getZoneId());
            } else {
                trip.setTripOrigin(secondZone);
                trip.setTripDestination(baseZone);
                usedBudget += dataSet.getTravelTimes("car").getTravelTime(baseZone.getZoneId(), secondZone.getZoneId());
            }
        }
        if (usedBudget > 0) {
            choiceUtilities.addBudgetForPurpose(NHBO, usedBudget);
        }
    }
}
