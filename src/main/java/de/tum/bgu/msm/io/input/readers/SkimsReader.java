package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.AbstractOmxReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

public class SkimsReader extends AbstractOmxReader {

    private static final Logger LOGGER = Logger.getLogger(SkimsReader.class);

    public SkimsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        LOGGER.info("Reading skims");
        readTravelTimeSkims();
        readTravelDistances();
    }

    public void readSkimDistancesAuto(){
        IndexedDoubleMatrix2D distanceSkimAuto = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.AUTO_TRAVEL_DISTANCE_SKIM),"distanceByTime", 1. / 1000.);
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
    }

    public void readSkimDistancesNMT(){
        IndexedDoubleMatrix2D distanceSkimNMT = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.NMT_TRAVEL_DISTANCE_SKIM),"distanceByDistance", 1. / 1000.);
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimNMT));
    }

    public void readOnlyTransitTravelTimes(){
        //todo has to be probably in silo
        SkimTravelTimes skimTravelTimes;
        skimTravelTimes = (SkimTravelTimes) dataSet.getTravelTimes();
        (skimTravelTimes).readSkim("bus", Resources.INSTANCE.getString(Properties.BUS_TRAVEL_TIME_SKIM), "mat1", 1.);
        (skimTravelTimes).readSkim("tramMetro", Resources.INSTANCE.getString(Properties.TRAM_METRO_TRAVEL_TIME_SKIM), "mat1", 1.);
        (skimTravelTimes).readSkim("train", Resources.INSTANCE.getString(Properties.TRAIN_TRAVEL_TIME_SKIM), "mat1", 1.);
        if (Resources.INSTANCE.getBoolean(Properties.RUN_DISABILITY)) {
            (skimTravelTimes).readSkim("busDisability", Resources.INSTANCE.getString(Properties.BUS_TRAVEL_TIME_SKIM_DISABILITY), "mat1", 1.);
            (skimTravelTimes).readSkim("tramMetroDisability", Resources.INSTANCE.getString(Properties.TRAM_METRO_TRAVEL_TIME_SKIM_DISABILITY), "mat1", 1.);
            (skimTravelTimes).readSkim("trainDisability", Resources.INSTANCE.getString(Properties.TRAIN_TRAVEL_TIME_SKIM_DISABILITY), "mat1", 1.);
        }
    }

    private void readTravelTimeSkims() {
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("car", Resources.INSTANCE.getString(Properties.AUTO_PEAK_SKIM), "timeByTime", 1/60.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("bus", Resources.INSTANCE.getString(Properties.BUS_TRAVEL_TIME_SKIM), "mat1", 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("tramMetro", Resources.INSTANCE.getString(Properties.TRAM_METRO_TRAVEL_TIME_SKIM), "mat1", 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("train", Resources.INSTANCE.getString(Properties.TRAIN_TRAVEL_TIME_SKIM), "mat1", 1.);
        if (Resources.INSTANCE.getBoolean(Properties.RUN_DISABILITY)) {
            ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("carDisability", Resources.INSTANCE.getString(Properties.AUTO_PEAK_SKIM_DISABILITY), "mat1", 1/60.);
            ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("busDisability", Resources.INSTANCE.getString(Properties.BUS_TRAVEL_TIME_SKIM_DISABILITY), "mat1", 1.);
            ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("tramMetroDisability", Resources.INSTANCE.getString(Properties.TRAM_METRO_TRAVEL_TIME_SKIM_DISABILITY), "mat1", 1.);
            ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("trainDisability", Resources.INSTANCE.getString(Properties.TRAIN_TRAVEL_TIME_SKIM_DISABILITY), "mat1", 1.);
        }
    }

    private void readTravelDistances(){
        IndexedDoubleMatrix2D distanceSkimAuto = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.AUTO_TRAVEL_DISTANCE_SKIM),"distanceByTime", 1. / 1000.);
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
        IndexedDoubleMatrix2D distanceSkimNMT = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.NMT_TRAVEL_DISTANCE_SKIM),"distanceByDistance", 1. / 1000.);
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimNMT));
    }
}
