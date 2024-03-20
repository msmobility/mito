package de.tum.bgu.msm.analysis.logsumAccessibility;

import de.tum.bgu.msm.analysis.DummyOccupation;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.EconomicStatusReader;
import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
import de.tum.bgu.msm.io.input.readers.OmxSkimsReader;
import de.tum.bgu.msm.io.input.readers.ZonesReader;
import de.tum.bgu.msm.resources.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LogsumByAveragePerson_TransportPolicy {

    private final static Logger logger = LogManager.getLogger(LogsumByAveragePerson_TransportPolicy.class);

    private static final Map<Integer, Map<Integer,  Double>> logsumTable = new HashMap<>();
    private static final Map<Integer, Boolean> evForbidden = new HashMap<>();

    private final DataSet dataSet = new DataSet();
    private final ArrayList<MitoTrip> trips = new ArrayList<>();
    private Purpose givenPurpose;
    private final Map<Integer, Double> logsums = new HashMap<>();
    MitoHousehold hh;
    MitoPerson pp;
    private Map<Mode, Map<String, Double>> coef;



    public static void main(String[] args) throws FileNotFoundException {
        LogsumByAveragePerson_TransportPolicy logsumByAveragePerson = new LogsumByAveragePerson_TransportPolicy();


        logsumByAveragePerson.setup();
        logsumByAveragePerson.load();
        logsumByAveragePerson.run();
    }


    public void setup() throws FileNotFoundException {
        Resources.initializeResources("D:/data/germany_sd/germany_sd.properties");
    }

    public void load() {
        new ZonesReader(dataSet).read();
        dataSet.setTravelTimes(new SkimTravelTimes());
        new OmxSkimsReader(dataSet).read();
        new EconomicStatusReader(dataSet).read();
        givenPurpose = Purpose.HBW;

        this.coef = new ModeChoiceCoefficientReader(dataSet, givenPurpose, Resources.instance.getModeChoiceCoefficients(givenPurpose)).readCoefficientsForThisPurpose();
    }

    public void run() {

        logger.info("Running logsum-based accessibility calculation for " + trips.size() + " synthetic trips/travellers");
        AtomicInteger counterTrip = new AtomicInteger(0);

        //Todo Initialize the logsumTable
        for (MitoZone origin : dataSet.getZones().values()) {
            logsumTable.put(origin.getId(), new HashMap<>());
            for (MitoZone destination : dataSet.getZones().values()) {
                logsumTable.get(origin.getId()).put(destination.getId(), 0.0);
            }
        }

        createAveragePopulation();
        createSyntheticTrips();

        trips.parallelStream().forEach(tripFromArray -> {
            calculateLogsums(tripFromArray, logsumTable);

            if (counterTrip.getAndIncrement() % 100 == 0) {
                logger.info("Trips logsums assigned: " + counterTrip.get());
            }

        });
        logger.info("Finished synthetic logsum calculator");

        writeLogsumAccessibility(logsumTable);

    }

    private void createSyntheticTrips() {

        for (MitoZone zone : dataSet.getZones().values()) {
            MitoTrip trip = new MitoTrip(zone.getZoneId(), givenPurpose);
            trip.setTripOrigin(zone);
            trip.setTripDestination(zone);
            trip.setPerson(pp);
            trips.add(trip);
        }

    }

    private void calculateLogsums(MitoTrip trip, Map<Integer, Map<Integer, Double>> logsumTable) {
        for (MitoZone destination : dataSet.getZones().values()) {
            logsumTable.get(trip.getTripOrigin().getZoneId())
                    .put(destination.getZoneId(), calculateLogsumsByZone(trip, destination.getZoneId()));
        }
    }

    private double calculateLogsumsByZone(MitoTrip trip, int destination) {
        trip.setTripDestination(dataSet.getZones().get(destination));

        LogsumCalculator calculator = new LogsumCalculator(givenPurpose, null);

        MitoZone originZone = dataSet.getZones().get(trip.getTripOrigin().getZoneId());
        MitoZone destinationZone = dataSet.getZones().get(destination);

        TravelTimes travelTimes = dataSet.getTravelTimes();
        final double travelDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(originZone.getId(), destinationZone.getId());
        final double travelDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(originZone.getId(), destinationZone.getId());

        return calculator.calculateLogsumByZone(givenPurpose, hh, pp, originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, 0);
    }

    private void createAveragePopulation() {
        int hhAutos = 1;
        int income = 1600;
        int age = 32;

        //Todo create an average person based on the trip list
        hh = new MitoHousehold(1, income, 1);
        pp = new MitoPerson(1, MitoOccupationStatus.WORKER, null, age, MitoGender.MALE, true);
        hh.addPerson(pp);
        pp.setHasBicycle(true);
    }

    private void writeLogsumAccessibility(Map<Integer, Map<Integer, Double>> logsumTable){
        PrintWriter pw = null;
        try {
            pw = new PrintWriter("C:/Users/Wei/Desktop/logsumTable_lowEmission.csv");
            pw.println("origin,destination,mode,logsum");

            for (MitoZone destination : dataSet.getZones().values()) {
                pw.println(dataSet.getZones().get(3094).getZoneId() + "," + destination.getId() + "," + logsumTable.get(dataSet.getZones().get(3094).getId()).get(destination.getId()));
            }

            pw.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
