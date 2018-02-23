package de.tum.bgu.msm.modules.modeChoice;

import cern.colt.matrix.io.MatrixVectorWriter;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunctionExecutor;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class ModeChoice extends Module {

    private final static Logger logger = Logger.getLogger(ModeChoice.class);

    private final Map<Purpose, DoubleMatrix2D> modeChoiceProbabilitiesByPurpose = new EnumMap<>(Purpose.class);
    private DoubleMatrix2D result;


    public ModeChoice(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        logger.info(" Calculating mode choice probabilities for each trip. Modes considered - 1. Auto driver, 2. Auto passenger, 3. Bicycle, 4. Bus, 5. Train, 6. Tram or Metro, 7. Walk ");
        calculateProbabilitiesByPurpose();
        aggregateProbabilitiesToOneMatrix();
        chooseTripMode(result);
        printModeShares();
    }

    private void calculateProbabilitiesByPurpose() {
        ConcurrentFunctionExecutor executor = new ConcurrentFunctionExecutor();
        for (Purpose purpose : Purpose.values()){
            executor.addFunction(new ModeChoiceByPurpose(dataSet, purpose, modeChoiceProbabilitiesByPurpose));
        }
        executor.execute();
    }

    private void aggregateProbabilitiesToOneMatrix() {

        // output bicycle probabilities and check if there are any zeroes

        result =  modeChoiceProbabilitiesByPurpose.values().stream().reduce(
                //open stream to aggregate/add matrices pair-wise (reduce)
                (matrix1, matrix2) ->
                        //add next matrix in the stream (matrix2) to current aggregated result (matrix1)
                        matrix1.assign(matrix2, (v1, v2) -> v1+v2)).get();
//        try {
//            MatrixVectorWriter writer = new MatrixVectorWriter(new FileWriter("output/ProbabilitiesMatrix"));
//                for(int i=0; i<result.columns();i++){
//                    writer.printArray(result.viewColumn(i).toArray());
//                    if(i %1000 == 0){
//                    writer.flush();
//                    }
//                }
//            writer.flush();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void chooseTripMode(DoubleMatrix2D result) {
        dataSet.getTrips().entrySet().parallelStream().forEach(i -> {
            final double[] probabilities = result.viewRow(i.getKey()).toArray();
            //logger.info("Mode choice probabilities: " + result.viewRow(i.getKey()).toString());
            i.getValue().setTripMode(Mode.valueOf(MitoUtil.select(probabilities)));
        });
    }

    private void printModeShares(){
        float tripsAutoDriver = 0;
        float tripsAutoPassenger = 0;
        float tripsBicycle = 0;
        float tripsBus = 0;
        float tripsTrain = 0;
        float tripsTramOrMetro = 0;
        float tripsWalk = 0;
        float tripsWithNoMode = 0;

        for(MitoTrip trip : dataSet.getTrips().values()){
            switch (trip.getTripMode()){
                case autoDriver:
                    tripsAutoDriver++;
                    break;
                case autoPassenger:
                    tripsAutoPassenger++;
                    break;
                case bicycle:
                    tripsBicycle++;
                    break;
                case bus:
                    tripsBus++;
                    break;
                case train:
                    tripsTrain++;
                    break;
                case tramOrMetro:
                    tripsTramOrMetro++;
                    break;
                case walk:
                    tripsWalk++;
                    break;
                default:
                    tripsWithNoMode++;
                    //logger.info("Mode was not assigned to trip "+trip.getId());
            }
        }
        float totalTrips = tripsAutoDriver + tripsAutoPassenger + tripsBicycle + tripsBus + tripsTrain + tripsTramOrMetro + tripsWalk;
             logger.info("Mode shares:\nAuto Driver " + tripsAutoDriver*100/totalTrips +
                "%\nAuto Passenger " + tripsAutoPassenger*100/totalTrips +
                "%\nBicycle " + tripsBicycle*100/totalTrips +
                "%\nBus " + tripsBus*100/totalTrips +
                "%\nTrain " + tripsTrain*100/totalTrips +
                "%\nTram and Metro " + tripsTramOrMetro*100/totalTrips +
                "%\nWalk " + tripsWalk*100/totalTrips +
                "%\n Mode not assigned to " + tripsWithNoMode + " trips\n Mode assigned to " + totalTrips + "trips" +
                "\ntotal trips = "+ totalTrips +
                "\nautoD trips = "+ tripsAutoDriver +
                "\nautoP trips = "+ tripsAutoPassenger +
                "\nbicycle trips = "+ tripsBicycle +
                "\nbus trips = "+ tripsBus +
                "\ntrain trips = "+ tripsTrain +
                "\ntramMetro trips = "+ tripsTramOrMetro +
                "\nwalk trips = "+ tripsWalk);
    }
}
