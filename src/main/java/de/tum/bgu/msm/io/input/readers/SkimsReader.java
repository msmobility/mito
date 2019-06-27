package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.accessTimes.AccessAndEgressVariables;
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
        readAccessTimeSkims();
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
    }

    private void readTravelTimeSkims() {
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("car", Resources.INSTANCE.getString(Properties.AUTO_PEAK_SKIM), "timeByTime", 1/60.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("bus", Resources.INSTANCE.getString(Properties.BUS_TRAVEL_TIME_SKIM), "mat1", 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("tramMetro", Resources.INSTANCE.getString(Properties.TRAM_METRO_TRAVEL_TIME_SKIM), "mat1", 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("train", Resources.INSTANCE.getString(Properties.TRAIN_TRAVEL_TIME_SKIM), "mat1", 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("uam", Resources.INSTANCE.getString(Properties.UAM_TRAVEL_TIME_SKIM), "total_time", 1.);
    }

    private void readAccessTimeSkims() {
        dataSet.getAccessAndEgressVariables().readSkim("transit", AccessAndEgressVariables.AccessVariable.ACCESS_T_MIN,  Resources.INSTANCE.getString(Properties.PT_ACCESS_TIME_SKIM), "mat1", 1.);
        //dataSet.getAccessTimes().readSkim("uam", Resources.INSTANCE.getString(Properties.UAM_Access_TIME_SKIM), "mat1", 1.);
    }

    private void readTravelDistances(){
        IndexedDoubleMatrix2D distanceSkimAuto = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.AUTO_TRAVEL_DISTANCE_SKIM),"distanceByTime", 1. / 1000.);
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
        IndexedDoubleMatrix2D distanceSkimNMT = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.NMT_TRAVEL_DISTANCE_SKIM),"distanceByDistance", 1. / 1000.);
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimNMT));
        //todo temporally we read cost and in the mode choice scripts we divided by 5 to use it as distance. In the future, read directly flying distance
        IndexedDoubleMatrix2D costSkimUAM = super.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.UAM_TRAVEL_COST_SKIM),"cost", 1);
        dataSet.setTravelCostUAM(new MatrixTravelDistances(costSkimUAM));
    }
}
