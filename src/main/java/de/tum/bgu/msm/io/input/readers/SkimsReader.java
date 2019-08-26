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

    public void readSkimDistancesAuto() {
        IndexedDoubleMatrix2D distanceSkimAuto = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.AUTO_TRAVEL_DISTANCE_SKIM), "distanceByTime", 1. / 1000.);
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
    }

    public void readSkimDistancesNMT() {
        IndexedDoubleMatrix2D distanceSkimNMT = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.NMT_TRAVEL_DISTANCE_SKIM), "distanceByDistance", 1. / 1000.);
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimNMT));
    }

    public void readOnlyTransitTravelTimes() {
        //todo has to be probably in silo
        SkimTravelTimes skimTravelTimes;
        skimTravelTimes = (SkimTravelTimes) dataSet.getTravelTimes();
        (skimTravelTimes).readSkim("bus", Resources.INSTANCE.getString(Properties.BUS_TRAVEL_TIME_SKIM), "mat1", 1.);
        (skimTravelTimes).readSkim("tramMetro", Resources.INSTANCE.getString(Properties.TRAM_METRO_TRAVEL_TIME_SKIM), "mat1", 1.);
        (skimTravelTimes).readSkim("train", Resources.INSTANCE.getString(Properties.TRAIN_TRAVEL_TIME_SKIM), "mat1", 1.);
    }

    private void readTravelTimeSkims() {
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("car", Resources.INSTANCE.getString(Properties.AUTO_PEAK_SKIM), "timeByTime", 1 / 60.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("bus", Resources.INSTANCE.getString(Properties.BUS_TRAVEL_TIME_SKIM), "mat1", 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("tramMetro", Resources.INSTANCE.getString(Properties.TRAM_METRO_TRAVEL_TIME_SKIM), "mat1", 1.);
        ((SkimTravelTimes) dataSet.getTravelTimes()).readSkim("train", Resources.INSTANCE.getString(Properties.TRAIN_TRAVEL_TIME_SKIM), "mat1", 1.);


        //read uam distance and divide by speed (property). Add access and egress time and update this matrix in travel times
        IndexedDoubleMatrix2D flyingDistance_km =
                AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.UAM_SKIM),
                        "uam_dist", 1.);
        IndexedDoubleMatrix2D accessTimeUam_min =
                AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.UAM_SKIM),
                        "access_time", 1. / 60);
        IndexedDoubleMatrix2D egressTimeUam_min =
                AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.UAM_SKIM),
                        "egress_time", 1. / 60);

        IndexedDoubleMatrix2D uamTravelTime = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());



        for (int originId : dataSet.getZones().keySet()) {
            for (int destinationId : dataSet.getZones().keySet()) {
                if (flyingDistance_km.getIndexed(originId, destinationId) < 10000.) {
                    double accessTime_min = accessTimeUam_min.getIndexed(originId, destinationId);
                    double egressTime_min = egressTimeUam_min.getIndexed(originId, destinationId);
                    double time = flyingDistance_km.getIndexed(originId, destinationId) / Resources.INSTANCE.getDouble(Properties.UAM_SPEED_KMH, 200.) * 60 *
                            Resources.INSTANCE.getDouble(Properties.UAM_DETOUR_FACTOR, 1.) +
                            accessTime_min +
                            egressTime_min +
                            Resources.INSTANCE.getDouble(Properties.UAM_TAKEOFF_MIN, 1.) +
                            Resources.INSTANCE.getDouble(Properties.UAM_LANDING_MIN, 1.);
                    uamTravelTime.setIndexed(originId, destinationId, time);
                } else {
                    uamTravelTime.setIndexed(originId, destinationId, 10000.);
                }

            }
        }
        ((SkimTravelTimes) dataSet.getTravelTimes()).updateSkimMatrix(uamTravelTime, "uam");

    }

    private void readAccessTimeSkims() {
        dataSet.getAccessAndEgressVariables().readSkim("transit", AccessAndEgressVariables.AccessVariable.ACCESS_T_MIN, Resources.INSTANCE.getString(Properties.PT_ACCESS_TIME_SKIM), "mat1", 1.);

        dataSet.getAccessAndEgressVariables().readSkim("uam",
                AccessAndEgressVariables.AccessVariable.ACCESS_T_MIN,
                Resources.INSTANCE.getString(Properties.UAM_SKIM), "access_time", 1. / 60);
        dataSet.getAccessAndEgressVariables().readSkim("uam",
                AccessAndEgressVariables.AccessVariable.EGRESS_T_MIN,
                Resources.INSTANCE.getString(Properties.UAM_SKIM), "egress_time", 1. / 60);


        //todo improve the original matrices so this is not required:
        //distances are read and converted to km. Erroneously the too high access distance was equal to 10000, it should be converted to 10,000,000
        IndexedDoubleMatrix2D access_dist =
                AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.UAM_SKIM), "access_dist", 1./1000);
        IndexedDoubleMatrix2D egress_dist =
                AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.UAM_SKIM), "egress_dist", 1./1000);

        for (int originId : dataSet.getZones().keySet()) {
            for (int destinationId : dataSet.getZones().keySet()) {
                double distance = access_dist.getIndexed(originId, destinationId);
                if (distance == 10.){
                    access_dist.setIndexed(originId, destinationId, 10000.);
                    egress_dist.setIndexed(originId, destinationId, 10000.);
                }
            }
        }
        //... and later set up within the access and egress data container
        dataSet.getAccessAndEgressVariables().setExternally(access_dist, "uam", AccessAndEgressVariables.AccessVariable.ACCESS_DIST_KM);
        dataSet.getAccessAndEgressVariables().setExternally(egress_dist, "uam", AccessAndEgressVariables.AccessVariable.EGRESS_DIST_KM);

        //todo the vertiport zones should not be a double matrix!! Consider to implement it.
        dataSet.getAccessAndEgressVariables().readSkim("uam",
                AccessAndEgressVariables.AccessVariable.ACCESS_VERTIPORT,
                Resources.INSTANCE.getString(Properties.UAM_SKIM), "access_vertiport", 1.);
        dataSet.getAccessAndEgressVariables().readSkim("uam",
                AccessAndEgressVariables.AccessVariable.EGRESS_VERTIPORT,
                Resources.INSTANCE.getString(Properties.UAM_SKIM), "egress_vertiport", 1.);
    }

    private void readTravelDistances() {
        IndexedDoubleMatrix2D distanceSkimAuto = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.AUTO_TRAVEL_DISTANCE_SKIM), "distanceByTime", 1. / 1000.);
        dataSet.setTravelDistancesAuto(new MatrixTravelDistances(distanceSkimAuto));
        IndexedDoubleMatrix2D distanceSkimNMT = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.NMT_TRAVEL_DISTANCE_SKIM), "distanceByDistance", 1. / 1000.);
        dataSet.setTravelDistancesNMT(new MatrixTravelDistances(distanceSkimNMT));
        //todo temporally we read cost and in the mode choice scripts we divided by 5 to use it as distance. In the future, read directly flying distance
        //read flying distance

        IndexedDoubleMatrix2D travelDistanceUAM = AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.INSTANCE.getString(Properties.UAM_SKIM), "uam_dist", 1);
        dataSet.setFlyingDistanceUAM(new MatrixTravelDistances(travelDistanceUAM));
    }
}
