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

    public ModeChoiceByPurpose(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoice"));
        calculator = new ModeChoiceJSCalculator(reader);
        probabilities = Matrices.doubleMatrix2DSparse(dataSet.getTrips().values(), Mode.valuesAsList());
    }

    @Override
    public Pair<Purpose, DoubleMatrix2D> call() throws Exception {
        final Map<String,Double> travelTimeByMode = new HashMap<>();
        int countTripsSkipped = 0;
        for (MitoHousehold household : dataSet.getHouseholds().values()){
            for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                if(trip.getTripOrigin() == null || trip.getTripDestination() == null) {
                    countTripsSkipped++;
                    continue;
                }
                travelTimeByMode.put("autoD", dataSet.getTravelTimes("car").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour()));
                travelTimeByMode.put("autoP", dataSet.getTravelTimes("car").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour()));
                travelTimeByMode.put("bus", dataSet.getTravelTimes("bus").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour()));
                travelTimeByMode.put("tramMetro", dataSet.getTravelTimes("tramMetro").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour()));
                travelTimeByMode.put("train", dataSet.getTravelTimes("train").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour()));
                final double travelDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId()) / 1000.;
                final double travelDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId()) / 1000.;
                final double[] probabilitiesArray = calculator.calculateProbabilities(household, trip.getPerson(), trip, travelTimeByMode, travelDistanceAuto, travelDistanceNMT);
                probabilities.viewRow(trip.getId()).assign(probabilitiesArray);
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
}
