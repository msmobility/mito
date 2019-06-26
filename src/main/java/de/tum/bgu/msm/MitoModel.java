package de.tum.bgu.msm;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.modules.logsumAccessibility.Accessibility;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.ImplementationConfig;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.commons.math.stat.Frequency;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


/**
 * Implements the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 * <p>
 * To run MITO, the following data need either to be passed in from another program or
 * need to be read from files and passed in (using method initializeStandAlone):
 * - zones
 * - autoTravelTimes
 * - transitTravelTimes
 * - timoHouseholds
 * - retailEmplByZone
 * - officeEmplByZone
 * - otherEmplByZone
 * - totalEmplByZone
 * - sizeOfZonesInAcre
 */
public final class MitoModel {

    private static final Logger logger = Logger.getLogger(MitoModel.class);
    private final String scenarioName;

    private DataSet dataSet;

    private MitoModel(DataSet dataSet, String scenarioName) {
        this.dataSet = dataSet;
        this.scenarioName = scenarioName;
        MitoUtil.initializeRandomNumber();
    }

    public static MitoModel standAloneModel(String propertiesFile, ImplementationConfig config) {
        logger.info(" Creating standalone version of MITO ");
        Resources.initializeResources(propertiesFile);
        MitoModel model = new MitoModel(new DataSet(), Resources.INSTANCE.getString(Properties.SCENARIO_NAME));
        model.readStandAlone(config);
        return model;
    }

    public static MitoModel initializeModelFromSilo(String propertiesFile, DataSet dataSet, String scenarioName) {
        logger.info(" Initializing MITO from SILO");
        Resources.initializeResources(propertiesFile);
        MitoModel model = new MitoModel(dataSet, scenarioName);
        new SkimsReader(dataSet).readOnlyTransitTravelTimes();
        new SkimsReader(dataSet).readSkimDistancesNMT();
        new SkimsReader(dataSet).readSkimDistancesAuto();
        model.readAdditionalData();
        return model;
    }

    public void runModel() {
        long startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        TravelDemandGenerator ttd = new TravelDemandGenerator(dataSet);
        ttd.generateTravelDemand(scenarioName);

        printOutline(startTime);
    }

    private void readStandAlone(ImplementationConfig config) {
        dataSet.setYear(Resources.INSTANCE.getInt(Properties.SCENARIO_YEAR));
        new ZonesReader(dataSet).read();
        if (Resources.INSTANCE.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER)) {
            new BorderDampersReader(dataSet).read();
        }
        if (Resources.INSTANCE.getBoolean(Properties.TRIP_GENERATION_POISSON)){
            new AccessibilityZonesReader(dataSet).read();
        }
        new JobReader(dataSet, config.getJobTypeFactory()).read();
        new SchoolsReader(dataSet).read();
        new HouseholdsReader(dataSet).read();
        new HouseholdsCoordReader(dataSet).read();
        new PersonsReader(dataSet).read();
        dataSet.setTravelTimes(new SkimTravelTimes());
        new SkimsReader(dataSet).read();
        readAdditionalData();
    }

    private void readAdditionalData() {
        new TripAttractionRatesReader(dataSet).read();
        new ModeChoiceInputReader(dataSet).read();
        new EconomicStatusReader(dataSet).read();
    }

    private void printOutline(long startTime) {
        String trips = MitoUtil.customFormat("  " + "###,###", dataSet.getTrips().size());
        logger.info("A total of " + trips.trim() + " microscopic trips were generated");
        logger.info("Completed the Microsimulation Transport Orchestrator (MITO)");
        float endTime = MitoUtil.rounder(((System.currentTimeMillis() - startTime) / 60000.f), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes.");
    }

    public DataSet getData() {
        return dataSet;
    }

    public void setBaseDirectory(String baseDirectory) {
        MitoUtil.setBaseDirectory(baseDirectory);
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setRandomNumberGenerator(Random random) {
        MitoUtil.initializeRandomNumber(random);
    }

    public void calculateAccessibility(){
        long startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        logger.info("Running Module: Trip Generation");
        TripGeneration tg = new TripGeneration(dataSet);
        tg.run();
        if (dataSet.getTrips().isEmpty()) {
            logger.warn("No trips created. End of program.");
            return;
        }

        logger.info("Running Module: Logsum-based Accessibility Calculation");
        Accessibility acc = new Accessibility(dataSet);
        acc.run();

        printOutline(startTime);
    }

    public void obtainTripLengthDistribution(){
        logger.info("Obtaining trip length distributions");
        Frequency commuteDistance = new Frequency();
        Frequency commuteTime = new Frequency();
        Frequency studentDistance = new Frequency();
        Frequency studentTime = new Frequency();
        Frequency collegeDistance = new Frequency();
        Frequency collegeTime = new Frequency();
        int tripid = 1;
        double peakHour = dataSet.getPeakHour();
        for (MitoHousehold hh : dataSet.getHouseholds().values()){
            for (MitoPerson pp : hh.getPersons().values()) {
                if (pp.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)) {
                    MitoZone home = hh.getHomeZone();
                    MitoZone destination = pp.getOccupation().getOccupationZone();
                    long tripTime = (long) dataSet.getTravelTimes().getTravelTime(home, destination, peakHour, "car");
                    commuteTime.addValue(tripTime);
                    long tripDistance = (long) dataSet.getTravelDistancesAuto().getTravelDistance(home.getId(), destination.getId());
                    commuteDistance.addValue(tripDistance);

                    MitoTrip tt = new MitoTrip(tripid, Purpose.HBW);
                    tt.setTripOrigin(home);
                    tt.setTripDestination(destination);
                    tt.setPerson(pp);
                    dataSet.addTrip(tt);
                    tripid++;
                }
                if (pp.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)) {
                    MitoZone home = hh.getHomeZone();
                    MitoZone destination = pp.getOccupation().getOccupationZone();
                    long tripTime = (long) dataSet.getTravelTimes().getTravelTime(home, destination, peakHour, "car");
                    long tripDistance = (long) dataSet.getTravelDistancesAuto().getTravelDistance(home.getId(), destination.getId());
                    if (pp.getAge() < 18) {
                        studentTime.addValue(tripTime);
                        studentDistance.addValue(tripDistance);
                    } else {
                        collegeTime.addValue(tripTime);
                        collegeDistance.addValue(tripDistance);
                    }
                    MitoTrip tt = new MitoTrip(tripid, Purpose.HBE);
                    dataSet.addTrip(tt);
                    tripid++;
                    tt.setTripOrigin(home);
                    tt.setTripDestination(destination);
                    tt.setPerson(pp);
                }
            }
        }
        summarizeTrips();
        summarizeFlows(commuteTime, "microData/interimData/tripLengthDistributionWorkTime.csv");
        summarizeFlows(collegeDistance, "microData/interimData/tripLengthDistributionWorkDistance.csv");
        summarizeFlows(studentTime, "microData/interimData/tripLengthDistributionSchoolTime.csv");
        summarizeFlows(studentDistance, "microData/interimData/tripLengthDistributionSchoolDistance.csv");
        summarizeFlows(collegeTime, "microData/interimData/tripLengthDistributionCollegeTime.csv");
        summarizeFlows(collegeDistance, "microData/interimData/tripLengthDistributionCollegeDistance.csv");

    }

    private void summarizeTrips() {
        String outputSubDirectory = "microData/interimData/";

        logger.info("  Writing trips file");
        String file = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + outputSubDirectory + "trips.csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(file, false);
        pwh.println("id,origin,destination,purpose,person,distance,time_auto,time_bus,time_train,time_tram_metro");
        for (MitoTrip trip : dataSet.getTrips().values()) {
            pwh.print(trip.getId());
            pwh.print(",");
            Location origin = trip.getTripOrigin();
            String originId = "null";
            if(origin != null) {
                originId = String.valueOf(origin.getZoneId());
            }
            pwh.print(originId);
            pwh.print(",");


            Location destination = trip.getTripDestination();
            String destinationId = "null";
            if(destination != null) {
                destinationId = String.valueOf(destination.getZoneId());
            }
            pwh.print(destinationId);
            pwh.print(",");

            pwh.print(trip.getTripPurpose());
            pwh.print(",");
            pwh.print(trip.getPerson().getId());
            pwh.print(",");
            if(origin != null && destination != null) {
                double distance = dataSet.getTravelDistancesAuto().getTravelDistance(origin.getZoneId(), destination.getZoneId());
                pwh.print(distance);
                pwh.print(",");
                double timeAuto = dataSet.getTravelTimes().getTravelTime(origin, destination, dataSet.getPeakHour(), "car");
                pwh.print(timeAuto);
                pwh.print(",");
                double timeBus = dataSet.getTravelTimes().getTravelTime(origin, destination, dataSet.getPeakHour(), "bus");
                pwh.print(timeBus);
                pwh.print(",");
                double timeTrain = dataSet.getTravelTimes().getTravelTime(origin, destination, dataSet.getPeakHour(), "train");
                pwh.print(timeTrain);
                pwh.print(",");
                double timeTramMetro = dataSet.getTravelTimes().getTravelTime(origin, destination, dataSet.getPeakHour(), "tramMetro");
                pwh.println(timeTramMetro);
            } else {
                pwh.println("NA,NA,NA,NA,NA");
            }


        }
        pwh.close();
    }

    private void summarizeFlows(Frequency travelTimes, String fileName){
        //to obtain the trip length distribution
        int[] timeThresholds1 = new int[79];
        double[] frequencyTT1 = new double[79];
        for (int row = 0; row < timeThresholds1.length; row++) {
            timeThresholds1[row] = row + 1;
            frequencyTT1[row] = travelTimes.getCumPct(timeThresholds1[row]);
        }
        writeVectorToCSV(timeThresholds1, frequencyTT1, fileName);

    }

    private void writeVectorToCSV(int[] thresholds, double[] frequencies, String outputFile){
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(outputFile, true));
            pw.println("threshold,frequency");
            for (int i = 0; i< thresholds.length; i++) {
                pw.println(thresholds[i] + "," + frequencies[i]);
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
