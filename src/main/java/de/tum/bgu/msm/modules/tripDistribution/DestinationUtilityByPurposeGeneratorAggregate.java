package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class DestinationUtilityByPurposeGeneratorAggregate implements Callable<Tuple<Integer, IndexedDoubleMatrix2D>> {

    private final static Logger logger = Logger.getLogger(DestinationUtilityByPurposeGeneratorAggregate.class);

    private final DestinationUtilityCalculatorAggregate calculator;
    private final Purpose purpose;
    private final Map<Integer, MitoZone> zones;

    private final Map<Integer, MitoZone> origins;
    private final TravelDistances travelDistances;
    private final DataSet dataSet;
    private final EnumMap<Purpose, TravelDistances> logsum_persona;


    private final MitoAggregatePersona persona;


    DestinationUtilityByPurposeGeneratorAggregate(MitoZone origins, Purpose purpose, DataSet dataSet,
                                                  DestinationUtilityCalculatorFactoryAggregate factory,
                                                  double travelDistanceCalibrationK,
                                                  double impendanceCalibrationK, MitoAggregatePersona persona) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.travelDistances = dataSet.getTravelDistancesNMT();
        if (persona.getId() == 1){
            this.logsum_persona = dataSet.getLogsumByPurpose_EV();
        } else {
            this.logsum_persona = dataSet.getLogsumByPurpose_NoEV();
        }
        this.dataSet = dataSet;
        this.persona = persona;
        calculator = factory.createDestinationUtilityCalculator(purpose,travelDistanceCalibrationK, impendanceCalibrationK);
        this.origins = new HashMap<>();
        this.origins.put(1, origins);
    }

    @Override
    public Tuple<Integer, IndexedDoubleMatrix2D> call() {
        final IndexedDoubleMatrix2D probMatrix = new IndexedDoubleMatrix2D(origins.values(), dataSet.getZones().values());
        final IndexedDoubleMatrix2D probMatrixIndex = new IndexedDoubleMatrix2D(origins.values(), dataSet.getZones().values());
        long counter = 0;
        int i = 0;
        double expLog = 0;

        for (MitoZone destination : zones.values()) {

            //Using Logsum
            final double expUtility =  calculator.calculateExpUtility2(destination.getTripAttraction(purpose),
                    logsum_persona.get(purpose).getTravelDistance(origins.get(1).getId(), destination.getId()),
                    travelDistances.getTravelDistance(origins.get(1).getId(), destination.getId()));

            /*if (origins.get(1).getId() == 3351){
                logger.info( purpose.toString() + ". expUtility of zone 3351 to destination " + destination.getId() + ": " + expUtility + ". Calculated with attractor: "
                        +destination.getTripAttraction(purpose) + ", logsum: " + logsum_persona.get(purpose).getTravelDistance(origins.get(1).getId(), destination.getId()) +
                        ", and distance: " + travelDistances.getTravelDistance(origins.get(1).getId(), destination.getId()));
            }*/

            if (Double.isInfinite(expUtility) || Double.isNaN(expUtility)) {
                throw new RuntimeException(expUtility + " utility calculated! Please check calculation!" +
                        " Origin: " + origins.get(1).getId() + " | Destination: " + destination + " | Logsum: "
                        + logsum_persona.get(purpose).getTravelDistance(origins.get(1).getId(), destination.getId()) +
                        " | Purpose: " + purpose + " | attraction rate: " + destination.getTripAttraction(purpose));
            }
            //double probability = Math.exp(exp);
            int originIndex = probMatrix.getIdForInternalRowIndex(origins.get(1).getId());
            probMatrix.setIndexed(origins.get(1).getId(), destination.getId(), expUtility);
            probMatrixIndex.setIndexed(originIndex, destination.getId(), expUtility);
            expLog += expUtility;

            /*Path filePersona = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_destinationChoice_"+ purpose.toString() +"_utilities.csv");
            PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filePersona.toAbsolutePath().toString(), true);
            if (origins.get(1).getId() == 1 && destination.getId() == 1){
                pwh.println("origin,destination,expUtility,attractor,logsum,distance");
                pwh.println();
            }
            if (origins.get(1).getId() == 3351 ||origins.get(1).getId() == 1426||origins.get(1).getId() == 1248||origins.get(1).getId() == 1564) {
                pwh.print(origins.get(1).getId());
                pwh.print(",");
                pwh.print(destination.getId());
                pwh.print(",");
                pwh.print(expUtility);
                pwh.print(",");
                pwh.print(destination.getTripAttraction(purpose));
                pwh.print(",");
                pwh.print(logsum_persona.get(purpose).getTravelDistance(origins.get(1).getId(), destination.getId()));
                pwh.print(",");
                pwh.print(travelDistances.getTravelDistance(origins.get(1).getId(), destination.getId()));
                pwh.println();
            }
            pwh.close();*/

            //Using distance
/*
            final double utility =  calculator.calculateExpUtility(destination.getTripAttraction(purpose),
                    travelDistances.getTravelDistance(origin.getId(), destination.getId()));
            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                System.out.println("Destination zone: " + destination + "for Purpose: " + purpose +
                        "has trip attraction of: " + destination.getTripAttraction(purpose));
                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                        " Origin: " + origin + " | Destination: " + destination + " | Distance: "
                        + travelDistances.getTravelDistance(origin.getId(), destination.getId()) +
                        " | Purpose: " + purpose + " | attraction rate: " + destination.getTripAttraction(purpose));
            }
            utilityMatrix.setIndexed(origin.getId(), destination.getId(), utility);
*/
            i++;

            if (LongMath.isPowerOfTwo(counter)) {
                //logger.info(counter + " OD pairs done for purpose " + purpose);
            }
            counter++;
        }

/*        if (origins.get(1).getId() == 3351){
            logger.info( purpose.toString() + ". Sum of expLog of zone 3351 " + expLog);
        }*/
        for (MitoZone destination : dataSet.getZones().values()) {
            probMatrix.setIndexed(origins.get(1).getId(), destination.getId(),
                    probMatrix.getIndexed(origins.get(1).getId(), destination.getId()) / expLog);
        }
        /*
        Path filePersona = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_destinationChoice_"+ purpose.toString() +"_logsums.csv");
        Path filePersonaProb = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_destinationChoice_"+ purpose.toString() +"_probabilities.csv");
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filePersona.toAbsolutePath().toString(), true);
        PrintWriter pwh2 = MitoUtil.openFileForSequentialWriting(filePersonaProb.toAbsolutePath().toString(), true);
        if (origins.get(1).getId() == 1){
            pwh.print("zone");
            for (MitoZone destination : dataSet.getZones().values()) {
                pwh.print(",");
                pwh.print(destination.getId());
            }
            pwh.println();
            pwh2.println("origin,destination,prob");
        }
        if (origins.get(1).getId() == 3351 ||origins.get(1).getId() == 1426||origins.get(1).getId() == 1248||origins.get(1).getId() == 1564) {
            pwh.print(origins.get(1).getId());
            for (MitoZone destination : dataSet.getZones().values()) {
                pwh.print(",");
                pwh.print(logsum_persona.get(purpose).getTravelDistance(origins.get(1).getId(), destination.getId()));
                pwh2.print(origins.get(1).getId());
                pwh2.print(",");
                pwh2.print(destination.getId());
                pwh2.print(",");
                pwh2.println(probMatrix.getIndexed(origins.get(1).getId(), destination.getId()));
            }
            pwh.println();
        }
        pwh.close();
        pwh2.close();*/
        //logger.info("Utility matrix for purpose " + purpose + " done.");
        return new Tuple<>(origins.get(1).getId(), probMatrix);
    }
}
