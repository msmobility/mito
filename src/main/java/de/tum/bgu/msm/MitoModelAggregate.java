package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoAggregatePersona;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.modules.aggregate.PersonaAggregation;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.ImplementationConfig;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Implements the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Ana Moreno
 * Created on May 8, 2024 in Valencia, Spain
 * <p>
 * To run aggregate MITO, the following data need either to be passed in from another program or
 * need to be read from files and passed in (using method initializeStandAlone):
 * - zones
 * - autoTravelTimes
 * - transitTravelTimes
 * - personTypes
 * - retailEmplByZone
 * - officeEmplByZone
 * - otherEmplByZone
 * - totalEmplByZone
 * - sizeOfZonesInAcre
 */
public final class MitoModelAggregate {

    private static final Logger logger = Logger.getLogger(MitoModelAggregate.class);
    private final String scenarioName;

    private DataSet dataSet;

    private MitoModelAggregate(DataSet dataSet, String scenarioName) {
        this.dataSet = dataSet;
        this.scenarioName = scenarioName;
        MitoUtil.initializeRandomNumber();
    }

    public static MitoModelAggregate standAloneModel(String propertiesFile, ImplementationConfig config) {
        logger.info(" Creating standalone version of MITO ");
        Resources.initializeResources(propertiesFile);
        MitoModelAggregate model = new MitoModelAggregate(new DataSet(), Resources.instance.getString(Properties.SCENARIO_NAME));
        model.readStandAlone(config);
        return model;
    }

    public static MitoModelAggregate initializeModelFromSilo(String propertiesFile, DataSet dataSet, String scenarioName) {
        logger.info(" Initializing MITO from SILO");
        Resources.initializeResources(propertiesFile);
        MitoModelAggregate model = new MitoModelAggregate(dataSet, scenarioName);
        new OmxSkimsReader(dataSet).readOnlyTransitTravelTimes();
        new OmxSkimsReader(dataSet).readSkimDistancesNMT();
        new OmxSkimsReader(dataSet).readSkimDistancesAuto();
        model.readAdditionalData();
        return model;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        logger.info("Started the Aggregated Transport Orchestrator (MITO)");

        new PersonaAggregation(dataSet, Purpose.getAllPurposes()).run();

        for (MitoAggregatePersona persona : dataSet.getAggregatePersonas().values()) {
            for (Purpose purpose : Purpose.getMandatoryPurposes()) {
                List<Purpose> purposes = Collections.singletonList(purpose);
                TravelDemandGeneratorAggregate ttd = new TravelDemandGeneratorAggregate.Builder(dataSet, persona, purposes).build();
                ttd.generateTravelDemand(scenarioName, purposes.get(0));
                printOutline(startTime);            }

            for (Purpose purpose : Purpose.getDiscretionaryPurposes()) {
                List<Purpose> purposes = Collections.singletonList(purpose);
                TravelDemandGeneratorAggregate ttd = new TravelDemandGeneratorAggregate.Builder(dataSet, persona, purposes).build();
                ttd.generateTravelDemand(scenarioName, purposes.get(0));
                printOutline(startTime);
            }
        }
    }

    private void readStandAlone(ImplementationConfig config) {
        dataSet.setYear(Resources.instance.getInt(Properties.SCENARIO_YEAR));
        new ZonesReader(dataSet).read();
        if (Resources.instance.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER)) {
            new BorderDampersReader(dataSet).read();
        }
        new JobReader(dataSet, config.getJobTypeFactory()).read();
        new SchoolsReader(dataSet).read();
        new HouseholdsReader(dataSet).read();
        new HouseholdsCoordReader(dataSet).read();
        new GiveHouseholdsWithoutDwellingsCoordinates(dataSet).read();
        new PersonsReader(dataSet).read();
        new VehicleReader(dataSet).read();
        dataSet.setTravelTimes(new SkimTravelTimes());
        new OmxSkimsReader(dataSet).read();
        readAdditionalData();

    }

    private void readAdditionalData() {
        new TripAttractionRatesReader(dataSet).read();
        new ModeChoiceInputReader(dataSet).read();
        new EconomicStatusReader(dataSet).read();
        new TimeOfDayDistributionsReader(dataSet).read();
        new CalibrationDataReader(dataSet).read();
        new CalibrationRegionMapReader(dataSet).read();
        new BicycleOwnershipReaderAndModel(dataSet).read();
        new TripListReader(dataSet).read();
        new LogsumReader(dataSet).read();


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

    public String getScenarioName() {
        return scenarioName;
    }

    public void setRandomNumberGenerator(Random random) {
        MitoUtil.initializeRandomNumber(random);
    }



}
