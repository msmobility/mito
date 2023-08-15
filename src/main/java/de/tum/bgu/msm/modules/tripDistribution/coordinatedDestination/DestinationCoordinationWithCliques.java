package de.tum.bgu.msm.modules.tripDistribution.coordinatedDestination;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.readers.SocialNetworkCliquesReader;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class DestinationCoordinationWithCliques extends Module {

    private final static Logger logger = Logger.getLogger(DestinationCoordinationWithCliques.class);
    private final double targetShareOfCoordination_no = 0.51;
    private int coordinateTrip;
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
        logger.info(" Finding coordinated destination for each trip");
        destinationCoordination();
        logger.info(" Total Coordinated Trip: " + coordinateTrip);
        logger.info(" Fail Purpose Match: " + failPurposeMatch);
        logger.info(" Fail Time Match: " + failTimeMatch);
        logger.info(" Fail SN Match: " + failSNMatch);
        logger.info(" Coordinated microlocation (school/work/home): " + microlocationCoordinated);
    }

    private void destinationCoordination() {


        SocialNetworkCliquesReader cliqueListReader = new SocialNetworkCliquesReader(dataSet);
        cliqueListReader.read();
        Map<Integer, List<Integer>> cliqueList = cliqueListReader.getCliqueList();

        Map<MitoTrip, Map<Integer, Set<MitoTrip>>> potentialCliqueTripList = new HashMap<>();

        for(Map.Entry<Integer, List<Integer>> clique : cliqueList.entrySet()){
            List<Integer> egoList = clique.getValue();

            // choose a random ego from egoList and get their trip list
            int randomIndex = new Random().nextInt(egoList.size());
            Integer randomSelectedEgo = egoList.get(randomIndex);

            List<Integer> randomSelectedEgoTripList = new ArrayList<>();
            for(MitoTrip egoTrip : dataSet.getPersons().get(randomSelectedEgo).getTrips()){
                if(egoTrip.getTripPurpose().equals(Purpose.HBW) || egoTrip.getTripPurpose().equals(Purpose.HBE)){
                    continue;
                }
                randomSelectedEgoTripList.add(egoTrip.getId());
            }

            // Loop through random selected ego's trip list
            for (Integer tripOfRandomSelectedEgo : randomSelectedEgoTripList) {
                // Create a map to hold matching trips from other egos in clique
                Map<Integer, Set<MitoTrip>> matchingEgoTripListMap = new HashMap<>();
                // Loop through other egos in clique
                for (int i = 0; i < egoList.size(); i++) {
                    // Skip the ego we are already looping through
                    if (i == randomIndex) {
                        continue;
                    }
                    // Get the Set of trips for the chosen ego
                    Set<MitoTrip> otherEgoTripSet = new HashSet<>(dataSet.getPersons().get(egoList.get(i)).getTrips());
                    //rule 1: filter out trips whose purposes do not match
                    List<Purpose> matchedPurposes = getMatchedPurpose(dataSet.getTrips().get(tripOfRandomSelectedEgo).getTripPurpose());
                    otherEgoTripSet.removeIf(trip -> !matchedPurposes.contains(trip.getTripPurpose()));
                    if (otherEgoTripSet.size() == 0) {
                        failPurposeMatch++;
                        continue;
                    }

                    //rule 2: filter out trips with time of day difference > 6 hr
                    int egoTripArrivalInMinutes = dataSet.getTrips().get(tripOfRandomSelectedEgo).getArrivalInMinutes();
                    otherEgoTripSet.removeIf(trip -> Math.abs(egoTripArrivalInMinutes - trip.getArrivalInMinutes()) >= (6. * 60.));
                    if (otherEgoTripSet.size() == 0) {
                        failTimeMatch++;
                        continue;
                    }

                    // Put matching trips into matchingEgoTripListMap
                    matchingEgoTripListMap.put(egoList.get(i),otherEgoTripSet);

                    totalPotentialMatchedTrips+= otherEgoTripSet.size();

                }
                if(!matchingEgoTripListMap.isEmpty()) {
                    potentialCliqueTripList.put(dataSet.getTrips().get(tripOfRandomSelectedEgo), matchingEgoTripListMap);
                }
            }


//            Map<Integer,List<Integer>> cliqueTripList = new HashMap<>();
//            for (int ego : egoList) {
//                List<Integer> egoTripList = new ArrayList<>();
//                for(MitoTrip egoTrip : dataSet.getPersons().get(ego).getTrips()){
//                    if(egoTrip.getTripPurpose().equals(Purpose.HBW) || egoTrip.getTripPurpose().equals(Purpose.HBE)){
//                        continue;
//                    }
//                    egoTripList.add(egoTrip.getId());
//
//                }
//                if (!egoTripList.isEmpty()) {
//                    cliqueTripList.put(ego, egoTripList);
//                }
//            }

//            // Find trips that are compatible by taking the trips of one random ego in clique as reference
//            Map.Entry<Integer, List<Integer>> randomSelectedEgo = cliqueTripList.entrySet().iterator().next();
//            List<Integer> randomSelectedEgoTripList = randomSelectedEgo.getValue();
//
//                // Loop through random selected ego's trip list
//                for (Integer tripOfRandomSelectedEgo : randomSelectedEgoTripList) {
//                    // Create a map to hold matching trips from other egos in clique
//                    Map<Integer, List<Integer>> matchingEgoTripListMap = new HashMap<>();
//
//                    // Loop through other egos in clique
//                    for (Integer ego : cliqueTripList.keySet()) {
//                        // Skip the ego we are already looping through
//                        if (ego.equals(randomSelectedEgo.getKey())) {
//                            continue;
//                        }
//
//                        List<Integer> otherEgoTripList = cliqueTripList.get(ego);
//                        //rule 1: filter out trips whose purposes do not match
//                        List<Purpose> matchedPurposes = getMatchedPurpose(dataSet.getTrips().get(tripOfRandomSelectedEgo).getTripPurpose());
//                        otherEgoTripList.removeIf(tripId -> !matchedPurposes.contains(dataSet.getTrips().get(tripId).getTripPurpose()));
//
//                        if (otherEgoTripList.size() == 0) {
//                            failPurposeMatch++;
//                            continue;
//                        }
//
//                        //rule 2: filter out trips with time of day difference > 6 hr
//                        int egoTripArrivalInMinutes = dataSet.getTrips().get(tripOfRandomSelectedEgo).getArrivalInMinutes();
//                        otherEgoTripList.removeIf(tripId -> Math.abs(egoTripArrivalInMinutes - dataSet.getTrips().get(tripId).getArrivalInMinutes()) >= (6. * 60.));
//
//                        if (otherEgoTripList.size() == 0) {
//                            failTimeMatch++;
//                            continue;
//                        }
//
//                        // Put matching trips into matchingEgoTripListMap
//                        matchingEgoTripListMap.put(ego,otherEgoTripList);
//                    }
//                    if(!matchingEgoTripListMap.isEmpty()) {
//                        potentialCliqueTripList.put(tripOfRandomSelectedEgo, matchingEgoTripListMap);
//                    }
//                }

        }

        int discretionaryTripCounts = dataSet.getTrips().values().stream().filter(mitoTrip -> !mitoTrip.getTripPurpose().equals(Purpose.HBW)&!mitoTrip.getTripPurpose().equals(Purpose.HBE)).collect(Collectors.toList()).size();
        double newShareOfCoordination_no = (targetShareOfCoordination_no * discretionaryTripCounts-(discretionaryTripCounts-totalPotentialMatchedTrips) )/ totalPotentialMatchedTrips;


            logger.info("Calculation info in destination coordination. No coordination share: " + newShareOfCoordination_no +
                    " discretionary trip count: " + discretionaryTripCounts +
                    " potential ego trip count: " + totalPotentialMatchedTrips);



        //draw for no/has coordination
        potentialCliqueTripList.keySet().removeIf(tripId -> MitoUtil.getRandomObject().nextDouble()<targetShareOfCoordination_no);

        List<MitoTrip> shuffledTripList = potentialCliqueTripList.keySet().stream().collect(Collectors.toList());
        Collections.shuffle(shuffledTripList);
        //select one trip from compatible egos in same clique's trips
        for(MitoTrip egoTrip : shuffledTripList) {
            Map<Integer, Set<MitoTrip>> matchingCliqueEgoTripSetMap = potentialCliqueTripList.get(egoTrip);
            for (Map.Entry<Integer, Set<MitoTrip>> matchingCliqueEgo : matchingCliqueEgoTripSetMap.entrySet()){
                Set<MitoTrip> potentialTrips;
                potentialTrips = matchingCliqueEgoTripSetMap.get(matchingCliqueEgo.getKey());
                potentialTrips.removeIf(trip -> trip.getCoordinatedTripId() > 0);
                if (potentialTrips.size() > 0) {
                    findCoordinatedTrip(egoTrip, potentialTrips);
                    continue;
                }
                failSNMatch++;
            }

        }

    }

    private void findCoordinatedTrip(MitoTrip egoTrip, Set<MitoTrip> potentialTrips) {
        //apply weight for potential trips based on distance
        Map<Integer, Double> probabilities = new HashMap<>();
        for(MitoTrip potentialTrip : potentialTrips){
            final int egoDestinationId = egoTrip.getTripDestination().getZoneId();
            final int alterDestinationId = potentialTrip.getTripDestination().getZoneId();
            final double distanceOffset = dataSet.getTravelDistancesAuto().getTravelDistance(egoDestinationId, alterDestinationId);
            probabilities.put(potentialTrip.getTripId(), 1./distanceOffset);
        }

        //select one coordinated trip
        int select = MitoUtil.select(probabilities, MitoUtil.getRandomObject());

        if(select > 0){
            egoTrip.setCoordinatedTripId(select);
            MitoTrip coordinatedTrip = dataSet.getTrips().get(select);
            coordinatedTrip.setCoordinatedTripId(egoTrip.getTripId());
            resetCoordinatedTrip(egoTrip, coordinatedTrip);
            coordinateTrip++;
        }else{
            logger.warn(" Fail to find coordinated trip for trip: " + egoTrip.getId());
        }
    }

    private void resetCoordinatedTrip(MitoTrip egoTrip, MitoTrip coordinatedTrip) {
        //define the primary and secondary trip HBS/HBR/HBO>NHB
        MitoTrip secondaryTrip;
        MitoTrip primaryTrip;
        if (isNonHomeBasedPurpose(coordinatedTrip.getTripPurpose())){
            secondaryTrip = coordinatedTrip;
            primaryTrip = egoTrip;
        }else if (isNonHomeBasedPurpose(egoTrip.getTripPurpose())){
            secondaryTrip = egoTrip;
            primaryTrip = coordinatedTrip;
        }else {
            secondaryTrip = coordinatedTrip;
            primaryTrip = egoTrip;
        }

        secondaryTrip.setTripDestination(primaryTrip.getTripDestination());
        if(primaryTrip.getTripDestination() instanceof MicroLocation){
            microlocationCoordinated++;
        }
        secondaryTrip.setDestinationCoord(primaryTrip.getDestinationCoord());
        secondaryTrip.setArrivalInMinutes(primaryTrip.getArrivalInMinutes());
    }


    private List<Purpose> getMatchedPurpose(Purpose tripPurpose) {
        switch (tripPurpose){
            case HBS:
                return Arrays.asList(new Purpose[]{Purpose.HBS,Purpose.NHBW,Purpose.NHBO});
            case HBR:
                return Arrays.asList(new Purpose[]{Purpose.HBR,Purpose.NHBW,Purpose.NHBO});
            case HBO:
                return Arrays.asList(new Purpose[]{Purpose.HBO,Purpose.NHBW,Purpose.NHBO});
            case NHBW:
            case NHBO:
                return Arrays.asList(new Purpose[]{Purpose.HBS,Purpose.HBO,Purpose.HBR,Purpose.NHBW,Purpose.NHBO});
            default:
                logger.warn("Purpose is not found!");
        }

        return null;
    }

    private boolean  isNonHomeBasedPurpose(Purpose p) {
        if (p.equals(Purpose.NHBW) || p.equals(Purpose.NHBO)) {
            return true;
        } else {
            return false;
        }

    }
}


