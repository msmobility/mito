package uk.cam.mrc.phm.io;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.DataSetImpl;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.AbstractOmxReader;
import de.tum.bgu.msm.io.input.readers.SkimsReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

public class OmxSkimsReaderMCR extends AbstractOmxReader implements SkimsReader {

    private static final Logger LOGGER = Logger.getLogger(OmxSkimsReaderMCR.class);

    public OmxSkimsReaderMCR(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        LOGGER.info("Reading skims");
        readTravelTimeSkims();
        readTravelDistances();
    }

    public void readSkimDistancesAuto(){
        IndexedDoubleMatrix2D distanceSkimAuto = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.AUTO_TRAVEL_DISTANCE_SKIM).toString(),"distanceByTime", 1. / 1000.);
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
    }

    public void readSkimDistancesNMT(){
        IndexedDoubleMatrix2D distanceSkimWalk = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.WALK_DIST_SKIM).toString(),
                Resources.instance.getString(Properties.WALK_DIST_SKIM_MATRIX), 1. / 1000.);
        ((DataSetImpl)dataSet).setTravelDistancesWalk(new MatrixTravelDistances(distanceSkimWalk));
        IndexedDoubleMatrix2D distanceSkimBike = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.BIKE_DIST_SKIM).toString(),
                Resources.instance.getString(Properties.BIKE_DIST_SKIM_MATRIX), 1. / 1000.);
        ((DataSetImpl)dataSet).setTravelDistancesBike(new MatrixTravelDistances(distanceSkimBike));
    }

    public void readOnlyTransitTravelTimes(){
        //todo has to be probably in silo
        SkimTravelTimes skimTravelTimes;
        skimTravelTimes = (SkimTravelTimes) dataSet.getTravelTimes();
        skimTravelTimes.readSkim("pt", Resources.instance.getRelativePath(Properties.PT_PEAK_SKIM).toString(),
                Resources.instance.getString(Properties.PT_PEAK_SKIM_MATRIX), 1/60.);
    }

    private void readTravelTimeSkims() {
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("car", Resources.instance.getRelativePath(Properties.AUTO_PEAK_SKIM).toString(),
                Resources.instance.getString(Properties.AUTO_PEAK_SKIM_MATRIX), 1/60.); //convert second to min, because time is translated to min in mode choice estimation
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("pt", Resources.instance.getRelativePath(Properties.PT_PEAK_SKIM).toString(),
                Resources.instance.getString(Properties.PT_PEAK_SKIM_MATRIX), 1/60.);
        //read bike walk generalized travel time by purpose
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("bikeCommute", Resources.instance.getRelativePath(Properties.BIKE_COST_COMMUTE_SKIM).toString(),
                Resources.instance.getString(Properties.BIKE_COST_COMMUTE_SKIM_MATRIX), 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("bikeDiscretionary", Resources.instance.getRelativePath(Properties.BIKE_COST_DISC_SKIM).toString(),
                Resources.instance.getString(Properties.BIKE_COST_DISC_SKIM_MATRIX), 1.);
        //TODO: no bike time skim for HBA, NHBO, NHBW

        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("walkCommute", Resources.instance.getRelativePath(Properties.WALK_COST_COMMUTE_SKIM).toString(),
                Resources.instance.getString(Properties.WALK_COST_COMMUTE_SKIM_MATRIX), 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("walkDiscretionary", Resources.instance.getRelativePath(Properties.WALK_COST_DISC_SKIM).toString(),
                Resources.instance.getString(Properties.WALK_COST_DISC_SKIM_MATRIX), 1.);
        //TODO: no walk time skim for NHBW
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("walkHBA", Resources.instance.getRelativePath(Properties.WALK_COST_HBA_SKIM).toString(),
                Resources.instance.getString(Properties.WALK_COST_HBA_SKIM_MATRIX), 1/60.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("walkNHBO", Resources.instance.getRelativePath(Properties.WALK_COST_NHBO_SKIM).toString(),
                Resources.instance.getString(Properties.WALK_COST_NHBO_SKIM_MATRIX), 1/60.);
    }

    private void readTravelDistances(){
        IndexedDoubleMatrix2D distanceSkimAuto = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.AUTO_TRAVEL_DISTANCE_SKIM).toString(),
                Resources.instance.getString(Properties.AUTO_TRAVEL_DISTANCE_SKIM_MATRIX), 1. / 1000.); //meter to km
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
        IndexedDoubleMatrix2D distanceSkimWalk = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.WALK_DIST_SKIM).toString(),
                Resources.instance.getString(Properties.WALK_DIST_SKIM_MATRIX), 1. / 1000.);
        ((DataSetImpl)dataSet).setTravelDistancesWalk(new MatrixTravelDistances(distanceSkimWalk));
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimWalk));
        IndexedDoubleMatrix2D distanceSkimBike = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.instance.getRelativePath(Properties.BIKE_DIST_SKIM).toString(),
                Resources.instance.getString(Properties.BIKE_DIST_SKIM_MATRIX), 1. / 1000.);
        ((DataSetImpl)dataSet).setTravelDistancesBike(new MatrixTravelDistances(distanceSkimBike));
    }
}
