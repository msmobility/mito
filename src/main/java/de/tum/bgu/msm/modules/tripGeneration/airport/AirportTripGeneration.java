package de.tum.bgu.msm.modules.tripGeneration.airport;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.calculators.AirportModeChoiceCalculator;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportTripGeneration {

    private final DataSet dataSet;
    private final static AtomicInteger TRIP_ID_COUNTER = new AtomicInteger();
    private int counter = 0;
    private final static Logger LOGGER = Logger.getLogger(AirportTripGeneration.class);
    private final int airportZoneId;

    private final AirportTripGenerator numberOfTripsCalculator;
    private final AirportDestinationCalculator airportDestinationCalculator;
    private final AirportModeChoiceCalculator airportModeChoiceCalculator;

    public AirportTripGeneration(DataSet dataSet) {
        this(dataSet, new AirportTripGeneratorImpl(),
                new AirportDestinationCalculatorImpl(),
                new AirportModeChoiceCalculator());
    }

    public AirportTripGeneration(DataSet dataSet, AirportTripGenerator tripGenerator,
                                 AirportDestinationCalculator airportDestinationCalculator,
                                 AirportModeChoiceCalculator airportModeChoiceCalculator) {
        this.dataSet = dataSet;
        TRIP_ID_COUNTER.set(dataSet.getTrips().size());
        this.airportZoneId = Resources.instance.getInt(Properties.AIRPORT_ZONE);
        this.numberOfTripsCalculator = tripGenerator;
        this.airportDestinationCalculator = airportDestinationCalculator;
        this.airportModeChoiceCalculator = airportModeChoiceCalculator;
    }

    public void run(double scaleFacotForTripGeneration) {

        Map<Integer, Map<MitoHousehold, Double>> hosuseholdProbabilities = calculateHouseholdProbabilities();
        Map<Integer, Integer> popByZone = calculatePopulationByZone();

        Map<Integer, Double> zonalProbabilities = calculateZonalProbability(hosuseholdProbabilities.keySet(), popByZone);


        int tripsToFromAirport = (int) (numberOfTripsCalculator.calculateTripRate(dataSet.getYear()) * scaleFacotForTripGeneration);
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
            double logsum = calculateLogsumForThisZone(dataSet.getZones().get(airportZoneId), mitoZone, travelTimes, travelDistance, dataSet.getPeakHour());
            int popEmp = popByZone.get(zoneId) + mitoZone.getTotalEmpl();
            double probability = airportDestinationCalculator.calculateUtilityOfThisZone(popEmp, logsum, mitoZone.getAreaTypeSG());
            zonalProbability.put(mitoZone.getId(), probability);
        }
        LOGGER.info("Assigned probabilities to zones");
        return zonalProbability;
    }

    private double calculateLogsumForThisZone(MitoZone origin, MitoZone destination, TravelTimes travelTimes, double travelDistance, double peakHour) {
        double[] utilities = airportModeChoiceCalculator.calculateUtilities(Purpose.AIRPORT, null
                , null, origin, destination, travelTimes, travelDistance, -1, peakHour);

        double sum = utilities[0] + utilities[1] + utilities[2] + utilities[3] + utilities[4];
        return Math.log(sum);
    }

    private Map<Integer, Map<MitoHousehold, Double>> calculateHouseholdProbabilities() {

        Map<Integer, Map<MitoHousehold, Double>> householdProbabilityByZone =  new HashMap<>();

        for (MitoHousehold mitoHousehold : dataSet.getHouseholds().values()){
            int zoneId = mitoHousehold.getZoneId();
            if (!householdProbabilityByZone.containsKey(zoneId)){
                householdProbabilityByZone.put(zoneId, new HashMap<>());
            }
            householdProbabilityByZone.get(zoneId).put(mitoHousehold, (double) mitoHousehold.getEconomicStatus());
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
