package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
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

    private static final Logger logger = Logger.getLogger(TripDistribution.class);

    private TripDistributionJSCalculator tripDistributionCalc;

    public TripDistribution(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        setupModel();
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

    private void setupModel() {
        logger.info("  Creating Utility Expression Calculators for microscopic trip distribution.");
        try {
            Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TripDistribution"));
            tripDistributionCalc = new TripDistributionJSCalculator(reader, dataSet.getAutoTravelTimes());
        } catch (ScriptException e) {
            logger.fatal("Error in input script!", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.fatal("Travel time budget script not found (property: \"ttb.js\")!", e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            logger.fatal("Travel time budget script not found (property: \"ttb.js\")!", e);
            e.printStackTrace();
        }
    }

    private void distributeHBW(MitoHousehold household) {
        tripDistributionCalc.setPurposeAndBudget(HBW, 0);
        for (MitoTrip trip : household.getTripsForPurpose(HBW)) {
            trip.setTripOrigin(household.getHomeZone());
            if (trip.getPerson().getOccupation() == WORKER && trip.getPerson().getWorkzone() != null) {
                trip.setTripDestination(trip.getPerson().getWorkzone());
                distributedTripsCounter++;
            } else {
                logger.debug(trip + " is not done by a worker (or invalid workzone). Selecting zone by total employment utility");
                tripDistributionCalc.setBaseZone(household.getHomeZone());
                Map<Zone, Double> probabilities = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    tripDistributionCalc.setTargetZone(zone);
                    double utility = tripDistributionCalc.calculate(true);
                    double probability = Math.exp(utility);
                    if (probability > 0) {
                        probabilities.put(zone, probability);
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
        tripDistributionCalc.setPurposeAndBudget(HBE, 0);
        for (MitoTrip trip : household.getTripsForPurpose(HBE)) {
            trip.setTripOrigin(household.getHomeZone());
            if (trip.getPerson().getOccupation() == STUDENT && trip.getPerson().getWorkzone() != null) {
                trip.setTripDestination(trip.getPerson().getWorkzone());
                distributedTripsCounter++;
            } else {
                logger.debug(trip + " is not done by a student (or invalid workzone). Selecting zone by school enrollment utility");
                tripDistributionCalc.setBaseZone(household.getHomeZone());
                Map<Zone, Double> probabilities = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    tripDistributionCalc.setTargetZone(zone);
                    double utility = tripDistributionCalc.calculate(false);
                    double probability = Math.exp(utility);
                    if (probability > 0) {
                        probabilities.put(zone, probability);
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
        List<MitoTrip> trips = household.getTripsForPurpose(HBS);
        double budgetPerTrip = household.getTravelTimeBudgetForPurpose(HBS) / trips.size();
        tripDistributionCalc.setBaseZone(household.getHomeZone());
        tripDistributionCalc.setPurposeAndBudget(HBS, budgetPerTrip);
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            trip.setTripOrigin(household.getHomeZone());
            for (Zone zone : dataSet.getZones().values()) {
                tripDistributionCalc.setTargetZone(zone);
                double utility = tripDistributionCalc.calculate(true);
                double probability = Math.exp(utility);
                if (probability > 0) {
                    probabilities.put(zone, probability);
                }
            }
            if (probabilities.isEmpty()) {
                logger.warn("Could not find destination for trip " + trip);
                failedTripsCounter++;
                continue;
            }
            Zone destination = MitoUtil.select(probabilities);
            trip.setTripDestination(destination);
            distributedTripsCounter++;
        }
    }

    private void distributeHBO(MitoHousehold household) {
        List<MitoTrip> trips = household.getTripsForPurpose(HBO);
        double budgetPerTrip = household.getTravelTimeBudgetForPurpose(HBO) / trips.size();
        tripDistributionCalc.setBaseZone(household.getHomeZone());
        tripDistributionCalc.setPurposeAndBudget(HBO, budgetPerTrip);
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            trip.setTripOrigin(household.getHomeZone());
            for (Zone zone : dataSet.getZones().values()) {
                tripDistributionCalc.setTargetZone(zone);
                double utility = tripDistributionCalc.calculate(true);
                double probability = Math.exp(utility);
                if (probability > 0) {
                    probabilities.put(zone, probability);
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

    private void distributeNHBW(MitoHousehold household) {
        List<MitoTrip> trips = household.getTripsForPurpose(NHBW);
        double budgetPerTrip = household.getTravelTimeBudgetForPurpose(NHBW) / trips.size();
        tripDistributionCalc.setPurposeAndBudget(NHBW, budgetPerTrip);

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
                tripDistributionCalc.setBaseZone(household.getHomeZone());
                for (Zone zone : dataSet.getZones().values()) {
                    tripDistributionCalc.setTargetZone(zone);
                    double utility = tripDistributionCalc.calculate(false);
                    double probability = Math.exp(utility);
                    if (probability > 0) {
                        probabilitiesAlt.put(zone, probability);
                    }
                }
                baseZone = MitoUtil.select(probabilitiesAlt);
            }
            tripDistributionCalc.setBaseZone(baseZone);
            for (Zone zone : dataSet.getZones().values()) {
                tripDistributionCalc.setTargetZone(zone);
                double utility = tripDistributionCalc.calculate(false);
                double probability = Math.exp(utility);
                if (probability > 0) {
                    probabilities.put(zone, probability);
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
            } else {
                trip.setTripOrigin(baseZone);
                trip.setTripDestination(secondZone);
            }
        }
    }

    private void distributeNHBO(MitoHousehold household) {
        List<MitoTrip> trips = household.getTripsForPurpose(NHBO);
        double budgetPerTrip = household.getTravelTimeBudgetForPurpose(NHBO) / trips.size();
        tripDistributionCalc.setPurposeAndBudget(NHBO, budgetPerTrip);
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

            if(!possibleBaseZones.isEmpty()) {
                baseZone = possibleBaseZones.get((int) MitoUtil.getRandomFloat() * (possibleBaseZones.size() - 1));
            }

            if (baseZone == null) {
                logger.warn("Could not find a previous home based trip destination for nhbo trip. Picking by random utility.");
                Map<Zone, Double> probabilitiesAlt = new HashMap<>();
                tripDistributionCalc.setBaseZone(household.getHomeZone());
                for (Zone zone : dataSet.getZones().values()) {
                    tripDistributionCalc.setTargetZone(zone);
                    double utility = tripDistributionCalc.calculate(false);
                    double probability = Math.exp(utility);
                    if (probability > 0) {
                        probabilitiesAlt.put(zone, probability);
                    }
                }
                baseZone = MitoUtil.select(probabilitiesAlt);
            }
            tripDistributionCalc.setBaseZone(baseZone);
            for (Zone zone : dataSet.getZones().values()) {
                tripDistributionCalc.setTargetZone(zone);
                double utility = tripDistributionCalc.calculate(false);
                double probability = Math.exp(utility);
                if (probability > 0) {
                    probabilities.put(zone, probability);
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
            } else {
                trip.setTripOrigin(secondZone);
                trip.setTripDestination(baseZone);
            }
        }
    }
}
