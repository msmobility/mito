package de.tum.bgu.msm.modules.modeChoice;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.util.matrices.Matrices;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ModeChoiceByPurpose implements Callable<Pair<Purpose, DoubleMatrix2D>> {

    private static final Logger LOGGER = Logger.getLogger(ModeChoiceByPurpose.class);

    private final DataSet dataSet;
    private final Purpose purpose;
    private final ModeChoiceJSCalculator calculator;
    private final DoubleMatrix2D probabilities;
    private int countTripsSkipped;
    private final Map<String, Double> travelTimeByMode = new HashMap<>();

    public ModeChoiceByPurpose(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoice"));
        calculator = new ModeChoiceJSCalculator(reader);
        probabilities = Matrices.doubleMatrix2DSparse(dataSet.getTrips().values(), Mode.valuesAsList());
    }

    @Override
    public Pair<Purpose, DoubleMatrix2D> call() throws Exception {
        countTripsSkipped = 0;
        for (MitoHousehold household : dataSet.getHouseholds().values()){
            for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                double[] probabilitiesArray = calculateTripProbabilities(household, trip);
                if(probabilitiesArray != null) {
                    probabilities.viewRow(trip.getId()).assign(probabilitiesArray);
                }
            }
        }
        LOGGER.info(countTripsSkipped + " trips skipped for " + purpose);
//        try {
//            MatrixVectorWriter writer = new MatrixVectorWriter(new FileWriter("output/modeChoiceMatrix_"+purpose));
//            for(int i=0; i<probabilities.columns();i++){
//                writer.printArray(probabilities.viewColumn(i).toArray());
//                if(i %1000 == 0){
//                    writer.flush();
//                }
//            }
//            writer.flush();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return new Pair(purpose, probabilities);
    }

    double[] calculateTripProbabilities(MitoHousehold household, MitoTrip trip) {
        if(trip.getTripOrigin() == null || trip.getTripDestination() == null) {
            countTripsSkipped++;
            return null;
        }
        travelTimeByMode.put("autoD", dataSet.getTravelTimes("car").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour()));
        travelTimeByMode.put("autoP",dataSet.getTravelTimes("car").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour()));
        double busTime =  dataSet.getTravelTimes("bus").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour());
        double trainTime = dataSet.getTravelTimes("train").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour());
        double tramMetroTime = dataSet.getTravelTimes("tramMetro").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour());
        if(busTime < 0) {
            busTime = 1000;
        }
        if(trainTime < 0) {
            trainTime = 1000;
        }
        if(tramMetroTime < 0) {
            tramMetroTime = 1000;
        }
        travelTimeByMode.put("bus", busTime);
        travelTimeByMode.put("tramMetro", trainTime);
        travelTimeByMode.put("train", tramMetroTime);

        final double travelDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId());
        final double travelDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId());
        return calculator.calculateProbabilities(household, trip.getPerson(), trip, travelTimeByMode, travelDistanceAuto, travelDistanceNMT);
    }
}
