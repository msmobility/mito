package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
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

    private AtomicInteger distributedTripsCounter = new AtomicInteger(0);
    private AtomicInteger failedTripsCounter = new AtomicInteger(0);

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
        long counter = 0;
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            if(Math.log10(counter) / Math.log10(2.) % 1 == 0) {
                logger.info(counter + " households done.");
            }
            distributeHBW(household);
            distributeHBE(household);
            distributeHBS(household);
            distributeHBO(household);
            distributeNHBW(household);
            distributeNHBO(household);
            counter++;
        }
        logger.info("Distributed: " + distributedTripsCounter + ", failed: " + failedTripsCounter);
        for (Purpose purpose : Purpose.values()) {
            logger.info("Average Budget for " + purpose + " reference: " + purpose.getAverageBudgetPerHousehold() + ", calculated: " + choiceUtilities.currentAverageTTB.get(purpose).getBudget());
        }
        logger.info("There have been " + randomHbwDestinationTrips + " HBW trips not done by a worker and "+ randomHbeDestinationTrips
                + " HBE trips not done by a student. Picked a destination by random utility instead.");
        logger.info("There have been " + completelyRandomNhboTrips + " NHBO trips and " + completelyRandomNhbWTrips
                + " NHBW trips by persons who don't have a matching home based trip. Assumed a destination for a suitable home based"
                + " trip as either origin or destination for the non-home-based trip.");
    }


    private void distributeHBW(MitoHousehold household) {
        for (MitoTrip trip : household.getTripsForPurpose(HBW)) {
            trip.setTripOrigin(household.getHomeZone());
            if (trip.getPerson().getOccupation() == WORKER && trip.getPerson().getWorkzone() != null) {
                trip.setTripDestination(trip.getPerson().getWorkzone());
                distributedTripsCounter.incrementAndGet();
            } else {
                logger.trace(trip + " is not done by a worker (or invalid workzone). Selecting zone by total employment utility");
                randomHbwDestinationTrips.incrementAndGet();
                Map<Zone, Double> probabilities = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    double utility = choiceUtilities.getUtilityMatrices().get(HBW).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                    if (utility > 0) {
                        probabilities.put(zone, utility);
                    }
                }
                if (probabilities.isEmpty()) {
                    logger.warn("Could not find destination for trip " + trip);
                    failedTripsCounter.incrementAndGet();
                    continue;
                }
                Zone destination = MitoUtil.select(probabilities);
                distributedTripsCounter.incrementAndGet();
                trip.setTripDestination(destination);
            }
        }
    }

    private void distributeHBE(MitoHousehold household) {
        for (MitoTrip trip : household.getTripsForPurpose(HBE)) {
            trip.setTripOrigin(household.getHomeZone());
            if (trip.getPerson().getOccupation() == STUDENT && trip.getPerson().getWorkzone() != null) {
                trip.setTripDestination(trip.getPerson().getWorkzone());
                distributedTripsCounter.incrementAndGet();
            } else {
                logger.trace(trip + " is not done by a student (or invalid workzone). Selecting zone by school enrollment utility");
                randomHbeDestinationTrips.incrementAndGet();
                Map<Zone, Double> probabilities = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    double utility = choiceUtilities.getUtilityMatrices().get(HBE).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                    if (utility > 0) {
                        probabilities.put(zone, utility);
                    }
                }
                if (probabilities.isEmpty()) {
                    logger.warn("Could not find destination for trip " + trip);
                    failedTripsCounter.incrementAndGet();
                    continue;
                }
                Zone destination = MitoUtil.select(probabilities);
                distributedTripsCounter.incrementAndGet();
                trip.setTripDestination(destination);
            }
        }
    }

    private void distributeHBS(MitoHousehold household) {
        List<MitoTrip> trips = household.getTripsForPurpose(HBS);
        if(trips.isEmpty()) {
            return;
        }
        choiceUtilities.updateUtilitiesForOriginAndPurpose(household.getHomeZone().getZoneId(), HBS, trips.size());
        double usedBudget = 0;
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            for (Zone zone : dataSet.getZones().values()) {
                double utility = choiceUtilities.getUtilityMatrices().get(HBS).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                if (utility > 0) {
                    probabilities.put(zone, utility);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("Could not find destination for trip " + trip);
                failedTripsCounter.incrementAndGet();
                continue;
            }
            Zone destination = MitoUtil.select(probabilities);
            trip.setTripDestination(destination);
            distributedTripsCounter.incrementAndGet();
            usedBudget += dataSet.getTravelTimes("car").getTravelTimeFromTo(household.getHomeZone().getZoneId(), destination.getZoneId());
        }
        if (usedBudget > 0) {
            choiceUtilities.addBudgetForPurpose(HBS, usedBudget);
        }
    }

    private void distributeHBO(MitoHousehold household) {
        List<MitoTrip> trips = household.getTripsForPurpose(HBO);
        if(trips.isEmpty()) {
            return;
        }
        choiceUtilities.updateUtilitiesForOriginAndPurpose(household.getHomeZone().getZoneId(), HBO, trips.size());
        double usedBudget = 0;
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            trip.setTripOrigin(household.getHomeZone());
            for (Zone zone : dataSet.getZones().values()) {
                double utility = choiceUtilities.getUtilityMatrices().get(HBO).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                if (utility > 0) {
                    probabilities.put(zone, utility);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("Could not find destination for trip " + trip);
                failedTripsCounter.incrementAndGet();
                continue;
            }
            Zone destination = MitoUtil.select(probabilities);
            distributedTripsCounter.incrementAndGet();
            trip.setTripDestination(destination);
            usedBudget += dataSet.getTravelTimes("car").getTravelTimeFromTo(household.getHomeZone().getZoneId(), destination.getZoneId());
        }
        if (usedBudget > 0) {
            choiceUtilities.addBudgetForPurpose(HBO, usedBudget);
        }
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
            choiceUtilities.updateUtilitiesForOriginAndPurpose(baseZone.getZoneId(), NHBW, trips.size());
            for (Zone zone : dataSet.getZones().values()) {
                double utility = choiceUtilities.getUtilityMatrices().get(NHBW).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                if (utility > 0) {
                    probabilities.put(zone, utility);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("No zone could be assigned by random utility");
                failedTripsCounter.incrementAndGet();
                continue;
            }
            Zone secondZone = MitoUtil.select(probabilities);
            distributedTripsCounter.incrementAndGet();
            if (MitoUtil.getRandomFloat() > 0.5f) {
                trip.setTripDestination(baseZone);
                trip.setTripOrigin(secondZone);
                usedBudget += dataSet.getTravelTimes("car").getTravelTimeFromTo(baseZone.getZoneId(), secondZone.getZoneId());
            } else {
                trip.setTripOrigin(baseZone);
                trip.setTripDestination(secondZone);
                usedBudget += dataSet.getTravelTimes("car").getTravelTimeFromTo(secondZone.getZoneId(), baseZone.getZoneId());
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
            choiceUtilities.updateUtilitiesForOriginAndPurpose(baseZone.getZoneId(), NHBO, trips.size());
            for (Zone zone : dataSet.getZones().values()) {
                double utility = choiceUtilities.getUtilityMatrices().get(NHBO).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                if (utility > 0) {
                    probabilities.put(zone, utility);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("No zone could be assigned by random utility");
                failedTripsCounter.incrementAndGet();
                continue;
            }
            Zone secondZone = MitoUtil.select(probabilities);
            distributedTripsCounter.incrementAndGet();
            if (MitoUtil.getRandomFloat() > 0.5f) {
                trip.setTripDestination(baseZone);
                trip.setTripOrigin(secondZone);
                usedBudget += dataSet.getTravelTimes("car").getTravelTimeFromTo(secondZone.getZoneId(), baseZone.getZoneId());
            } else {
                trip.setTripOrigin(secondZone);
                trip.setTripDestination(baseZone);
                usedBudget += dataSet.getTravelTimes("car").getTravelTimeFromTo(baseZone.getZoneId(), secondZone.getZoneId());
            }
        }
        if (usedBudget > 0) {
            choiceUtilities.addBudgetForPurpose(NHBO, usedBudget);
        }
    }
}
