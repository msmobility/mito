package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceJSCalculator;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tum.bgu.msm.resources.Properties.UAM_CHOICE;

public class AirportTripGeneration {

    private final DataSet dataSet;
    final static AtomicInteger TRIP_ID_COUNTER = new AtomicInteger();
    private int counter = 0;
    private final static Logger LOGGER = Logger.getLogger(AirportTripGeneration.class);
    private final int airportZoneId;
    private AirportNumberOfTripsCalculator numberOfTripsCalculator;
    private AirportDestinationCalculator airportDestinationCalculator;
    private AirportLogsumCalculator airportLogsumCalculator;

    public AirportTripGeneration(DataSet dataSet) {
        this.dataSet = dataSet;
        this.TRIP_ID_COUNTER.set(dataSet.getTrips().size());
        this.airportZoneId = Resources.INSTANCE.getInt(Properties.AIRPORT_ZONE);
        this.numberOfTripsCalculator = new AirportNumberOfTripsCalculator(new InputStreamReader(this.getClass().getResourceAsStream("AirportTripRateCalc")));
        this.airportDestinationCalculator = new AirportDestinationCalculator(new InputStreamReader(TripDistribution.class.getResourceAsStream("AirportTripDistribution")));
        if (Resources.INSTANCE.getBoolean(UAM_CHOICE, true)) {
            this.airportLogsumCalculator = new AirportLogsumCalculator(new InputStreamReader(ModeChoice.class.getResourceAsStream("ModeChoiceUAMIncremental")));

        } else{
            this.airportLogsumCalculator = new AirportLogsumCalculator(new InputStreamReader(ModeChoice.class.getResourceAsStream("ModeChoice")));
        }


    }

    public void run() {

        Map<Integer, Map<MitoHousehold, Double>> hosuseholdProbabilities = calculateHouseholdProbabilities();
        Map<Integer, Integer> popByZone = calculatePopulationByZone();

        Map<Integer, Double> zonalProbabilities = calculateZonalProbability(hosuseholdProbabilities.keySet(), popByZone);


        int tripsToFromAirport = numberOfTripsCalculator.calculateTripRate(dataSet.getYear());
        while (counter < tripsToFromAirport) {
            MitoTrip trip = new MitoTrip(TRIP_ID_COUNTER.incrementAndGet(), Purpose.AIRPORT);
            dataSet.addTrip(trip);
            //look for a zone and household
            int destinationZone = MitoUtil.select(zonalProbabilities, MitoUtil.getRandomObject());
            MitoHousehold hh = MitoUtil.select(hosuseholdProbabilities.get(destinationZone), MitoUtil.getRandomObject());
            if (hh.getTripsForPurpose(Purpose.AIRPORT).isEmpty()){
                List<MitoTrip> trips = new ArrayList<>();
                trips.add(trip);
                hh.setTripsByPurpose(trips, Purpose.AIRPORT);
            } else {
                hh.getTripsForPurpose(Purpose.AIRPORT).add(trip);
            }

            counter++;
        }
        LOGGER.info("Generated " + counter + " trips to or from the airport");
    }

    private Map<Integer,Double> calculateZonalProbability(Set<Integer> zonesWithHh, Map<Integer, Integer> popByZone) {
        Map<Integer, Double> zonalProbability = new HashMap<>();

        for (int zoneId : zonesWithHh){
            MitoZone mitoZone = dataSet.getZones().get(zoneId);
            TravelTimes travelTimes = dataSet.getTravelTimes();
            double travelDistance = dataSet.getTravelDistancesAuto().getTravelDistance(airportZoneId, zoneId);
            double logsum;

            if (Resources.INSTANCE.getBoolean(UAM_CHOICE, true)){
                double processingTime = Double.parseDouble(Resources.INSTANCE.getString(Properties.UAM_BOARDINGTIME));//dataSet.getWaitingTimes().getWaitingTime(trip.getTripOrigin(), trip.getTripDestination(), Mode.uam.toString());
                double uamCost = Double.parseDouble(Resources.INSTANCE.getString(Properties.UAM_COST));
                logsum= airportLogsumCalculator.calculateLogsumForThisZoneUAM(dataSet.getZones().get(airportZoneId), mitoZone, travelTimes, travelDistance, dataSet.getPeakHour(), processingTime, uamCost);

            }else {
                logsum= airportLogsumCalculator.calculateLogsumForThisZone(dataSet.getZones().get(airportZoneId), mitoZone, travelTimes, travelDistance, dataSet.getPeakHour());
            }




            int popEmp = popByZone.get(zoneId) + mitoZone.getTotalEmpl();
            double probability = airportDestinationCalculator.calculateUtilityOfThisZone(popEmp, logsum, mitoZone.getAreaTypeSG());
            zonalProbability.put(mitoZone.getId(), probability);
        }
        LOGGER.info("Assigned probabilities to zones");
        return zonalProbability;
    }

    private Map<Integer, Map<MitoHousehold, Double>> calculateHouseholdProbabilities() {

        Map<Integer, Map<MitoHousehold, Double>> householdProbabilityByZone =  new HashMap<>();

        for (MitoHousehold mitoHousehold : dataSet.getHouseholds().values()){
            int zoneId = mitoHousehold.getZoneId();
            if (!householdProbabilityByZone.containsKey(zoneId)){
                householdProbabilityByZone.put(zoneId, new HashMap<>());
            }
            householdProbabilityByZone.get(zoneId).put(mitoHousehold, Double.valueOf(mitoHousehold.getEconomicStatus()));
        }

        LOGGER.info("Assigned probabilities to households");
        return householdProbabilityByZone;
    }

    private Map<Integer, Integer> calculatePopulationByZone() {

        Map<Integer, Integer> popByZone =  new HashMap<>();

        for (MitoHousehold mitoHousehold : dataSet.getHouseholds().values()){
            int zoneId = mitoHousehold.getZoneId();
            popByZone.putIfAbsent(zoneId, 0);
            popByZone.put(zoneId, popByZone.get(zoneId)+ mitoHousehold.getPersons().size());
        }

        LOGGER.info("Assigned population to zones");
        return popByZone;
    }

}
