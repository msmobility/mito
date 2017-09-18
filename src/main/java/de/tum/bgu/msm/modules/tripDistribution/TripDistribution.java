package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.tum.bgu.msm.resources.Occupation.STUDENT;
import static de.tum.bgu.msm.resources.Occupation.WORKER;
import static de.tum.bgu.msm.resources.Purpose.*;


public class TripDistribution extends Module {

    private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    private int distributedTripsCounter = 0;
    private int failedTripsCounter = 0;

    private static final Logger logger = Logger.getLogger(TripDistribution.class);
    private TripDistributionCalculator hbwCalculator;
    private TripDistributionCalculator hbeCalculator;
    private TripDistributionCalculator hbsCalculator;
    private TripDistributionCalculator hboCalculator;
    private TripDistributionCalculator nhbwCalculator;
    private TripDistributionCalculator nhboCalculator;
    private TripDistributionCalculator nhbwAltCalculator;
    private TripDistributionCalculator nhboAltCalculator;

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
        int hbwSheetNumber = Resources.INSTANCE.getInt(Properties.HBW_TRIP_DISTRIBUTION_UEC_SHEET);
        hbwCalculator = new TripDistributionCalculator(hbwSheetNumber, dataSet.getAutoTravelTimes());

        int hbeSheetNumber = Resources.INSTANCE.getInt(Properties.HBE_TRIP_DISTRIBUTION_UEC_SHEET);
        hbeCalculator = new TripDistributionCalculator(hbeSheetNumber, dataSet.getAutoTravelTimes());

        int hbsSheetNumber = Resources.INSTANCE.getInt(Properties.HBS_TRIP_DISTRIBUTION_UEC_SHEET);
        hbsCalculator = new TripDistributionCalculator(hbsSheetNumber, dataSet.getAutoTravelTimes());

        int hboSheetNumber = Resources.INSTANCE.getInt(Properties.HBO_TRIP_DISTRIBUTION_UEC_SHEET);
        hboCalculator = new TripDistributionCalculator(hboSheetNumber, dataSet.getAutoTravelTimes());

        int nhbwSheetNumber = Resources.INSTANCE.getInt(Properties.NHBW_TRIP_DISTRIBUTION_UEC_SHEET);
        nhbwCalculator = new TripDistributionCalculator(nhbwSheetNumber, dataSet.getAutoTravelTimes());

        int nhboSheetNumber = Resources.INSTANCE.getInt(Properties.NHBO_TRIP_DISTRIBUTION_UEC_SHEET);
        nhboCalculator = new TripDistributionCalculator(nhboSheetNumber, dataSet.getAutoTravelTimes());

        int nhbwAltSheetNumber = Resources.INSTANCE.getInt(Properties.NHBW_ALT_TRIP_DISTRIBUTION_UEC_SHEET);
        nhbwAltCalculator = new TripDistributionCalculator(nhbwAltSheetNumber, dataSet.getAutoTravelTimes());

        int nhboAltSheetNumber = Resources.INSTANCE.getInt(Properties.NHBO_ALT_TRIP_DISTRIBUTION_UEC_SHEET);
        nhboAltCalculator = new TripDistributionCalculator(nhboAltSheetNumber, dataSet.getAutoTravelTimes());
    }

    private void distributeHBW(MitoHousehold household) {
        for (MitoTrip trip : household.getTripsForPurpose(HBW)) {
            trip.setTripOrigin(household.getHomeZone());
            hbwCalculator.setOriginZone(household.getHomeZone());
            if (trip.getPerson().getOccupation() == WORKER && trip.getPerson().getWorkzone() != null) {
                trip.setTripDestination(trip.getPerson().getWorkzone());
                distributedTripsCounter++;
            } else {
                logger.debug(trip + " is not done by a worker (or invalid workzone). Selecting zone by total employment utility");
                Map<Zone, Double> probabilities = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {




                    double utility = hbwCalculator.calculate(false, zone);
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
        for (MitoTrip trip : household.getTripsForPurpose(HBE)) {
            trip.setTripOrigin(household.getHomeZone());
            hbeCalculator.setOriginZone(household.getHomeZone());
            if (trip.getPerson().getOccupation() == STUDENT && trip.getPerson().getWorkzone() != null) {
                trip.setTripDestination(trip.getPerson().getWorkzone());
                distributedTripsCounter++;
            } else {
                logger.debug(trip + " is not done by a student (or invalid workzone). Selecting zone by school enrollment utility");
                Map<Zone, Double> probabilities = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    double utility = hbeCalculator.calculate(false, zone);
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
        hbsCalculator.setOriginZone(household.getHomeZone());
        hbsCalculator.setBudget(budgetPerTrip);
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            trip.setTripOrigin(household.getHomeZone());
            for (Zone zone : dataSet.getZones().values()) {
                double utility = hbsCalculator.calculate(false, zone);
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
        hboCalculator.setOriginZone(household.getHomeZone());
        hboCalculator.setBudget(budgetPerTrip);
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            trip.setTripOrigin(household.getHomeZone());
            for (Zone zone : dataSet.getZones().values()) {
                double utility = hboCalculator.calculate(false, zone);
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
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            Zone firstZone = null;

            for (MitoTrip hbwTrip : household.getTripsForPurpose(HBW)) {
                if (hbwTrip.getPerson().equals(trip.getPerson())) {
                    firstZone = hbwTrip.getTripDestination();
                    break;
                }
            }

            if (firstZone == null) {
                logger.warn("Could not find a previous home based trip destination for nhbw trip. Picking by random utility.");
                Map<Zone, Double> probabilitiesAlt = new HashMap<>();
                nhbwAltCalculator.setOriginZone(household.getHomeZone());
                nhbwAltCalculator.setBudget(0);
                for (Zone zone : dataSet.getZones().values()) {
                    double utility = nhbwAltCalculator.calculate(false, zone);
                    double probability = Math.exp(utility);
                    if (probability > 0) {
                        probabilitiesAlt.put(zone, probability);
                    }
                }
                firstZone = MitoUtil.select(probabilitiesAlt);
            }
            nhbwCalculator.setOriginZone(firstZone);
            nhboCalculator.setBudget(budgetPerTrip);
            for (Zone zone : dataSet.getZones().values()) {
                double utility = nhbwCalculator.calculate(false, zone);
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
                trip.setTripDestination(firstZone);
                trip.setTripOrigin(secondZone);
            } else {
                trip.setTripOrigin(firstZone);
                trip.setTripDestination(secondZone);
            }
        }
    }

    private void distributeNHBO(MitoHousehold household) {
        List<MitoTrip> trips = household.getTripsForPurpose(NHBO);
        double budgetPerTrip = household.getTravelTimeBudgetForPurpose(NHBO) / trips.size();
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            Zone firstZone = null;
            for (MitoTrip hboTrip : household.getTripsForPurpose(HBO)) {
                if (hboTrip.getPerson().equals(trip.getPerson())) {
                    firstZone = hboTrip.getTripDestination();
                    break;
                }
            }
            for (MitoTrip hbsTrip : household.getTripsForPurpose(HBS)) {
                    if (hbsTrip.getPerson().equals(trip.getPerson())) {
                        firstZone = hbsTrip.getTripDestination();
                        break;
                    }
            }
                for (MitoTrip hbeTrip: household.getTripsForPurpose(HBE)) {
                        if (hbeTrip.getPerson().equals(trip.getPerson())) {
                            firstZone = hbeTrip.getTripDestination();
                            break;
                        }
            }
            if (firstZone == null) {
                logger.warn("Could not find a previous home based trip destination for nhbo trip. Picking by random utility.");
                Map<Zone, Double> probabilitiesAlt = new HashMap<>();
                nhboAltCalculator.setOriginZone(household.getHomeZone());
                nhboAltCalculator.setBudget(0);
                for (Zone zone : dataSet.getZones().values()) {
                    double utility = nhboAltCalculator.calculate(false, zone);
                    double probability = Math.exp(utility);
                    if (probability > 0) {
                        probabilitiesAlt.put(zone, probability);
                    }
                }
                firstZone = MitoUtil.select(probabilitiesAlt);
            }
            nhboCalculator.setOriginZone(firstZone);
            nhboCalculator.setBudget(budgetPerTrip);
            for (Zone zone : dataSet.getZones().values()) {
                double utility = nhboCalculator.calculate(false, zone);
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
                trip.setTripDestination(firstZone);
                trip.setTripOrigin(secondZone);
            } else {
                trip.setTripOrigin(secondZone);
                trip.setTripDestination(firstZone);
            }
        }
    }
}
