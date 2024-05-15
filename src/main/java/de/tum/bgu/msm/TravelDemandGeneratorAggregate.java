package de.tum.bgu.msm;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceAggregate;
import de.tum.bgu.msm.modules.modeChoice.calculators.CalibratingModeChoiceCalculatorImplAggregate;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculator2017ImplAggregate;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule;
import de.tum.bgu.msm.modules.tripDistribution.*;
import de.tum.bgu.msm.modules.tripGeneration.TripGenerationAggregate;
import de.tum.bgu.msm.modules.tripGeneration.TripsByPurposeGeneratorFactoryPersonBasedHurdleAggregate;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.tum.bgu.msm.io.input.readers.LogsumReader.convertArrayListToIntArray;

/**
 * Generates travel demand for the Microscopic Transport Orchestrator (MITO)
 *
 * @author Ana Moreno
 * Created on May 8, 2024 in Valencia, Spain
 */
public final class TravelDemandGeneratorAggregate {

    private static final Logger logger = Logger.getLogger(TravelDemandGeneratorAggregate.class);
    private final DataSet dataSet;

    private final Module tripGenerationMandatory;
    private final Module personTripAssignmentMandatory;
    private final Module travelTimeBudgetMandatory;
    private final Module distributionMandatory;
    private final Module modeChoiceMandatory;
    private final Module tripGenerationDiscretionary;
    private final Module personTripAssignmentDiscretionary;
    private final Module travelTimeBudgetDiscretionary;
    private final Module distributionDiscretionary;
    private final Module modeChoiceDiscretionary;
    private final Module timeOfDayChoiceMandatory;
    private final Module timeOfDayChoiceDiscretionary;
    private final Module tripScaling;
    private final Module matsimPopulationGenerator;
    private final Module longDistanceTraffic;



    private TravelDemandGeneratorAggregate(
            DataSet dataSet,
            Module tripGenerationMandatory,
            Module personTripAssignmentMandatory,
            Module travelTimeBudgetMandatory,
            Module distributionMandatory,
            Module modeChoiceMandatory,
            Module timeOfDayChoiceMandatory,
            Module tripGenerationDiscretionary,
            Module personTripAssignmentDiscretionary,
            Module travelTimeBudgetDiscretionary,
            Module distributionDiscretionary,
            Module modeChoiceDiscretionary,
            Module timeOfDayChoiceDiscretionary,
            Module tripScaling,
            Module matsimPopulationGenerator,
            Module longDistanceTraffic
            ) {

        this.dataSet = dataSet;
        this.tripGenerationMandatory = tripGenerationMandatory;
        this.personTripAssignmentMandatory = personTripAssignmentMandatory;
        this.travelTimeBudgetMandatory = travelTimeBudgetMandatory;
        this.distributionMandatory = distributionMandatory;
        this.modeChoiceMandatory = modeChoiceMandatory;
        this.timeOfDayChoiceMandatory = timeOfDayChoiceMandatory;
        this.tripGenerationDiscretionary = tripGenerationDiscretionary;
        this.personTripAssignmentDiscretionary = personTripAssignmentDiscretionary;
        this.travelTimeBudgetDiscretionary = travelTimeBudgetDiscretionary;
        this.distributionDiscretionary = distributionDiscretionary;
        this.modeChoiceDiscretionary = modeChoiceDiscretionary;
        this.timeOfDayChoiceDiscretionary = timeOfDayChoiceDiscretionary;
        this.tripScaling = tripScaling;
        this.matsimPopulationGenerator = matsimPopulationGenerator;
        this.longDistanceTraffic = longDistanceTraffic;
    }


    public static class Builder {

        private final DataSet dataSet;

        private Module tripGenerationMandatory;
        private Module personTripAssignmentMandatory;
        private Module travelTimeBudgetMandatory;
        private Module distributionMandatory;
        private Module modeChoiceMandatory;
        private Module timeOfDayChoiceMandatory;

        private Module tripGenerationDiscretionary;
        private Module personTripAssignmentDiscretionary;
        private Module travelTimeBudgetDiscretionary;
        private Module distributionDiscretionary;
        private Module modeChoiceDiscretionary;
        private Module timeOfDayChoiceDiscretionary;

        private Module tripScaling;
        private Module matsimPopulationGenerator;
        private Module longDistanceTraffic;

        public Builder(DataSet dataSet, MitoAggregatePersona persona, List<Purpose> purposes) {
            this.dataSet = dataSet;

            Purpose purpose = purposes.get(0);
            if (purpose.equals(Purpose.HBW) ||  purpose.equals(Purpose.HBE)){
                tripGenerationMandatory = new TripGenerationAggregate(dataSet,
                        new TripsByPurposeGeneratorFactoryPersonBasedHurdleAggregate(), purposes, persona);
                travelTimeBudgetMandatory = new TravelTimeBudgetModule(dataSet, purposes);
                //distributionMandatory = new TripDistributionAggregate(dataSet, purposes, false,
                        //new DestinationUtilityCalculatorFactoryImplLogsumAggregate(), persona);
                modeChoiceMandatory = new ModeChoiceAggregate(dataSet, purposes, persona);
                ((ModeChoiceAggregate) modeChoiceMandatory).registerModeChoiceCalculatorAggregate(purpose,
                        new CalibratingModeChoiceCalculatorImplAggregate(
                                new ModeChoiceCalculator2017ImplAggregate(purpose, dataSet),
                                dataSet.getModeChoiceCalibrationDataAggregate()));
                //timeOfDayChoiceMandatory = new TimeOfDayChoice(dataSet, purposes);

            } else {
                tripGenerationDiscretionary = new TripGenerationAggregate(dataSet,
                    new TripsByPurposeGeneratorFactoryPersonBasedHurdleAggregate(), purposes, persona);
                //distributionDiscretionary = new TripDistributionAggregate(dataSet, purposes, false,
                    //new DestinationUtilityCalculatorFactoryImplLogsumAggregate(), persona);
                modeChoiceDiscretionary = new ModeChoiceAggregate(dataSet, purposes, persona);
                ((ModeChoiceAggregate) modeChoiceDiscretionary).registerModeChoiceCalculatorAggregate(purpose,
                        new CalibratingModeChoiceCalculatorImplAggregate(
                                new ModeChoiceCalculator2017ImplAggregate(purpose, dataSet),
                                dataSet.getModeChoiceCalibrationDataAggregate()));
                //timeOfDayChoiceDiscretionary = new TimeOfDayChoice(dataSet, purposes);
            }

           /* tripScaling = new TripScaling(dataSet, purposes);
            matsimPopulationGenerator = new MatsimPopulationGenerator(dataSet, purposes);
            if (Resources.instance.getBoolean(Properties.ADD_EXTERNAL_FLOWS, false)) {
                longDistanceTraffic = new LongDistanceTraffic(dataSet, Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)), purposes);
            }*/


        }

        public TravelDemandGeneratorAggregate build() {

            return new TravelDemandGeneratorAggregate(dataSet,
                    tripGenerationMandatory,
                    personTripAssignmentMandatory,
                    travelTimeBudgetMandatory,
                    distributionMandatory,
                    modeChoiceMandatory,
                    timeOfDayChoiceMandatory,
                    tripGenerationDiscretionary,
                    personTripAssignmentDiscretionary,
                    travelTimeBudgetDiscretionary,
                    distributionDiscretionary,
                    modeChoiceDiscretionary,
                    timeOfDayChoiceDiscretionary,
                    tripScaling,
                    matsimPopulationGenerator,
                    longDistanceTraffic);
        }



        public void setTripGeneration(Module tripGeneration) {
            this.tripGenerationMandatory = tripGeneration;
        }

        public void setPersonTripAssignment(Module personTripAssignment) {
            this.personTripAssignmentMandatory = personTripAssignment;
        }

        public void setTravelTimeBudget(Module travelTimeBudget) {
            this.travelTimeBudgetMandatory = travelTimeBudget;
        }

        public void setDistribution(Module distribution) {
            this.distributionMandatory = distribution;
        }

        public void setModeChoice(Module modeChoice) {
            this.modeChoiceMandatory = modeChoice;
        }

        public void setTimeOfDayChoiceMandatory(Module timeOfDayChoiceMandatory) {
            this.timeOfDayChoiceMandatory = timeOfDayChoiceMandatory;
        }

        public void setTripScaling(Module tripScaling) {
            this.tripScaling = tripScaling;
        }

        public void setMatsimPopulationGenerator(Module matsimPopulationGenerator) {
            this.matsimPopulationGenerator = matsimPopulationGenerator;
        }

        public void setLongDistanceTraffic(Module longDistanceTraffic) {
            this.longDistanceTraffic = longDistanceTraffic;
        }

        public DataSet getDataSet() {
            return dataSet;
        }

        public Module getTripGeneration() {
            return tripGenerationMandatory;
        }

        public Module getPersonTripAssignment() {
            return personTripAssignmentMandatory;
        }

        public Module getTravelTimeBudget() {
            return travelTimeBudgetMandatory;
        }

        public Module getDistribution() {
            return distributionMandatory;
        }

        public Module getModeChoice() {
            return modeChoiceMandatory;
        }

        public Module getTimeOfDayChoiceMandatory() {
            return timeOfDayChoiceMandatory;
        }

        public Module getTripScaling() {
            return tripScaling;
        }

        public Module getMatsimPopulationGenerator() {
            return matsimPopulationGenerator;
        }

        public Module getLongDistanceTraffic() {
            return longDistanceTraffic;
        }
    }

    public void generateTravelDemand(String scenarioName, Purpose purpose) {


        logger.info("Running Module: Aggregated Trip Generation");

        initializeTripMatrix();
        if (purpose.equals(Purpose.HBW)|| purpose.equals(Purpose.HBE)){
            logger.info("Running Module: Aggregated Trip Generation. Purpose " + purpose);
            tripGenerationMandatory.run();
            logger.info("Running Module: Aggregated Trip Distribution. Purpose " + purpose);
            //distributionMandatory.run();
            logger.info("Running Module: Aggregated Mode Choice. Purpose " + purpose);
            modeChoiceMandatory.run();
            /*logger.info("Running time of day choice");
            timeOfDayChoiceMandatory.run();

*/
        } else {
            logger.info("Running Module: Aggregated Trip Generation. Purpose " + purpose);
            tripGenerationDiscretionary.run();
            logger.info("Running Module: Aggregated Trip Distribution. Purpose " + purpose);
            //distributionDiscretionary.run();
            logger.info("Running Module: Aggregated Mode Choice. Purpose " + purpose);
            modeChoiceDiscretionary.run();
            /*logger.info("Running time of day choice");
            timeOfDayChoiceMandatory.run();
*/
        }

        logger.info("Running trip scaling");
        /*tripScaling.run();

        matsimPopulationGenerator.run();

        if (Resources.instance.getBoolean(Properties.ADD_EXTERNAL_FLOWS, false)) {
            longDistanceTraffic.run();
        }

        TripGenerationWriter.writeTripsByPurposeAndZone(dataSet, scenarioName);
        SummarizeDataToVisualize.writeFinalSummary(dataSet, scenarioName);

        if (Resources.instance.getBoolean(Properties.PRINT_MICRO_DATA, true)) {
            SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet);
            SummarizeData.writeOutTrips(dataSet, scenarioName);
        }
        if (Resources.instance.getBoolean(Properties.CREATE_CHARTS, true)) {
            DistancePlots.writeDistanceDistributions(dataSet, scenarioName);
            ModeChoicePlots.writeModeChoice(dataSet, scenarioName);
            SummarizeData.writeCharts(dataSet, scenarioName);
        }
        if (Resources.instance.getBoolean(Properties.WRITE_MATSIM_POPULATION, true)) {
            SummarizeData.writeMatsimPlans(dataSet, scenarioName);
        }*/
    }

    private void initializeTripMatrix() {
        int[] zoneIds = convertArrayListToIntArray(dataSet.getZones().values());
        IndexedDoubleMatrix1D homeBasedTrips = new IndexedDoubleMatrix1D(dataSet.getZones().values());
        homeBasedTrips.assign(0.);

        ConcurrentMap<Mode, IndexedDoubleMatrix2D> tripMatrix = new ConcurrentHashMap<>();
        IndexedDoubleMatrix2D matrix = new IndexedDoubleMatrix2D(zoneIds);
        matrix.assign(0.);
        tripMatrix.put(Mode.pooledTaxi, matrix);

        Map<MitoAggregatePersona, Map<Purpose, Map<AreaTypes.SGType, Double>>> personas = new LinkedHashMap<>();
        for (MitoAggregatePersona pp : dataSet.getAggregatePersonas().values()){
            Map<Purpose, Map<AreaTypes.SGType, Double>> purposes = new LinkedHashMap<>();
            for (Purpose purpose : Purpose.values()){
                Map<AreaTypes.SGType, Double> areas = new LinkedHashMap<>();
                for (AreaTypes.SGType area : AreaTypes.SGType.values()){
                    areas.putIfAbsent(area, 0.);
                }
                purposes.putIfAbsent(purpose, areas);
            }
            personas.putIfAbsent(pp, purposes);
        }

        Map<MitoAggregatePersona, Map<Purpose, Map<AreaTypes.SGType, Double>>> totalTrips = new LinkedHashMap<>();
        for (MitoAggregatePersona pp : dataSet.getAggregatePersonas().values()){
            Map<Purpose, Map<AreaTypes.SGType, Double>> purposes = new LinkedHashMap<>();
            for (Purpose purpose : Purpose.values()){
                Map<AreaTypes.SGType, Double> areas = new LinkedHashMap<>();
                for (AreaTypes.SGType area : AreaTypes.SGType.values()){
                    areas.putIfAbsent(area, 0.);
                }
                purposes.putIfAbsent(purpose, areas);
            }
            totalTrips.putIfAbsent(pp, purposes);
        }

        Map<AreaTypes.SGType, MitoZone> zonesByAreaType = new LinkedHashMap<>();
        zonesByAreaType.put(AreaTypes.SGType.CORE_CITY, dataSet.getZones().get(3351));
        zonesByAreaType.put(AreaTypes.SGType.MEDIUM_SIZED_CITY, dataSet.getZones().get(1426));
        zonesByAreaType.put(AreaTypes.SGType.TOWN, dataSet.getZones().get(1248));
        zonesByAreaType.put(AreaTypes.SGType.RURAL, dataSet.getZones().get(1564));

        final IndexedDoubleMatrix1D hbwTripsAttracted = new IndexedDoubleMatrix1D(dataSet.getZones().values());
        hbwTripsAttracted.assign(0.);

        dataSet.setAggregateTripMatrix(tripMatrix);
        dataSet.setHomeBasedTripsAttractedToZone(homeBasedTrips);
        dataSet.setAverageTripsByPurpose(personas);
        dataSet.setTotalTripsGenByPurpose(personas);
        dataSet.setZonesByAreaType(zonesByAreaType);
        dataSet.setHBWtripsAttracted(hbwTripsAttracted);

    }
}
