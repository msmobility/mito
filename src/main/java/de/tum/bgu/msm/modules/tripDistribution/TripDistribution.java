package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.tum.bgu.msm.resources.Occupation.STUDENT;
import static de.tum.bgu.msm.resources.Occupation.WORKER;
import static de.tum.bgu.msm.resources.Purpose.*;


public class TripDistribution extends Module {

    private int distributedTripsCounter = 0;
    private int failedTripsCounter = 0;

    private final ChoiceUtilities choiceUtilities;

    private final static Logger logger = Logger.getLogger(TripDistribution.class);

    public TripDistribution(DataSet dataSet) {
        super(dataSet);
        choiceUtilities = new ChoiceUtilities(dataSet);
    }

    @Override
    public void run() {
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            distributeHBW(household);
            distributeHBE(household);
            distributeHBS(household);
            distributeHBO(household);
            distributeNHBW(household);
            distributeNHBO(household);
        }
        System.out.println("Distributed: " + distributedTripsCounter + ", failed: " + failedTripsCounter);
    }


    private void distributeHBW(MitoHousehold household) {
        for (MitoTrip trip : household.getTripsForPurpose(HBW)) {
            trip.setTripOrigin(household.getHomeZone());
            if (trip.getPerson().getOccupation() == WORKER && trip.getPerson().getWorkzone() != null) {
                trip.setTripDestination(trip.getPerson().getWorkzone());
                distributedTripsCounter++;
            } else {
                logger.debug(trip + " is not done by a worker (or invalid workzone). Selecting zone by total employment utility");
                Map<Zone, Double> probabilities = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    double utility = choiceUtilities.getUtilityforRelationAndPurpose(household.getHomeZone().getZoneId(), zone.getZoneId(), HBW);
                    if (utility > 0) {
                        probabilities.put(zone, utility);
                    }
                }
                if (probabilities.isEmpty()) {
                    logger.warn("Could not find destination for trip " + trip);
                    failedTripsCounter++;
                    continue;
                }
                Zone destination = MitoUtil.select(probabilities);
                distributedTripsCounter++;
                trip.setTripDestination(destination);
            }
        }
    }

    private void distributeHBE(MitoHousehold household) {
        for (MitoTrip trip : household.getTripsForPurpose(HBE)) {
            trip.setTripOrigin(household.getHomeZone());
            if (trip.getPerson().getOccupation() == STUDENT && trip.getPerson().getWorkzone() != null) {
                trip.setTripDestination(trip.getPerson().getWorkzone());
                distributedTripsCounter++;
            } else {
                logger.debug(trip + " is not done by a student (or invalid workzone). Selecting zone by school enrollment utility");
                Map<Zone, Double> probabilities = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    double utility = choiceUtilities.getUtilityforRelationAndPurpose(household.getHomeZone().getZoneId(), zone.getZoneId(), HBE);
                    if (utility > 0) {
                        probabilities.put(zone, utility);
                    }
                }
                if (probabilities.isEmpty()) {
                    logger.warn("Could not find destination for trip " + trip);
                    failedTripsCounter++;
                    continue;
                }
                Zone destination = MitoUtil.select(probabilities);
                distributedTripsCounter++;
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
                double utility = choiceUtilities.getUtilityforRelationAndPurpose(household.getHomeZone().getZoneId(), zone.getZoneId(), HBS);
                if (utility > 0) {
                    probabilities.put(zone, utility);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("Could not find destination for trip " + trip);
                failedTripsCounter++;
                continue;
            }
            Zone destination = MitoUtil.select(probabilities);
            trip.setTripDestination(destination);
            choiceUtilities.addSelectedRelationForPurpose(household.getHomeZone(), destination, HBS);
            distributedTripsCounter++;
        }
    }

    private void distributeHBO(MitoHousehold household) {
        choiceUtilities.updateUtilitiesForOriginAndPurpose(household.getHomeZone().getZoneId(), HBO);
        List<MitoTrip> trips = household.getTripsForPurpose(HBO);
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            trip.setTripOrigin(household.getHomeZone());
            for (Zone zone : dataSet.getZones().values()) {
                double utility = choiceUtilities.getUtilityforRelationAndPurpose(household.getHomeZone().getZoneId(), zone.getZoneId(), HBO);
                if (utility > 0) {
                    probabilities.put(zone, utility);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("Could not find destination for trip " + trip);
                failedTripsCounter++;
                continue;
            }
            Zone destination = MitoUtil.select(probabilities);
            choiceUtilities.addSelectedRelationForPurpose(household.getHomeZone(), destination, HBO);
            distributedTripsCounter++;
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
                    double utility = choiceUtilities.getUtilityforRelationAndPurpose(household.getHomeZone().getZoneId(), zone.getZoneId(), HBW);
                    if (utility > 0) {
                        probabilitiesAlt.put(zone, utility);
                    }
                }
                baseZone = MitoUtil.select(probabilitiesAlt);
            }
            choiceUtilities.updateUtilitiesForOriginAndPurpose(baseZone.getZoneId(), NHBW);
            for (Zone zone : dataSet.getZones().values()) {
                double utility = choiceUtilities.getUtilityforRelationAndPurpose(household.getHomeZone().getZoneId(), zone.getZoneId(), NHBW);
                if (utility > 0) {
                    probabilities.put(zone, utility);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("No zone could be assigned by random utility");
                failedTripsCounter++;
                continue;
            }
            Zone secondZone = MitoUtil.select(probabilities);
            distributedTripsCounter++;
            if (MitoUtil.getRandomFloat() > 0.5f) {
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
                baseZone = possibleBaseZones.get((int) (MitoUtil.getRandomFloat() * (possibleBaseZones.size() - 1)));
            }

            if (baseZone == null) {
                logger.warn("Could not find a previous home based trip destination for nhbo trip. Picking by random utility.");

                Purpose[] possibleHomeBasedPurposes = {HBE, HBO, HBS};
                Purpose selectedPurpose = possibleHomeBasedPurposes[MitoUtil.select(new double[]{1.,1.,1.})];

                Map<Zone, Double> probabilitiesAlt = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    double utility = choiceUtilities.getUtilityforRelationAndPurpose(household.getHomeZone().getZoneId(), zone.getZoneId(), selectedPurpose);
                    double probability = Math.exp(utility);
                    if (probability > 0) {
                        probabilitiesAlt.put(zone, probability);
                    }
                }
                baseZone = MitoUtil.select(probabilitiesAlt);
            }
            choiceUtilities.updateUtilitiesForOriginAndPurpose(baseZone.getZoneId(), NHBO);
            for (Zone zone : dataSet.getZones().values()) {
                double utility = choiceUtilities.getUtilityforRelationAndPurpose(baseZone.getZoneId(), zone.getZoneId(), NHBO);
                if (utility > 0) {
                    probabilities.put(zone, utility);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("No zone could be assigned by random utility");
                failedTripsCounter++;
                continue;
            }
            Zone secondZone = MitoUtil.select(probabilities);
            distributedTripsCounter++;
            if (MitoUtil.getRandomFloat() > 0.5f) {
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
