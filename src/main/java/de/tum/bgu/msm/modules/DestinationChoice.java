package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs destination choice for each trip for the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel, Ana Moreno, Nico Kuehnel
 * Created on June 8, 2017 in Munich, Germany
 */
public class DestinationChoice extends Module {

    private static final Logger logger = Logger.getLogger(DestinationChoice.class);

    private final double ALPHA_SHOP = 1;
    private final double BETA_SHOP = 1;
    private final double GAMMA_SHOP = -1;
    private final double DELTA_SHOP = 1;

    private final double ALPHA_OTHER = 1;
    private final double BETA_OTHER = 1;
    private final double GAMMA_OTHER = -1;
    private final double DELTA_OTHER = 1;

    public DestinationChoice(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        selectTripDestinations();
    }


    private void selectTripDestinations() {
        logger.info("  Started Destination Choice");
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            selectDestinationsForHouseholdTrips(household);
        }
        logger.info("  Finished Destination Choice");
    }

    private void selectDestinationsForHouseholdTrips(MitoHousehold household) {
        if (household.getTripsByPurpose().containsKey(dataSet.getPurposeIndex("HBW"))) {
            processHBW(household);
        }
        if (household.getTripsByPurpose().containsKey(dataSet.getPurposeIndex("HBE"))) {
            processHBE(household);
        }
        if (household.getTripsByPurpose().containsKey(dataSet.getPurposeIndex("HBS"))) {
            processHBS(household);
        }
        if (household.getTripsByPurpose().containsKey(dataSet.getPurposeIndex("HBO"))) {
            processHBO(household);
        }
    }


    private void processHBW(MitoHousehold household) {
        for (MitoTrip trip : household.getTripsByPurpose().get(dataSet.getPurposeIndex("HBW"))) {
            if (trip.getPerson() == null) {
                logger.warn("No person specified for HBW trip. Could not define trip destination");
                continue;
            }
            trip.setTripDestination(trip.getPerson().getWorkzone());
        }
    }

    private void processHBE(MitoHousehold household) {
        for (MitoTrip trip : household.getTripsByPurpose().get(dataSet.getPurposeIndex("HBE"))) {
            if (trip.getPerson() == null) {
                logger.warn("No person specified for HBE trip. Could not define trip destination");
                continue;
            }
            trip.setTripDestination(trip.getPerson().getWorkzone());
        }
    }

    private void processHBS(MitoHousehold household) {
        int numberOfHBSTrips = household.getTripsByPurpose().get(dataSet.getPurposeIndex("HBS")).size();
        for (MitoTrip trip : household.getTripsByPurpose().get(dataSet.getPurposeIndex("HBS"))) {
            double individualBudget = household.getTravelTimeBudgetForPurpose("HBS") / numberOfHBSTrips;
            Map<Integer, Double> probabilities = getProbabilities(trip, individualBudget, "HBS");
            selectDestinationByProbabilities(trip, probabilities);
        }
    }

    private void processHBO(MitoHousehold household) {
        int numberOfHBOTrips = household.getTripsByPurpose().get(dataSet.getPurposeIndex("HBO")).size();
        for (MitoTrip trip : household.getTripsByPurpose().get(dataSet.getPurposeIndex("HBO"))) {
            double individualBudget = household.getTravelTimeBudgetForPurpose("HBO") / numberOfHBOTrips;
            Map<Integer, Double> probabilities = getProbabilities(trip, individualBudget, "HBO");
            selectDestinationByProbabilities(trip, probabilities);
        }
    }


    private Map<Integer, Double> getProbabilities(MitoTrip trip, double budget, String purpose) {

        Map<Integer, Double> probabilities = new HashMap<>();
        double probabilitySum = 0;
        for (Zone zone : dataSet.getZones().values()) {
            if (exceedsTravelTimeBudget(trip.getTripOrigin(), zone.getZoneId(), budget)) {
                continue;
            }
            double utility = calculateUtility(trip.getTripOrigin(), zone, purpose);
            double probability = Math.exp(utility);
            probabilitySum += probability;
            probabilities.put(zone.getZoneId(), probability);
        }

        if (probabilitySum > 0) {
            MitoUtil.scaleMapBy(probabilities, 1 / probabilitySum);
        }
        return probabilities;
    }

    private void selectDestinationByProbabilities(MitoTrip trip, Map<Integer, Double> probabilities) {
        if (probabilities.isEmpty()) {
            trip.setTripDestination(trip.getTripOrigin());
            return;
        }
        trip.setTripDestination(MitoUtil.select(probabilities, 1));
    }

    private boolean exceedsTravelTimeBudget(int tripOrigin, int tripDestination, double budget) {
        if (dataSet.getAutoTravelTimes().getTravelTimeFromTo(tripOrigin, tripDestination) > budget) {
            return true;
        } else {
            return false;
        }
    }

    private double calculateUtility(int tripOrigin, Zone zone, String purpose) {

        double travelTime = dataSet.getAutoTravelTimes().getTravelTimeFromTo(tripOrigin, zone.getZoneId());

        if (purpose.equals("HBS")) {
            double travelImpedance = Math.exp(BETA_SHOP * travelTime);
            double shopEmpls = zone.getRetailEmpl();
            double size = Math.log(shopEmpls);
            return ALPHA_SHOP + GAMMA_SHOP * travelImpedance + DELTA_SHOP * size;
        } else if (purpose.equals("HBO")) {
            double travelImpedance = Math.exp(BETA_OTHER * travelTime);
            double otherEmpl = zone.getOtherEmpl();
            double numberOfHouseholds = zone.getNumberOfHouseholds();
            double size = Math.log(otherEmpl) + Math.log(numberOfHouseholds);
            return ALPHA_OTHER + GAMMA_OTHER * travelImpedance + DELTA_OTHER * size;
        }
        return 0;
    }
}
