package de.tum.bgu.msm.io.input.readers;

import cern.colt.matrix.tfloat.FloatMatrix2D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.MatrixTravelTimes;
import de.tum.bgu.msm.io.input.OMXReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

public class SkimsReader extends OMXReader {

    private static final Logger logger = Logger.getLogger(SkimsReader.class);

    public SkimsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading skims");
        readHighwaySkims();
        readTransitSkims();
        readBusSkimForModeChoice();
        readTrainSkimForModeChoice();
        readTramMetroSkimForModeChoice();
        readTravelDistanceForModeChoice();
    }

    private void readHighwaySkims() {
        FloatMatrix2D timeSkimAutos = super.readAndConvertToFloatMatrix(Resources.INSTANCE.getString(Properties.AUTO_PEAK_SKIM), "mat1", "lookup1");
        dataSet.addTravelTimeForMode("car", new MatrixTravelTimes(timeSkimAutos));
    }

    private void readTransitSkims() {
        FloatMatrix2D timeSkimTransit = super.readAndConvertToFloatMatrix(Resources.INSTANCE.getString(Properties.TRANSIT_PEAK_SKIM), "CheapJrnyTime", "lookup1");
        dataSet.addTravelTimeForMode("pt", new MatrixTravelTimes(timeSkimTransit));
    }

        private void readBusSkimForModeChoice(){
        FloatMatrix2D timeSkimBus = super.readAndConvertToFloatMatrix(Resources.INSTANCE.getString(Properties.BUS_TRAVEL_TIME_SKIM),"mat1", "lookup1");
        dataSet.addTravelTimeForMode("bus", new MatrixTravelTimes(timeSkimBus));
    }

    private void readTramMetroSkimForModeChoice(){
        FloatMatrix2D timeSkimTramMetro = super.readAndConvertToFloatMatrix(Resources.INSTANCE.getString(Properties.TRAM_METRO_TRAVEL_TIME_SKIM),"mat1", "lookup1");
        dataSet.addTravelTimeForMode("tramMetro", new MatrixTravelTimes(timeSkimTramMetro));
    }

    private void readTrainSkimForModeChoice(){
        FloatMatrix2D timeSkimTrain = super.readAndConvertToFloatMatrix(Resources.INSTANCE.getString(Properties.TRAIN_TRAVEL_TIME_SKIM),"mat1", "lookup1");
        dataSet.addTravelTimeForMode("train", new MatrixTravelTimes(timeSkimTrain));
    }

    private void readTravelDistanceForModeChoice(){
        FloatMatrix2D distanceSkimAuto = super.readAndConvertToFloatMatrix(Resources.INSTANCE.getString(Properties.AUTO_TRAVEL_DISTANCE_SKIM),"mat1", "lookup1");
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
        FloatMatrix2D distanceSkimNMT = super.readAndConvertToFloatMatrix(Resources.INSTANCE.getString(Properties.NMT_TRAVEL_DISTANCE_SKIM),"mat1", "lookup1");
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimNMT));
    }


}
