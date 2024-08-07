package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DestinationCoordinationWithCliques extends Module {
    private final static Logger logger = Logger.getLogger(DestinationCoordinationWithCliques.class);
    private int coordinatedTrip;
    private int failPurposeMatch;
    private int failTimeMatch;
    private int failSNMatch;
    private int microlocationCoordinated;
    private int totalPotentialMatchedTrips = 0;

    public DestinationCoordinationWithCliques(DataSet dataSet, List<Purpose> purposes) {

        super(dataSet, purposes);
    }

    @Override
    public void run() {
        logger.info(" Finding coordinated destination for each clique and pair");
        destinationCoordination();

        logger.info(" Fail Purpose Match: " + failPurposeMatch);
        logger.info(" Fail Time Match: " + failTimeMatch);
        logger.info(" Fail SN Match: " + failSNMatch);
        logger.info(" Coordinated microlocation (school/work/home): " + microlocationCoordinated);
    }


    private List<Purpose> getMatchedPurpose(Purpose tripPurpose) {
        switch (tripPurpose) {
            case HBS:
                return Arrays.asList(new Purpose[]{Purpose.HBS, Purpose.NHBW, Purpose.NHBO});
            case HBR:
                return Arrays.asList(new Purpose[]{Purpose.HBR, Purpose.NHBW, Purpose.NHBO});
            case HBO:
                return Arrays.asList(new Purpose[]{Purpose.HBO, Purpose.NHBW, Purpose.NHBO});
            case NHBW:
            case NHBO:
                return Arrays.asList(new Purpose[]{Purpose.HBS, Purpose.HBO, Purpose.HBR, Purpose.NHBW, Purpose.NHBO});
            default:
                logger.warn("Purpose is not found!");
        }

        return null;
    }

    private boolean isNonHomeBasedPurpose(Purpose p) {
        if (p.equals(Purpose.NHBW) || p.equals(Purpose.NHBO)) {
            return true;
        } else {
            return false;
        }

    }

    /* Clique destination coordination method 2 */
    class Trip {
        Location destination;
        Coord coord;
        Day arrivalDay;
        int arrivalInMinutes;

        int mergingPriority; // isNonHomeBasedPurpose() == 1, otherwise 0.
        // Todo:    Potentially want to store Purpose in here.
        //          But, not sure how it'd be merged when trips are combined.
        //          Probably don't want to set it back on the original trips either.

        long egoBitmask;

        int originalEgoIndex;
        int originalEgoTripIndex;
        int precombinedTripIndexA;
        int precombinedTripIndexB;
        int combinedIntoTripIndex;
        int tripGroupSize;
        int coordinatedTripId;
        int originalTripId;
    }

    class PotentiallyCombinedTrip {
        int tripIndexA;
        int tripIndexB;
        float probability;
    }

    private void destinationCoordination() {


        //logger.info("All " + loopedThroughEgos.size() + " egos looped through in pair-wise destination coordination");
        //logger.info("Combined pair trips: " + combinedTrips);


        //loop over cliques and coordinate destinations
        SocialNetworkCliquesReader cliqueListReader = new SocialNetworkCliquesReader(dataSet);
        cliqueListReader.read();
        Map<Integer, List<Integer>> cliqueList = cliqueListReader.getCliqueList();

        //Map<MitoTrip, Map<Integer, Set<MitoTrip>>> potentialCliqueTripList = new HashMap<>();

        int largestGroupSize = 0;
        int cliqueNumber = 0;
        int combinedTrips = 0;
        for (Map.Entry<Integer, List<Integer>> clique : cliqueList.entrySet()) {
            cliqueNumber += 1;
            List<Integer> cliqueEgoList = clique.getValue();
            coordinateTripsForEgoIds(cliqueEgoList);
            int totalOriginalTrips = 0;
            if (cliqueEgoList.size() > largestGroupSize) {
                largestGroupSize = cliqueEgoList.size();
            }
            if (cliqueEgoList.size() > 64) {
                logger.warn("Found clique size of " + cliqueEgoList.size() + " which is larger than 64 - combining trips might not work correctly.");
            }
            //logger.info("clique: " + cliqueNumber + " initial destinations " + totalOriginalTrips + " combined " + combinedTrips);
            //logger.info("Combined clique trips: " + combinedTrips);
        }

        logger.info("largestCliqueSize: " + largestGroupSize);


        //loop over ego-alter pairs and coordinate destinations
        List<MitoPerson> personList = dataSet.getPersons().values().stream().collect(Collectors.toList());
        Collections.shuffle(personList, MitoUtil.getRandomObject());
        Set<Integer> loopedThroughEgos = new HashSet<>();
        List<Integer> egoList = new ArrayList<>();

        for(MitoPerson person : personList) {
            if(person instanceof MitoPersonTengos) {
                MitoPersonTengos ego = (MitoPersonTengos) person;
                loopedThroughEgos.add(ego.getId());
                System.out.println("Processed " + loopedThroughEgos.size() + " egos");
                if(ego.getAlterLists()==null){
                    System.out.println(ego.getId() + "'s alter list is null");
                    continue;
                }
                for (int alterId : ego.getAlterLists()) {
                    if (loopedThroughEgos.contains(alterId)) {
                        continue; // Skip this alter because this particular ego-alter pair has been through coordination already
                    }
                    egoList.clear();
                    egoList.add(ego.getId());
                    egoList.add(alterId);
                    coordinateTripsForEgoIds(egoList);
                }

            }
        }

        int discretionaryTripCounts = dataSet.getTrips().values().stream().filter(mitoTrip -> !mitoTrip.getTripPurpose().equals(Purpose.HBW)&!mitoTrip.getTripPurpose().equals(Purpose.HBE)).collect(Collectors.toList()).size();
        int coordinatedTripCounts = dataSet.getTrips().values().stream()
                .filter(trip -> trip instanceof MitoTripTengos)
                .map(trip -> (MitoTripTengos) trip)
                .filter(mitoTripTengos -> mitoTripTengos.getCoordinatedTripId() > 0)
                .collect(Collectors.toList())
                .size();

        //logger.info("Total Coordinated Trip: " + coordinatedTrip);
        logger.info("Discretionary trip counts: " + discretionaryTripCounts);
        logger.info("Total Coordinated Trip counts: " + coordinatedTripCounts);
    }

    public void coordinateTripsForEgoIds(List<Integer> egoIds){
        List<Trip> trips = new ArrayList<>();
        List<PotentiallyCombinedTrip> potentiallyCombinedTrips = new ArrayList<>();
        int totalOriginalTrips = 0;
        int egoIndex = 0;

        for (Integer ego : egoIds) {
            Set<MitoTrip> egoTrips = dataSet.getPersons().get(ego).getTrips();

            int egoTripIndex = 0;
            for (MitoTrip egoTrip : egoTrips) {
                if (egoTrip.getTripPurpose().equals(Purpose.HBW) || egoTrip.getTripPurpose().equals(Purpose.HBE)) {
                    egoTripIndex += 1;
                    continue;
                }
                if (egoTrip instanceof MitoTripTengos) {
                    MitoTripTengos egoTripTengos = (MitoTripTengos) egoTrip;

                    if (egoTripTengos.getCoordinatedTripId() > 0) {
                        egoTripIndex += 1;
                        continue;
                    }

                    Trip trip = new Trip();
                    trip.destination = egoTripTengos.getTripDestination();
                    Coord destinationCoord;
                    if(egoTripTengos.getTripDestination() instanceof MicroLocation) {
                        destinationCoord = CoordUtils.createCoord(((MicroLocation) egoTripTengos.getTripDestination()).getCoordinate());
                    } else {
                        destinationCoord =
                                CoordUtils.createCoord(dataSet.getZones().get(egoTripTengos.getTripDestination().getZoneId()).getRandomCoord(MitoUtil.getRandomObject()));
                    }
                    trip.coord = destinationCoord;
                    trip.arrivalDay = egoTripTengos.getArrivalDay();
                    trip.arrivalInMinutes = egoTripTengos.getArrivalInMinutes();
                    if (isNonHomeBasedPurpose(egoTripTengos.getTripPurpose())) {
                        trip.mergingPriority = 1;
                    } else {
                        trip.mergingPriority = 0;
                    }

                    trip.egoBitmask = 1 << egoIndex;
                    trip.originalEgoIndex = egoIndex;
                    trip.originalEgoTripIndex = egoTripIndex;
                    trip.precombinedTripIndexA = -1;
                    trip.precombinedTripIndexB = -1;
                    trip.combinedIntoTripIndex = -1;
                    trip.tripGroupSize = 1;
                    trip.coordinatedTripId = 0;
                    trip.originalTripId = egoTripTengos.getTripId();
                    trips.add(trip);
                    totalOriginalTrips += 1;
                }
                egoTripIndex += 1;
            }

            egoIndex += 1;
        }

        //logger.info("trips: " + trips.size());

        // Compare all trips against each other.
        for (int i = 0; i < trips.size() - 1; i++) {
            for (int j = i + 1; j < trips.size(); j++) {
                potentiallyCombineTrips(potentiallyCombinedTrips, trips, i, j);
            }
        }

        //logger.info("potentiallyCombinedTrips: " + potentiallyCombinedTrips.size());

        boolean hasTestedAllPairs = false;
        while (!hasTestedAllPairs) {
            Collections.sort(potentiallyCombinedTrips, (PotentiallyCombinedTrip a, PotentiallyCombinedTrip b) -> {
                if (a.probability < b.probability) return 1;
                if (a.probability > b.probability) return -1;
                return 0;
            });

            int i = 0;
            while (i < potentiallyCombinedTrips.size()) {
                PotentiallyCombinedTrip trip = potentiallyCombinedTrips.get(i);
                if (MitoUtil.getRandomObject().nextDouble() < trip.probability) {
                    trip.probability = 0;
                    potentiallyCombinedTrips.set(i, trip);

                    // Combine
                    Trip tripA = trips.get(trip.tripIndexA);
                    Trip tripB = trips.get(trip.tripIndexB);

                    // Don't combine existing trip with this new one if the existing trip has itself already been combined.
                    if ((tripA.combinedIntoTripIndex < 0) && (tripB.combinedIntoTripIndex < 0)) {
                        tripA.combinedIntoTripIndex = trips.size();
                        tripB.combinedIntoTripIndex = trips.size();

                        trips.set(trip.tripIndexA, tripA);
                        trips.set(trip.tripIndexB, tripB);

                        Trip mergedTripData;
                        if (tripB.mergingPriority < tripA.mergingPriority) {
                            mergedTripData = tripB;
                        } else {
                            mergedTripData = tripA;
                        }

                        Trip combinedTrip = new Trip();
//                            combinedTrip.destination = mergedTripData.destination;
//                            combinedTrip.coord = mergedTripData.coord;
                        Coord midpoint = calculateMidpoint(tripA.coord, tripB.coord, tripA.tripGroupSize,tripB.tripGroupSize);
                        Location combinedDestination = findZonesForCoordinates(midpoint.getX(), midpoint.getY());
                        if (combinedDestination == null) {
                            combinedTrip.destination = mergedTripData.destination;
                            combinedTrip.coord = mergedTripData.coord;
                        } else {
                            combinedTrip.coord = midpoint;
                            combinedTrip.destination = combinedDestination;
                        }
                        combinedTrip.arrivalDay = mergedTripData.arrivalDay;
                        combinedTrip.arrivalInMinutes = mergedTripData.arrivalInMinutes;
                        combinedTrip.mergingPriority = mergedTripData.mergingPriority;
                        combinedTrip.egoBitmask = tripA.egoBitmask | tripB.egoBitmask;
                        combinedTrip.originalEgoIndex = -1;
                        combinedTrip.originalEgoTripIndex = -1;
                        combinedTrip.precombinedTripIndexA = trip.tripIndexA;
                        combinedTrip.precombinedTripIndexB = trip.tripIndexB;
                        combinedTrip.combinedIntoTripIndex = -1;
                        combinedTrip.tripGroupSize = tripA.tripGroupSize + tripB.tripGroupSize;
                        if (tripA.coordinatedTripId != 0 || tripB.coordinatedTripId != 0){
                            combinedTrip.coordinatedTripId = (tripA.coordinatedTripId != 0) ? tripA.coordinatedTripId : tripB.coordinatedTripId;
                        } else{
                            combinedTrip.coordinatedTripId = mergedTripData.originalTripId;
                        }
                        trips.add(combinedTrip);


                        coordinatedTrip += 1;

                        //logger.info("added combined trip: " + trip.tripIndexA + " and " + trip.tripIndexB + "(" + trips.size() + " total)");

                        // Experimental.
                        for (int j = 0; j < trips.size() - 1; j++) {
                            potentiallyCombineTrips(potentiallyCombinedTrips, trips, j, trips.size() - 1);
                            //logger.info("added potentiallyCombinedTrip: " + j + " and " + (trips.size() - 1) + "(" + potentiallyCombinedTrips.size() + " total)");
                        }

                        break;
                    }
                } else {
                    trip.probability = 0;
                    potentiallyCombinedTrips.set(i, trip);
                }

                i += 1;
            }

            if (i == potentiallyCombinedTrips.size()) {
                hasTestedAllPairs = true;
            }
        }

        for (int i = totalOriginalTrips; i < trips.size(); i++) {
            recurseUpdateOriginalEgoTrips(egoIds, trips, i, i);
        }
    }

    public void potentiallyCombineTrips(List<PotentiallyCombinedTrip> potentiallyCombinedTrips, List<Trip> trips, int tripIndexA, int tripIndexB) {
        Trip tripA = trips.get(tripIndexA);
        Trip tripB = trips.get(tripIndexB);

        // Don't allow combining trips if someone is going on both.
        if ((tripA.egoBitmask & tripB.egoBitmask) != 0) return;

        // Don't combine existing trip with this new one if the existing trip has itself already been combined.
        if ((tripA.combinedIntoTripIndex >= 0) || (tripB.combinedIntoTripIndex >= 0)) return;

        // Don't combine trips on different days
        if (tripA.arrivalDay != tripB.arrivalDay) return;
        // Don't combine trips that have arrival time difference of greater than X (set to 6 hours)
        if (Math.abs(tripA.arrivalInMinutes-tripB.arrivalInMinutes)>=(6.*60.)) return;

        // Going ahead to store this as a potential pair that can be combined.
        int totalEgosTripA = Long.bitCount(tripA.egoBitmask);
        int totalEgosTripB = Long.bitCount(tripB.egoBitmask);

        double distance = dataSet.getTravelDistancesAuto().getTravelDistance(
                tripA.destination.getZoneId(),
                tripB.destination.getZoneId());

/*
        final float COEFF = 0.02F; // p = 0.5 if totalEgos = 2 and distance = 2km.
        final float PEOPLE_COEFF = 3.0f;
        final float DISTANCE_COEFF= 0.5f;

        //probability same for > or < 1 km
        float probability = (float) (COEFF * Math.pow(totalEgosTripA + totalEgosTripB-1, PEOPLE_COEFF) / Math.pow(distance, DISTANCE_COEFF));
*/

        final float COEFF = 0.17F; // p = 0.5 if totalEgos = 2 and distance = 2km.
        final float PEOPLE_COEFF = 1.5f;
        final float DISTANCE_COEFF= 1.5f;

        //probability same for > or < 1 km
        float probability = (float) (COEFF * Math.pow(totalEgosTripA + totalEgosTripB-1, PEOPLE_COEFF) / Math.pow(distance, DISTANCE_COEFF));

        //probability flattens after 1km
/*        float probability;
        if (distance >= 1){
            probability = (float) (COEFF * Math.pow(totalEgosTripA + totalEgosTripB-1, PEOPLE_COEFF) / Math.pow(distance, DISTANCE_COEFF));
        } else {
            probability = (float) (COEFF * (1/distance));
        }*/


        PotentiallyCombinedTrip potentiallyCombinedTrip = new PotentiallyCombinedTrip();
        potentiallyCombinedTrip.tripIndexA = tripIndexA;
        potentiallyCombinedTrip.tripIndexB = tripIndexB;
        potentiallyCombinedTrip.probability = probability;
        potentiallyCombinedTrips.add(potentiallyCombinedTrip);
    }

    public void recurseUpdateOriginalEgoTrips(List<Integer> egoList, List<Trip> trips, int storeTripIndex, int recurseTripIndex) {
        Trip trip = trips.get(recurseTripIndex);
        if (trip.precombinedTripIndexA >= 0) {
            recurseUpdateOriginalEgoTrips(egoList, trips, storeTripIndex, trip.precombinedTripIndexA);
            recurseUpdateOriginalEgoTrips(egoList, trips, storeTripIndex, trip.precombinedTripIndexB);
        } else {
            Trip storeTrip = trips.get(storeTripIndex);

            int egoId = egoList.get(trip.originalEgoIndex);
            Set<MitoTrip> egoTrips = dataSet.getPersons().get(egoId).getTrips();

            int egoTripIndex = trip.originalEgoTripIndex;
            for (MitoTrip egoTrip : egoTrips) {
                if (egoTrip instanceof MitoTripTengos) {
                    MitoTripTengos egoTripTengos = (MitoTripTengos) egoTrip;
                    if (egoTripIndex == 0) {
                        egoTripTengos.setTripDestination(storeTrip.destination);
                        egoTripTengos.setDestinationCoord(storeTrip.coord);
                        egoTripTengos.setArrivalDay(storeTrip.arrivalDay);
                        egoTripTengos.setArrivalInMinutes(storeTrip.arrivalInMinutes);
                        egoTripTengos.setCoordinatedTripId(storeTrip.coordinatedTripId);
                        break;
                    }
                }
                egoTripIndex -= 1;
            }
        }
    }

    public static Coord calculateMidpoint(Coord coord1, Coord coord2, int tripAGroupSize, int tripBGroupSize) {

        double midX = roundToNearest0_5((coord1.getX()*tripAGroupSize + coord2.getX()*tripBGroupSize) / (tripAGroupSize+tripBGroupSize));
        double midY = roundToNearest0_5((coord1.getY()*tripAGroupSize + coord2.getY()*tripBGroupSize) / (tripAGroupSize+tripBGroupSize));
        return new Coord(midX, midY);
    }
    public static double roundToNearest0_5(double value) {
        return Math.round(value * 2) / 2.0;
    }

    public Location findZonesForCoordinates(double x, double y) {

        // Create a point geometry for the input coordinates
        Geometry pointGeometry = new GeometryFactory().createPoint(new Coordinate(x, y));

        // Check if the point is within the geometry of each zone
        for (MitoZone zone : dataSet.getZones().values()) {
            Geometry zoneGeometry = zone.getGeometry();
            if (zoneGeometry != null && zoneGeometry.contains(pointGeometry)) {
                return zone;
            }
        }

        return null;
    }
}
