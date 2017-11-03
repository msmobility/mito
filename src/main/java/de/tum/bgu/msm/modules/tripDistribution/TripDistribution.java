package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunctionExecutor;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
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

    private final ChoiceUtilities choiceUtilities;

    private final static Logger logger = Logger.getLogger(TripDistribution.class);

    public TripDistribution(DataSet dataSet) {
        super(dataSet);
        choiceUtilities = new ChoiceUtilities(dataSet);
    }

    @Override
    public void run() {
        ConcurrentFunctionExecutor executor = new ConcurrentFunctionExecutor();
        for(MitoHousehold household: dataSet.getHouseholds().values()) {
            executor.addFunction(new HouseholdTripsHandler(household));
        }
        executor.execute();
        logger.info("Distributed: " + distributedTripsCounter + ", failed: " + failedTripsCounter);
    }

    private class HouseholdTripsHandler extends RandomizableConcurrentFunction {
        private final MitoHousehold household;

        private HouseholdTripsHandler(MitoHousehold household) {
            this.household = household;
        }

        @Override
        public void execute() {
            distributeHBW(household);
            distributeHBE(household);
            distributeHBS(household);
            distributeHBO(household);
            distributeNHBW(household);
            distributeNHBO(household);
        }

        private void distributeHBW(MitoHousehold household) {
            for (MitoTrip trip : household.getTripsForPurpose(HBW)) {
                trip.setTripOrigin(household.getHomeZone());
                if (trip.getPerson().getOccupation() == WORKER && trip.getPerson().getWorkzone() != null) {
                    trip.setTripDestination(trip.getPerson().getWorkzone());
                    distributedTripsCounter.incrementAndGet();
                } else {
                    logger.debug(trip + " is not done by a worker (or invalid workzone). Selecting zone by total employment utility");
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
                    Zone destination = MitoUtil.select(probabilities, random);
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
                    logger.debug(trip + " is not done by a student (or invalid workzone). Selecting zone by school enrollment utility");
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
                    Zone destination = MitoUtil.select(probabilities, random);
                    distributedTripsCounter.incrementAndGet();
                    trip.setTripDestination(destination);
                }
            }
        }

        private void distributeHBS(MitoHousehold household) {
            choiceUtilities.updateUtilitiesForOriginAndPurpose(household.getHomeZone().getZoneId(), HBS);
            List<MitoTrip> trips = household.getTripsForPurpose(HBS);
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
                Zone destination = MitoUtil.select(probabilities, random);
                trip.setTripDestination(destination);
                choiceUtilities.addSelectedRelationForPurpose(household.getHomeZone(), destination, HBS);
                distributedTripsCounter.incrementAndGet();
            }
        }

        private void distributeHBO(MitoHousehold household) {
            choiceUtilities.updateUtilitiesForOriginAndPurpose(household.getHomeZone().getZoneId(), HBO);
            List<MitoTrip> trips = household.getTripsForPurpose(HBO);
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
                Zone destination = MitoUtil.select(probabilities, random);
                choiceUtilities.addSelectedRelationForPurpose(household.getHomeZone(), destination, HBO);
                distributedTripsCounter.incrementAndGet();
                trip.setTripDestination(destination);
            }
        }

        private void distributeNHBW(MitoHousehold household) {
            List<MitoTrip> trips = household.getTripsForPurpose(NHBW);
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
                    logger.warn("Could not find a previous home based work trip destination for nhbw trip. Picking by random utility.");
                    Map<Zone, Double> probabilitiesAlt = new HashMap<>();
                    for (Zone zone : dataSet.getZones().values()) {
                        double utility = choiceUtilities.getUtilityMatrices().get(HBW).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                        if (utility > 0) {
                            probabilitiesAlt.put(zone, utility);
                        }
                    }
                    baseZone = MitoUtil.select(probabilitiesAlt, random);
                }
                choiceUtilities.updateUtilitiesForOriginAndPurpose(baseZone.getZoneId(), NHBW);
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
                Zone secondZone = MitoUtil.select(probabilities, random);
                distributedTripsCounter.incrementAndGet();
                if (random.nextFloat() > 0.5f) {
                    trip.setTripDestination(baseZone);
                    trip.setTripOrigin(secondZone);
                    choiceUtilities.addSelectedRelationForPurpose(secondZone, baseZone, NHBW);
                } else {
                    trip.setTripOrigin(baseZone);
                    trip.setTripDestination(secondZone);
                    choiceUtilities.addSelectedRelationForPurpose(baseZone, secondZone, NHBW);
                }
            }
        }

        private void distributeNHBO(MitoHousehold household) {
            List<MitoTrip> trips = household.getTripsForPurpose(NHBO);
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
                    baseZone = possibleBaseZones.get((int) (random.nextFloat() * (possibleBaseZones.size() - 1)));
                }

                if (baseZone == null) {
                    logger.warn("Could not find a previous home based trip destination for nhbo trip. Picking by random utility.");

                    Purpose[] possibleHomeBasedPurposes = {HBE, HBO, HBS};
                    Purpose selectedPurpose = possibleHomeBasedPurposes[MitoUtil.select(new double[]{1., 1., 1.}, random)];

                    Map<Zone, Double> probabilitiesAlt = new HashMap<>();
                    for (Zone zone : dataSet.getZones().values()) {
                        double utility = choiceUtilities.getUtilityMatrices().get(selectedPurpose).get(household.getHomeZone().getZoneId(), zone.getZoneId());
                        probabilitiesAlt.put(zone, utility);

                    }
                    baseZone = MitoUtil.select(probabilitiesAlt, random);
                }
                choiceUtilities.updateUtilitiesForOriginAndPurpose(baseZone.getZoneId(), NHBO);
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
                Zone secondZone = MitoUtil.select(probabilities, random);
                distributedTripsCounter.incrementAndGet();
                if (random.nextFloat() > 0.5f) {
                    trip.setTripDestination(baseZone);
                    trip.setTripOrigin(secondZone);
                    choiceUtilities.addSelectedRelationForPurpose(secondZone, baseZone, NHBO);
                } else {
                    trip.setTripOrigin(secondZone);
                    trip.setTripDestination(baseZone);
                    choiceUtilities.addSelectedRelationForPurpose(baseZone, secondZone, NHBO);
                }
            }
        }
    }
}
