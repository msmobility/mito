package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.AbstractOmxReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CsvGzSkimsReader implements SkimsReader {


    private final DataSet dataSet;

    public CsvGzSkimsReader(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public void read() {
        readSkimDistancesAuto();
        readSkimDistancesNMT();
        readTravelTimeSkims();
    }


    @Override
    public void readSkimDistancesAuto() {
        IndexedDoubleMatrix2D distanceSkimAuto = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.AUTO_TRAVEL_DISTANCE_SKIM).toString(),"distanceByTime", 1. / 1000.);
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));

    }

    @Override
    public void readSkimDistancesNMT() {
        IndexedDoubleMatrix2D distanceSkimNMT = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.NMT_TRAVEL_DISTANCE_SKIM).toString(),"distanceByDistance", 1. / 1000.);
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimNMT));
    }

    @Override
    public void readOnlyTransitTravelTimes() {
        Collection<MitoZone> lookup = dataSet.getZones().values();
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkimFromCsvGz("train", Resources.instance.getRelativePath(Properties.TRAIN_TRAVEL_TIME_SKIM).toString(), 1/60., lookup);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkimFromCsvGz("bus", Resources.instance.getRelativePath(Properties.BUS_TRAVEL_TIME_SKIM).toString(), 1/60., lookup);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkimFromCsvGz("tramMetro", Resources.instance.getRelativePath(Properties.TRAM_METRO_TRAVEL_TIME_SKIM).toString(), 1/60., lookup);
    }

    private void readTravelTimeSkims() {
        Collection<MitoZone> lookup = dataSet.getZones().values();
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkimFromCsvGz("train", Resources.instance.getRelativePath(Properties.TRAIN_TRAVEL_TIME_SKIM).toString(), 1/60., lookup);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkimFromCsvGz("bus", Resources.instance.getRelativePath(Properties.BUS_TRAVEL_TIME_SKIM).toString(), 1/60., lookup);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkimFromCsvGz("tramMetro", Resources.instance.getRelativePath(Properties.TRAM_METRO_TRAVEL_TIME_SKIM).toString(), 1/60., lookup);

    }

}
