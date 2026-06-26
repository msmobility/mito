package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.AbstractParquetReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

import java.util.Collection;

public class ParquetSkimsReader extends AbstractParquetReader implements SkimsReader {
    private static final Logger LOGGER = Logger.getLogger(ParquetSkimsReader.class);

    Collection<MitoZone> lookup = dataSet.getZones().values();

    public ParquetSkimsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        LOGGER.info("Reading skims");
        readTravelTimeSkims();
        readTravelDistances();
    }

    public void readSkimDistancesAuto(){
        IndexedDoubleMatrix2D distanceSkimAuto = AbstractParquetReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.AUTO_TRAVEL_DISTANCE_SKIM).toString(),"FROM","TO","inVehDistance_m", 1. / 1000., lookup);
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
    }

    public void readSkimDistancesNMT(){
        IndexedDoubleMatrix2D distanceSkimNMT = AbstractParquetReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.NMT_TRAVEL_DISTANCE_SKIM).toString(),"FROM","TO","walkDistance_m", 1. / 1000., lookup);
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimNMT));
    }

    public void readOnlyTransitTravelTimes(){
        //todo has to be probably in silo
        SkimTravelTimes skimTravelTimes;
        skimTravelTimes = (SkimTravelTimes) dataSet.getTravelTimes();
        skimTravelTimes.readSkimFromParquet("bus", Resources.instance.getRelativePath(Properties.BUS_TRAVEL_TIME_SKIM).toString(), "FROM", "To", "totalTravelTime_sec",
                1/60., lookup);
        skimTravelTimes.readSkimFromParquet("tramMetro", Resources.instance.getRelativePath(Properties.TRAM_METRO_TRAVEL_TIME_SKIM).toString(), "FROM", "To", "totalTravelTime_sec",
                1/60., lookup);
        skimTravelTimes.readSkimFromParquet("train", Resources.instance.getRelativePath(Properties.TRAIN_TRAVEL_TIME_SKIM).toString(), "FROM", "To", "totalTravelTime_sec",
                1/60., lookup);
    }

    private void readTravelTimeSkims() {
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkimFromParquet("car", Resources.instance.getRelativePath(Properties.AUTO_PEAK_SKIM).toString(), "FROM", "TO", "inVehTime_sec",1/60., lookup);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkimFromParquet("bus", Resources.instance.getRelativePath(Properties.BUS_TRAVEL_TIME_SKIM).toString(), "FROM", "TO", "totalTravelTime_sec",1/60., lookup);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkimFromParquet("tramMetro", Resources.instance.getRelativePath(Properties.TRAM_METRO_TRAVEL_TIME_SKIM).toString(), "FROM", "TO", "totalTravelTime_sec",1/60., lookup);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkimFromParquet("train", Resources.instance.getRelativePath(Properties.TRAIN_TRAVEL_TIME_SKIM).toString(), "FROM", "TO", "totalTravelTime_sec",1/60., lookup);
    }

    private void readTravelDistances(){
        IndexedDoubleMatrix2D distanceSkimAuto = AbstractParquetReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.AUTO_PEAK_SKIM).toString(),
                "FROM","TO", "inVehDistance_m",1. / 1000., lookup);
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
        IndexedDoubleMatrix2D distanceSkimNMT = AbstractParquetReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.NMT_TRAVEL_DISTANCE_SKIM).toString(),
                "FROM","TO", "walkDistance_m",1. / 1000., lookup);
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimNMT));
    }
}
