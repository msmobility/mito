package de.tum.bgu.msm.analysis.logsumAccessibility;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.jobTypes.munich.MunichJobTypeFactory;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
//import de.tum.bgu.msm.io.input.readers.mid.MiDHouseholdsReader;
//import de.tum.bgu.msm.io.input.readers.mid.MiDPersonsReader;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator;
import de.tum.bgu.msm.resources.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

//if want to use this class, first uncomment commented lines and resolve errors due to no mid MID readers
public class LogsumByIndividualMiD {

    private final DataSet dataSet = new DataSet();
    private final DataSet dataSetTempForMiD = new DataSet();

    private final ArrayList<MitoTrip> trips = new ArrayList<>();
    private final Map<Integer, Map<Purpose, Map<Double, Map<Double, Double>>>> logsums = new HashMap<>(); //Ppp.id; purpose; alpha; beta; logsum accessibility

    private final double detourNMT = 0.8;

    private final static Logger logger = LogManager.getLogger(LogsumByIndividualMiD.class);

    private double[] alphas = {1.0}; //{0.6, 0.8, 1.0, 1.2, 1.4};
    private double[] betas = {0.05, 0.10, 0.30, 0.50, 0.80, 1.0, 1.5, 2.0}; //{0.5, 0.8, 1.0, 1.2, 1.5};

    public static void main(String[] args) throws FileNotFoundException {
        LogsumByIndividualMiD logsumByAveragePerson = new LogsumByIndividualMiD();
        logsumByAveragePerson.setup();
        logsumByAveragePerson.load();
        logsumByAveragePerson.run();
    }


    public void setup() throws FileNotFoundException {
        Resources.initializeResources("D:/data/mitoMunich/mito.properties");
    }

    public void load() {
        new ZonesReader(dataSet).read();
        new ZonesReader(dataSetTempForMiD).read();

        new HouseholdsReader(dataSet).read();
        new JobReader(dataSet, new MunichJobTypeFactory()).read();
        new SchoolsReader(dataSet).read();
        new TripAttractionRatesReader(dataSet).read();
        //AttractionCalculator calculator = new AttractionCalculator(dataSet, Purpose.getAllPurposes());
        //calculator.run();

        dataSet.setTravelTimes(new SkimTravelTimes());
        new OmxSkimsReader(dataSet).read();
        //new EconomicStatusReader(dataSet).read();

        //new MiDHouseholdsReader(dataSetTempForMiD).read();
        //new MiDPersonsReader(dataSetTempForMiD).read();
        createSyntheticTrips();
    }

    public void run() {

        logger.info("Running logsum-based accessibility calculation for " + trips.size() + " MiD records.");
        AtomicInteger counterTrip = new AtomicInteger(0);
        int[] zones = dataSet.getZones().keySet().stream().mapToInt(u -> u).toArray(); //select all the zones


        for (double alpha : alphas) {
            for (double beta : betas) {
                trips.parallelStream().forEach(tripFromArray -> {

                    calculateLogsumAccessibility(zones, tripFromArray, alpha, beta);

                    if (counterTrip.getAndIncrement() % 100 == 0) {
                        logger.info("Individual logsums assigned: " + counterTrip.get());
                    }
                });
            }
        }

        writeLogsumAccessibility();
        logger.info("Finished synthetic logsum calculator");
    }

    private void createSyntheticTrips() {
        AtomicInteger counterTrip = new AtomicInteger(0);

        for (MitoPerson person : dataSetTempForMiD.getPersons().values()) {
            for (Purpose purpose : Purpose.getAllPurposes()) {
                MitoTrip trip = new MitoTrip(person.getId(), purpose);
                //trip.setTripOrigin(dataSetTempForMiD.getHouseholds().get(person.getHhId()).getHomeZone());
                //trip.setTripDestination(dataSetTempForMiD.getHouseholds().get(person.getHhId()).getHomeZone());
                trip.setPerson(person);
                trips.add(trip);
                System.out.println(counterTrip.getAndIncrement() + " trips generated.");
            }
        }
    }

    private void calculateLogsumAccessibility(int[] zones, MitoTrip trip, double alpha, double beta) {
        LogsumCalculator calculator = new LogsumCalculator(trip.getTripPurpose(), dataSet);
        double[] logsumAccessibilityByODs = Arrays.stream(zones).mapToDouble(zone -> calculateLogsumAccessibilityByZone(calculator, trip, zone, alpha, beta)).toArray();

        double sumLogsumAccessibility = Arrays.stream(logsumAccessibilityByODs).sum();
        logsums.putIfAbsent(trip.getTripId(), new HashMap<>());
        logsums.get(trip.getTripId()).putIfAbsent(trip.getTripPurpose(), new HashMap<>());
        logsums.get(trip.getTripId()).get(trip.getTripPurpose()).putIfAbsent(alpha, new HashMap<>());
        logsums.get(trip.getTripId()).get(trip.getTripPurpose()).get(alpha).putIfAbsent(beta, sumLogsumAccessibility);
    }

    private double calculateLogsumAccessibilityByZone(LogsumCalculator calculator, MitoTrip trip, int destination, double alpha, double beta) {
        MitoZone originZone = dataSet.getZones().get(trip.getTripOrigin().getZoneId());
        MitoZone destinationZone = dataSet.getZones().get(destination);

        trip.setTripDestination(destinationZone);

        TravelTimes travelTimes = dataSet.getTravelTimes();
        final double travelDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(originZone.getId(), destinationZone.getId());
        final double travelDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(originZone.getId(), destinationZone.getId());

        //MitoHousehold hh = dataSetTempForMiD.getHouseholds().get(trip.getPerson().getHhId());

        //double modeChoiceLogsums = calculator.calculateLogsumByZone(trip.getTripPurpose(), hh, trip.getPerson(), originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, 0);
        //return Math.exp(beta * modeChoiceLogsums) * Math.pow(destinationZone.getTripAttraction(trip.getTripPurpose()), alpha);
        return 0;
    }

    private void writeLogsumAccessibility() {
        try {
            PrintWriter pw = new PrintWriter("//nas.ads.mwn.de/tubv/mob/indiv/ana/TRB2022_induced demand/R/byPurpose/individualLogsumAccessibility_20220715.csv");
            pw.println("pp.id,purpose,alpha,beta,logsumAccessibility");

            for (int ppId : logsums.keySet()) {
                for (Purpose purpose : logsums.get(ppId).keySet()) {
                    for (double alpha : logsums.get(ppId).get(purpose).keySet()) {
                        for (double beta : logsums.get(ppId).get(purpose).get(alpha).keySet()) {
                            pw.println(ppId + "," + purpose.toString() + "," + alpha + "," + beta + "," + logsums.get(ppId).get(purpose).get(alpha).get(beta));
                        }
                    }
                }
            }

            pw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
