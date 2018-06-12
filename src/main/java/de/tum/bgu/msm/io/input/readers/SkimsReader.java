package de.tum.bgu.msm.io.input.readers;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.AbstractOmxReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

public class SkimsReader extends AbstractOmxReader {

    private static final Logger LOGGER = Logger.getLogger(SkimsReader.class);

    public SkimsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        LOGGER.info("  Reading skims");
        readTravelTimeSkims();
        readTravelDistances();
    }

    public void readSkimDistances(){
        readTravelDistances();
    }

    private void readTravelTimeSkims() {
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("car", Resources.INSTANCE.getString(Properties.AUTO_PEAK_SKIM), "timeByTime", 1/60.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("bus", Resources.INSTANCE.getString(Properties.BUS_TRAVEL_TIME_SKIM), "mat1", 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("tramMetro", Resources.INSTANCE.getString(Properties.TRAM_METRO_TRAVEL_TIME_SKIM), "mat1", 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("train", Resources.INSTANCE.getString(Properties.TRAIN_TRAVEL_TIME_SKIM), "mat1", 1.);
    }

    private void readTravelDistances(){
        DoubleMatrix2D distanceSkimAuto = super.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.AUTO_TRAVEL_DISTANCE_SKIM),"distanceByTime", 1. / 1000.);
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
        DoubleMatrix2D distanceSkimNMT = super.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.NMT_TRAVEL_DISTANCE_SKIM),"distanceByDistance", 1. / 1000.);
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimNMT));
    }
}
