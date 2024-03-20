package de.tum.bgu.msm.analysis.logsumAccessibility;

import de.tum.bgu.msm.analysis.DummyOccupation;
import de.tum.bgu.msm.analysis.DummyZone;
import de.tum.bgu.msm.analysis.ModeChoiceCalculatorWithPriceFactors;
import de.tum.bgu.msm.common.matrix.Matrix;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.calculators.CalibratingModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculator2017Impl;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LogsumByAveragePerson {

    private final DataSet dataSet = new DataSet();
    private final ArrayList<MitoTrip> trips = new ArrayList<>();
    private Purpose givenPurpose;
    private final Map<Integer, Double> logsums = new HashMap<>();
    MitoHousehold hh;
    MitoPerson pp;
    private Map<Mode, Map<String, Double>> coef;



    private final double detourNMT = 0.8;
//    private final int[] incomes = new int[]{2000, 10000};

    private final static Logger logger = LogManager.getLogger(LogsumByAveragePerson.class);


    public static void main(String[] args) throws FileNotFoundException {
        LogsumByAveragePerson logsumByAveragePerson = new LogsumByAveragePerson();
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

        createAveragePopulation();
        createSyntheticTrips();

        int[] zones = dataSet.getZones().keySet().stream().mapToInt(u -> u).toArray();
        Matrix matrix = new Matrix(dataSet.getZones().keySet().size(), dataSet.getZones().keySet().size());

        trips.parallelStream().forEach(tripFromArray -> {
            calculateLogsums(tripFromArray, matrix);

            if (counterTrip.getAndIncrement() % 100 == 0) {
                logger.info("Trips logsums assigned: " + counterTrip.get());
            }

        });
        logger.info("Finished synthetic logsum calculator");

        writeLogsumAccessibility(matrix);

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

    private void calculateLogsums(MitoTrip trip, Matrix matrix) {
        int[] zones = dataSet.getZones().keySet().stream().mapToInt(u -> u).toArray(); //select all the zones
        Arrays.stream(zones).forEach(zone -> matrix.setValueAt(trip.getTripOrigin().getZoneId(), zone, (float) calculateLogsumsByZone(trip, zone)));
        //double logsumsAllZones = Arrays.stream(logsumsByZones).sum();
        //logsums.put(trip.getTripId(), logsumsAllZones);
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
        int age = 42;

        //Todo create an average person based on the trip list
        hh = new MitoHousehold(1, income, hhAutos);
        pp = new MitoPerson(1, MitoOccupationStatus.WORKER, DummyOccupation.dummy, age, MitoGender.MALE, true);
        hh.addPerson(pp);
        pp.setHasBicycle(true);
    }

    private void writeLogsumAccessibility(Matrix matrix) {

        try {
            PrintWriter pw = new PrintWriter("D:/code/pt_germany/output/skims/sdModeChoiceLogsums/sd" + givenPurpose.toString() + ".csv");
            pw.println("FROM;TO;VALUE");

            for (int i = 1; i <= matrix.getRowCount(); i++) {
                for (int j = 1; j <= matrix.getColumnCount(); j++) {
                    pw.println(i + ";" + j + ";" + matrix.getValueAt(i, j));
                }
            }

            pw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
