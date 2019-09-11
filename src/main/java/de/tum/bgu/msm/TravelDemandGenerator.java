package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.output.OmxMatrixWriter;
import de.tum.bgu.msm.io.output.SummarizeData;
import de.tum.bgu.msm.io.output.SummarizeDataToVisualize;
import de.tum.bgu.msm.io.output.TripGenerationWriter;
import de.tum.bgu.msm.modules.PedestrianModel;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.personTripAssignment.PersonTripAssignment;
import de.tum.bgu.msm.modules.scaling.TripScaling;
import de.tum.bgu.msm.modules.timeOfDay.TimeOfDayChoice;
import de.tum.bgu.msm.modules.trafficAssignment.TrafficAssignment;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;

/**
 * Generates travel demand for the Microscopic Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */
public class TravelDemandGenerator {

    private static final Logger logger = Logger.getLogger(TravelDemandGenerator.class);
    private final DataSet dataSet;

    public TravelDemandGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void generateTravelDemand (String scenarioName) {

        logger.info("Running Module: Microscopic Trip Generation");
        TripGeneration tg = new TripGeneration(dataSet);
        tg.run();
        if(dataSet.getTrips().isEmpty()){
            logger.warn("No trips created. End of program.");
            return;
        }

        logger.info("Running Module: Person to Trip Assignment");
        PersonTripAssignment personTripAssignment = new PersonTripAssignment(dataSet);
        personTripAssignment.run();

        logger.info("Running Module: Travel Time Budget Calculation");
        TravelTimeBudgetModule ttb = new TravelTimeBudgetModule(dataSet);
        ttb.run();

        logger.info("Running Module: Moped Pedestrian Model");
        PedestrianModel pedestrianModel = new PedestrianModel(dataSet);
        pedestrianModel.runMoped();



        logger.info("Running Module: Microscopic Trip Distribution");
        TripDistribution distribution = new TripDistribution(dataSet);
        distribution.run();


        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        ModeChoice modeChoice = new ModeChoice(dataSet);
        modeChoice.run();

        boolean runTimeOfDayChoice = Resources.INSTANCE.getBoolean(Properties.RUN_TIME_OF_DAY_CHOICE, false);
        if (runTimeOfDayChoice) {
            logger.info("Running time of day choice");
            TimeOfDayChoice timeOfDayChoice = new TimeOfDayChoice(dataSet);
            timeOfDayChoice.run();
        }

        boolean runScaling = Resources.INSTANCE.getBoolean(Properties.RUN_TRIP_SCALING, false);
        if (runTimeOfDayChoice && runScaling) {
            logger.info("Running trip scaling");
            TripScaling tripScaling = new TripScaling(dataSet);
            tripScaling.run();
        }

        boolean runAssignment = Resources.INSTANCE.getBoolean(Properties.RUN_TRAFFIC_ASSIGNMENT, false);
        if (runTimeOfDayChoice && runScaling && runAssignment) {
            logger.info("Running traffic assignment in MATsim");
            TrafficAssignment trafficAssignment = new TrafficAssignment(dataSet, scenarioName);
            trafficAssignment.run();
        }

        TripGenerationWriter.writeTripsByPurposeAndZone(dataSet, scenarioName);
        SummarizeDataToVisualize.writeFinalSummary(dataSet, scenarioName);
        if (Resources.INSTANCE.getBoolean(Properties.PRINT_MICRO_DATA, true)) {
            SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet, scenarioName);
            SummarizeData.writeOutTrips(dataSet, scenarioName);
        }
        if(Resources.INSTANCE.getBoolean(Properties.CREATE_CHARTS, true)){
            SummarizeData.writeCharts(dataSet, scenarioName);
        }

        if(Resources.INSTANCE.getBoolean(Properties.PRINT_OUT_SKIM,false)){
            try {

                String fileName = "./scenOutput/" + scenarioName + "/" + dataSet.getYear() + "/" + Resources.INSTANCE.getString(Properties.SKIM_FILE_NAME);
                int dimension = dataSet.getZones().size();
                OmxMatrixWriter.createOmxFile(fileName, dimension);

                SkimTravelTimes tt = (SkimTravelTimes) dataSet.getTravelTimes();
                tt.printOutCarSkim(TransportMode.car, fileName, "timeByTime");

                MatrixTravelDistances td = (MatrixTravelDistances) dataSet.getTravelDistancesAuto();
                td.printOutDistanceSkim(fileName, "distanceByTime");

            } catch (ClassCastException e){
                logger.info("Currently it is not possible to print out a matrix from an object which is not SkimTravelTime");
            }

        }

    }
}
