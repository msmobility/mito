//package de.tum.bgu.msm.modules.tripDistribution;
//
//import de.tum.bgu.msm.MitoUtil;
//import de.tum.bgu.msm.data.DataSet;
//import de.tum.bgu.msm.data.MitoHousehold;
//import de.tum.bgu.msm.data.MitoTrip;
//import de.tum.bgu.msm.data.Zone;
//import de.tum.bgu.msm.resources.Purpose;
//import org.apache.log4j.Logger;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Runs destination choice for each trip for the Microsimulation Transport Orchestrator (MITO)
// *
// * @author Rolf Moeckel, Ana Moreno, Nico Kuehnel
// * Created on June 8, 2017 in Munich, Germany
// */
//public class DestinationChoice {
//
//    private static final Logger logger = Logger.getLogger(DestinationChoice.class);
//
//
//
//    private final DataSet dataSet;
//
//    public DestinationChoice(DataSet dataSet) {
//        this.dataSet = dataSet;
//    }
//
//    public void run() {
//        logger.info("  Started Destination Choice");
//        for (MitoHousehold household : dataSet.getHouseholds().values()) {
//            selectDestinationsForHouseholdTrips(household);
//        }
//        logger.info("  Finished Destination Choice");
//    }
//
//    private void selectDestinationsForHouseholdTrips(MitoHousehold household) {
//        if (household.getTripsByPurpose().containsKey(Purpose.HBW)) {
//            processHBW(household);
//        }
//        if (household.getTripsByPurpose().containsKey(Purpose.HBE)) {
//            processHBE(household);
//        }
//        if (household.getTripsByPurpose().containsKey(Purpose.HBS)) {
//            processHBS(household);
//        }
//        if (household.getTripsByPurpose().containsKey(Purpose.HBO)) {
//            processHBO(household);
//        }
//    }
//
//    private void processHBW(MitoHousehold household) {
//        for (MitoTrip trip : household.getTripsByPurpose().get(Purpose.HBW)) {
//            if (trip.getPerson() == null) {
//                logger.warn("No person specified for HBW trip. Could not define trip destination");
//                continue;
//            }
//            trip.setTripDestination(trip.getPerson().getWorkzone());
//        }
//    }
//
//    private void processHBE(MitoHousehold household) {
//        for (MitoTrip trip : household.getTripsByPurpose().get(Purpose.HBE)) {
//            if (trip.getPerson() == null) {
//                logger.warn("No person specified for HBE trip. Could not define trip destination");
//                continue;
//            }
//            trip.setTripDestination(trip.getPerson().getWorkzone());
//        }
//    }
//
//    private void processHBS(MitoHousehold household) {
//        List<MitoTrip> trips = household.getTripsByPurpose().get(Purpose.HBS);
//        for (MitoTrip trip : trips) {
//            double individualBudget = household.getTravelTimeBudgetForPurpose(Purpose.HBS) / trips.size();
//            Map<Integer, Double> probabilities = getProbabilities(trip, individualBudget, Purpose.HBS);
//            selectDestinationByProbabilities(trip, probabilities);
//        }
//    }
//
//    private void processHBO(MitoHousehold household) {
//        List<MitoTrip> trips = household.getTripsByPurpose().get(Purpose.HBO);
//        for (MitoTrip trip : trips) {
//            double individualBudget = household.getTravelTimeBudgetForPurpose(Purpose.HBO) / trips.size();
//            Map<Integer, Double> probabilities = getProbabilities(trip, individualBudget, Purpose.HBO);
//            selectDestinationByProbabilities(trip, probabilities);
//        }
//    }
//
//    private Map<Integer, Double> getProbabilities(MitoTrip trip, double budget, Purpose purpose) {
//
//
//        double probabilitySum = 0;
//        for (Zone zone : dataSet.getZones().values()) {
//            if (exceedsTravelTimeBudget(trip.getTripOrigin(), zone.getZoneId(), budget)) {
//                logger.debug("Travel time to destination " + zone.getZoneId() + " exceeds budget for " + trip);
//                continue;
//            }
//            double utility = calculateUtility(trip.getTripOrigin(), zone, purpose);
//            double probability = Math.exp(utility);
//            probabilitySum += probability;
//            probabilities.put(zone.getZoneId(), probability);
//        }
//
//        if (probabilitySum > 0) {
//            MitoUtil.scaleMapBy(probabilities, 1 / probabilitySum);
//        } else {
//            logger.debug("No zone qualifies for " + trip + ".");
//            return null;
//        }
//        return probabilities;
//    }
//
//    private void selectDestinationByProbabilities(MitoTrip trip, Map<Integer, Double> probabilities) {
//        if (probabilities == null || probabilities.isEmpty()) {
//            trip.setTripDestination(trip.getTripOrigin());
//            logger.warn("No destination found for " + trip + ".");
//            return;
//        }
//        Integer selectedZone = MitoUtil.select(probabilities, 1);
//        trip.setTripDestination(selectedZone);
//    }
//
//    private boolean exceedsTravelTimeBudget(int tripOrigin, int tripDestination, double budget) {
//        if (dataSet.getAutoTravelTimes().getTravelTimeFromTo(tripOrigin, tripDestination) > budget) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    private double calculateUtility(int tripOrigin, Zone zone, Purpose purpose) {
//
//
//
//        if (purpose.equals(Purpose.HBS)) {
//
//        } else if (purpose.equals(Purpose.HBO)) {
//
//        }
//        return 0;
//    }
//}
