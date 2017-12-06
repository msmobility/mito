package de.tum.bgu.msm.io.input.readers;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.MatrixTravelTimes;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.OMXReader;
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
        readAutoSkimForModeChoice();
        readBusSkimForModeChoice();
        readTrainSkimForModeChoice();
        readTramMetroSkimForModeChoice();
        readTravelDistanceForModeChoice();
    }

    private void readHighwaySkims() {
        Matrix timeSkimAutos = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.AUTO_PEAK_SKIM), "mat1", "lookup1");
        dataSet.addTravelTimeForMode("car", new MatrixTravelTimes(timeSkimAutos));
    }

    private void readTransitSkims() {
        Matrix timeSkimTransit = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.TRANSIT_PEAK_SKIM), "CheapJrnyTime", "lookup1");
        dataSet.addTravelTimeForMode("pt", new MatrixTravelTimes(timeSkimTransit));
    }

    private void readAutoSkimForModeChoice(){
        Matrix timeSkimAuto = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.AUTO_TRAVEL_TIME_SKIM),"AutoTravelTime");
        dataSet.addTravelTimeForMode("autoD", new MatrixTravelTimes(timeSkimAuto));
        dataSet.addTravelTimeForMode("autoP", new MatrixTravelTimes(timeSkimAuto));
    }

    private void readBusSkimForModeChoice(){
        Matrix timeSkimBus = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.BUS_TRAVEL_TIME_SKIM),"BusTravelTime");
        dataSet.addTravelTimeForMode("bus", new MatrixTravelTimes(timeSkimBus));
    }

    private void readTramMetroSkimForModeChoice(){
        Matrix timeSkimTramMetro = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.TRAM_METRO_TRAVEL_TIME_SKIM),"TramMetroTravelTime");
        dataSet.addTravelTimeForMode("tramMetro", new MatrixTravelTimes(timeSkimTramMetro));
    }

    private void readTrainSkimForModeChoice(){
        Matrix timeSkimTrain = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.TRAIN_TRAVEL_TIME_SKIM),"TrainTravelTime");
        dataSet.addTravelTimeForMode("train", new MatrixTravelTimes(timeSkimTrain));
    }

    private void readTravelDistanceForModeChoice(){
        Matrix distanceSkim = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.TRAVEL_DISTANCE_SKIM),"TravelDistance");
        dataSet.setTravelDistances(new MatrixTravelDistances(distanceSkim));
    }


}
