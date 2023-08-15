package de.tum.bgu.msm.modules.tripDistribution.coordinatedDestination;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class DestinationCoordination extends Module {

    private final static Logger logger = Logger.getLogger(DestinationCoordination.class);
    private final double targetShareOfCoordination_no = 0.51;
    private int coordinateTrip;
    private int failPurposeMatch;
    private int failTimeMatch;
    private int failSNMatch;
    private int microlocationCoordinated;

    public DestinationCoordination(DataSet dataSet, List<Purpose> purposes) {
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
        List<MitoPerson> personList = dataSet.getPersons().values().stream().collect(Collectors.toList());
        Collections.shuffle(personList,MitoUtil.getRandomObject());
        Map<MitoTrip,Map<Integer,SocialNetworkType>> potentialEgoAlterTripMap = new HashMap<>();
        for(MitoPerson ego : personList){

            Map<Integer,SocialNetworkType> alterTrips = new HashMap<>();

            for (SocialNetworkType type : ego.getAlterLists().keySet()){
                for(int alterId : ego.getAlterLists().get(type)){
                    for(MitoTrip alterTrip : dataSet.getPersons().get(alterId).getTrips()){
                        if(alterTrip.getTripPurpose().equals(Purpose.HBW) || alterTrip.getTripPurpose().equals(Purpose.HBE)){
                            continue;
                        }
                        alterTrips.put(alterTrip.getId(),type);
                    }
                }
            }

            for (MitoTrip egoTrip : ego.getTrips()){
                if(egoTrip.getTripPurpose().equals(Purpose.HBW) || egoTrip.getTripPurpose().equals(Purpose.HBE)){
                    continue;
                }

                Map<Integer,SocialNetworkType> potentialTrips = new HashMap<>();
                for(Map.Entry<Integer, SocialNetworkType> entry : alterTrips.entrySet()){
                    potentialTrips.put(entry.getKey(),entry.getValue());
                }

                //rule 1: filter out trips which purposes do not match
                List<Purpose> matchedPurposes = getMatchedPurpose(egoTrip.getTripPurpose());
                potentialTrips.keySet().removeIf(tripId -> !matchedPurposes.contains(dataSet.getTrips().get(tripId).getTripPurpose()));

                if(potentialTrips.size()==0){
                    failPurposeMatch++;
                    continue;
                }

                //rule 2: filter out trips which time of day difference > 6 hr
                int egoTripArrivalInMinutes = egoTrip.getArrivalInMinutes();
                potentialTrips.keySet().removeIf(tripId -> Math.abs(egoTripArrivalInMinutes-dataSet.getTrips().get(tripId).getArrivalInMinutes())>=(6.*60.));

                if(potentialTrips.size()==0){
                    failTimeMatch++;
                    continue;
                }

                potentialEgoAlterTripMap.put(egoTrip, potentialTrips);
            }
        }

        int discretionaryTripCounts = dataSet.getTrips().values().stream().filter(mitoTrip -> !mitoTrip.getTripPurpose().equals(Purpose.HBW)&!mitoTrip.getTripPurpose().equals(Purpose.HBE)).collect(Collectors.toList()).size();
        double newShareOfCoordination_no = (targetShareOfCoordination_no * discretionaryTripCounts-(discretionaryTripCounts-potentialEgoAlterTripMap.size())) / potentialEgoAlterTripMap.size();

        if(newShareOfCoordination_no<0||newShareOfCoordination_no>targetShareOfCoordination_no){
            logger.error("Calculation error in destination coordination. No coordination share: " + newShareOfCoordination_no +
                    " discretionary trip count: " + discretionaryTripCounts +
                    " potential ego trip count: " + potentialEgoAlterTripMap.size());
        }else{
            logger.info("Calculation info in destination coordination. No coordination share: " + newShareOfCoordination_no +
                    " discretionary trip count: " + discretionaryTripCounts +
                    " potential ego trip count: " + potentialEgoAlterTripMap.size());
        }
        
        
        //draw for no/has coordination
        potentialEgoAlterTripMap.keySet().removeIf(tripId -> MitoUtil.getRandomObject().nextDouble()<newShareOfCoordination_no);

        List<MitoTrip> shuffledTripList = potentialEgoAlterTripMap.keySet().stream().collect(Collectors.toList());
        Collections.shuffle(shuffledTripList);
        //choose coordination type and select one trip from type
        for(MitoTrip egoTrip : shuffledTripList){
            Set<Integer> potentialTrips;
            
            potentialTrips = potentialEgoAlterTripMap.get(egoTrip).entrySet().stream().
                    filter(entry->SocialNetworkType.HOUSEHOLD.equals(entry.getValue())).
                    collect(Collectors.toMap(e->e.getKey(),e->e.getValue())).keySet();

            potentialTrips.removeIf(tripId -> dataSet.getTrips().get(tripId).getCoordinatedTripId()>0);
            
            if (potentialTrips.size()>0){
                findCoordinatedTrip(egoTrip, potentialTrips);
                continue;
            }

            potentialTrips = potentialEgoAlterTripMap.get(egoTrip).entrySet().stream().
                    filter(entry->SocialNetworkType.FRIEND.equals(entry.getValue())).
                    collect(Collectors.toMap(e->e.getKey(),e->e.getValue())).keySet();

            potentialTrips.removeIf(tripId -> dataSet.getTrips().get(tripId).getCoordinatedTripId()>0);

            if (potentialTrips.size()>0){
                findCoordinatedTrip(egoTrip, potentialTrips);
                continue;
            }

            potentialTrips = potentialEgoAlterTripMap.get(egoTrip).entrySet().stream().
                    filter(entry->SocialNetworkType.COWORKER.equals(entry.getValue())||SocialNetworkType.SCHOOLMATE.equals(entry.getValue())).
                    collect(Collectors.toMap(e->e.getKey(),e->e.getValue())).keySet();

            potentialTrips.removeIf(tripId -> dataSet.getTrips().get(tripId).getCoordinatedTripId()>0);

            if (potentialTrips.size()>0){
                findCoordinatedTrip(egoTrip, potentialTrips);
                continue;
            }


            potentialTrips = potentialEgoAlterTripMap.get(egoTrip).entrySet().stream().
                    filter(entry->SocialNetworkType.NEIGHBOR.equals(entry.getValue())).
                    collect(Collectors.toMap(e->e.getKey(),e->e.getValue())).keySet();

            potentialTrips.removeIf(tripId -> dataSet.getTrips().get(tripId).getCoordinatedTripId()>0);

            if (potentialTrips.size()>0){
                findCoordinatedTrip(egoTrip, potentialTrips);
                continue;
            }

            failSNMatch++;
        }


    }

    private void findCoordinatedTrip(MitoTrip egoTrip, Set<Integer> potentialTrips) {
        //apply weight for potential trips based on distance
        Map<Integer, Double> probabilities = new HashMap<>();
        for(int potentialTripId : potentialTrips){
            final int egoDestinationId = egoTrip.getTripDestination().getZoneId();
            final int alterDestinationId = dataSet.getTrips().get(potentialTripId).getTripDestination().getZoneId();
            final double distanceOffset = dataSet.getTravelDistancesAuto().getTravelDistance(egoDestinationId, alterDestinationId);
            probabilities.put(potentialTripId, 1./distanceOffset);
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

    private boolean isNonHomeBasedPurpose(Purpose p) {
        if (p.equals(Purpose.NHBW) || p.equals(Purpose.NHBO)) {
            return true;
        } else {
            return false;
        }

    }
}


