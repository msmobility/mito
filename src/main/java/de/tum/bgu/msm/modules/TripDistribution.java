package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.tum.bgu.msm.resources.Occupation.STUDENT;
import static de.tum.bgu.msm.resources.Occupation.WORKER;
import static de.tum.bgu.msm.resources.Purpose.*;


public class TripDistribution extends Module {

    private int distributedTripsCounter = 0;
    private int failedTripsCounter = 0;

    private final double ALPHA_HBW = 1;
    private final double BETA_HBW = 1;
    private final double GAMMA_HBW = -1;
    private final double DELTA_HBW = 1;

    private final double ALPHA_HBE = 1;
    private final double BETA_HBE = 1;
    private final double GAMMA_HBE = -1;
    private final double DELTA_HBE = 1;

    private final double ALPHA_SHOP = 1;
    private final double BETA_SHOP = 1;
    private final double GAMMA_SHOP = -1;
    private final double DELTA_SHOP = 1;

    private final double ALPHA_OTHER = 1;
    private final double BETA_OTHER = 1;
    private final double GAMMA_OTHER = -1;
    private final double DELTA_OTHER = 1;

    private final double ALPHA_NHBW = 1;
    private final double BETA_NHBW = 1;
    private final double GAMMA_NHBW = -1;
    private final double DELTA_NHBW = 1;


    private static final Logger logger = Logger.getLogger(TripDistribution.class);

    public TripDistribution(DataSet dataSet) {
        super(dataSet);
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

    private MitoTrip getTrip(int id) {
        MitoTrip trip = dataSet.getTrips().get(id);
        if (trip == null) {
            logger.warn("Household refers to trip " + id + " but id doesn't exist.");
        }
        return trip;
    }

    private void distributeHBW(MitoHousehold household) {
        for (MitoTrip trip : household.getTripsForPurpose(HBW)) {
            trip.setTripOrigin(household.getHomeZone());
            if (trip.getPerson().getOccupation() == WORKER) {
                trip.setTripDestination(trip.getPerson().getWorkzone());
                distributedTripsCounter++;
            } else {
                logger.debug(trip + " is not done by a worker. Selecting zone by total employment utility");
                Map<Zone, Double> probabilities = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    double travelTime = dataSet.getAutoTravelTimes().getTravelTimeFromTo(trip.getTripOrigin(), zone);
                    double travelImpedance = Math.exp(BETA_HBW * travelTime);
                    int employees = zone.getTotalEmpl();
                    double size = Math.log(employees);
                    double utility = ALPHA_HBW + GAMMA_HBW * travelImpedance + DELTA_HBW * size;
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
            if (trip.getPerson().getOccupation() == STUDENT) {
                trip.setTripDestination(trip.getPerson().getWorkzone());
                distributedTripsCounter++;
            } else {
                logger.debug(trip + " is not done by a student. Selecting zone by school enrollment utility");
                Map<Zone, Double> probabilities = new HashMap<>();
                for (Zone zone : dataSet.getZones().values()) {
                    double travelTime = dataSet.getAutoTravelTimes().getTravelTimeFromTo(trip.getTripOrigin(), zone);
                    double travelImpedance = Math.exp(BETA_HBE * travelTime);
                    int schoolEnrollment = zone.getSchoolEnrollment();
                    double size = Math.log(schoolEnrollment);
                    double utility = ALPHA_HBE + GAMMA_HBE * travelImpedance + DELTA_HBE * size;
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
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            trip.setTripOrigin(household.getHomeZone());

            for (Zone zone : dataSet.getZones().values()) {
                double travelTime = dataSet.getAutoTravelTimes().getTravelTimeFromTo(trip.getTripOrigin(), zone);
                if (travelTime > budgetPerTrip) {
                    continue;
                }
                double travelImpedance = Math.exp(BETA_SHOP * travelTime);
                double shopEmpls = zone.getRetailEmpl();
                double size = Math.log(shopEmpls);
                double utility = ALPHA_SHOP + GAMMA_SHOP * travelImpedance + DELTA_SHOP * size;
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
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            trip.setTripOrigin(household.getHomeZone());

            for (Zone zone : dataSet.getZones().values()) {
                double travelTime = dataSet.getAutoTravelTimes().getTravelTimeFromTo(trip.getTripOrigin(), zone);
                if (travelTime > budgetPerTrip) {
                    continue;
                }
                double travelImpedance = Math.exp(BETA_OTHER * travelTime);
                double otherEmpl = zone.getOtherEmpl();
                double numberOfHouseholds = zone.getNumberOfHouseholds();
                double size = Math.log(otherEmpl) + Math.log(numberOfHouseholds);
                double utility = ALPHA_OTHER + GAMMA_OTHER * travelImpedance + DELTA_OTHER * size;
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
            Zone workZone = null;

            for (MitoTrip hbwTrip : household.getTripsForPurpose(HBW)) {
                if (hbwTrip.getPerson().equals(trip.getPerson())) {
                    workZone = hbwTrip.getTripDestination();
                    break;
                }
            }

            if (workZone == null) {
                logger.warn("Could not find a previous home based trip destination for nhbw trip");
                failedTripsCounter++;
                continue;
            }
            for (Zone zone : dataSet.getZones().values()) {
                double travelTime = dataSet.getAutoTravelTimes().getTravelTimeFromTo(workZone, zone);
                if (travelTime > budgetPerTrip) {
                    continue;
                }
                double travelImpedance = Math.exp(BETA_NHBW * travelTime);
                double empls = zone.getTotalEmpl();
                double size = Math.log(empls);
                double utility = ALPHA_NHBW + GAMMA_NHBW * travelImpedance + DELTA_NHBW * size;
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
            Zone originOrDestination = MitoUtil.select(probabilities);
            distributedTripsCounter++;
            if (MitoUtil.getRandomFloat() > 0.5f) {
                trip.setTripDestination(workZone);
                trip.setTripOrigin(originOrDestination);
            } else {
                trip.setTripOrigin(workZone);
                trip.setTripDestination(originOrDestination);
            }
        }
    }

    private void distributeNHBO(MitoHousehold household) {
        List<MitoTrip> trips = household.getTripsForPurpose(NHBO);
        double budgetPerTrip = household.getTravelTimeBudgetForPurpose(NHBO) / trips.size();
        for (MitoTrip trip : trips) {
            Map<Zone, Double> probabilities = new HashMap<>();
            Zone otherZone = null;
            for (MitoTrip hboTrip : household.getTripsForPurpose(HBO)) {
                if (hboTrip.getPerson().equals(trip.getPerson())) {
                    otherZone = hboTrip.getTripDestination();
                    break;
                }
            }
            for (MitoTrip hbsTrip : household.getTripsForPurpose(HBS)) {
                    if (hbsTrip.getPerson().equals(trip.getPerson())) {
                        otherZone = hbsTrip.getTripDestination();
                        break;
                    }
            }
                for (MitoTrip hbeTrip: household.getTripsForPurpose(HBE)) {
                        if (hbeTrip.getPerson().equals(trip.getPerson())) {
                            otherZone = hbeTrip.getTripDestination();
                            break;
                        }
            }
            if (otherZone == null) {
                logger.warn("Could not find a previous home based trip destination for nhbo trip");
                failedTripsCounter++;
                continue;
            }
            for (Zone zone : dataSet.getZones().values()) {
                double travelTime = dataSet.getAutoTravelTimes().getTravelTimeFromTo(otherZone, zone);
                if (travelTime > budgetPerTrip) {
                    continue;
                }
                double travelImpedance = Math.exp(BETA_NHBW * travelTime);
                double empls = zone.getTotalEmpl();
                double size = Math.log(empls);
                double utility = ALPHA_NHBW + GAMMA_NHBW * travelImpedance + DELTA_NHBW * size;
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
            Zone originOrDestination = MitoUtil.select(probabilities);
            distributedTripsCounter++;
            if (MitoUtil.getRandomFloat() > 0.5f) {
                trip.setTripDestination(otherZone);
                trip.setTripOrigin(originOrDestination);
            } else {
                trip.setTripOrigin(otherZone);
                trip.setTripDestination(originOrDestination);
            }
        }
    }
}
