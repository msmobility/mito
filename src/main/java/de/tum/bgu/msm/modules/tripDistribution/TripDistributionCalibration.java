package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.math.Quantiles;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoOccupationStatus;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Resources;
import org.apache.commons.math.exception.MathIllegalNumberException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class TripDistributionCalibration extends Module {

    private Map<Purpose, Double> observedAverageDistances = new HashMap<>();
    private Map<Purpose, Double> simulatedAverageDistances = new HashMap<>();
    private Map<Purpose, Double> travelDistanceParameters;
    private Map<Purpose, Double> impendanceParameters;
    private PrintWriter pw = null;
    private int iteration;

    public TripDistributionCalibration(DataSet dataSet, List<Purpose> purposes,
                                       Map<Purpose, Double> travelDistanceParameters,
                                       Map<Purpose, Double> impendanceParameters) {

        super(dataSet, purposes);
        iteration = 0;
        observedAverageDistances.put(Purpose.HBE , 7.29);
        observedAverageDistances.put(Purpose.HBW , 18.1);
        observedAverageDistances.put(Purpose.HBO , 10.4);
        observedAverageDistances.put(Purpose.HBS , 5.07);
        observedAverageDistances.put(Purpose.NHBO , 11.7);
        observedAverageDistances.put(Purpose.NHBW , 16.1);

        String purposesString = "";
        for (Purpose purpose : purposes) {
            purposesString += "_" + purpose.toString();
        }

        this.impendanceParameters = impendanceParameters;
        this.travelDistanceParameters = travelDistanceParameters;

        String path = Resources.instance.getBaseDirectory().toString() + "/scenOutput/";

        try {
            pw = new PrintWriter(path + "dc_calibration" + purposesString + ".csv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.println("iteration,purpose,observed,simulated,factorDistance,factorImpedance");

    }


    public void update(int iteration){

        for (Purpose purpose : purposes) {

            int count = 0;
            double sum = 0;
            List<MitoTrip> tripsThisPurpose =
                    dataSet.getTrips().values().stream().filter(t -> t.getTripPurpose().equals(purpose)).collect(Collectors.toList());

            if(Purpose.getMandatoryPurposes().contains(purpose)){
                tripsThisPurpose.removeIf(trip -> trip.getPerson().getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER) ||
                        trip.getPerson().getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT));
            }


            for (MitoTrip trip : tripsThisPurpose) {
                count++;
                sum += dataSet.getTravelDistancesAuto().
                        getTravelDistance(trip.getTripOrigin().getZoneId(), trip.getTripDestination().getZoneId());
            }

            double avg = sum / count;

            //double[] distances = tripsThisPurpose.stream().mapToDouble(this::getDistanceOfThisTrip).toArray();


            //double avg = Quantiles.median().compute(distances);


            simulatedAverageDistances.put(purpose, avg);
            double ratio = avg / observedAverageDistances.get(purpose); //greater than 1 if the model simulates too long trips
            // - in this case, the parameter of distance needs to be larger (more negative)
            ratio = Math.max(ratio, 0.5);
            ratio = Math.min(ratio, 2);


            travelDistanceParameters.put(purpose, travelDistanceParameters.get(purpose) * ratio);
            pw.println(iteration + "," +
                    purpose + "," +
                    observedAverageDistances.get(purpose) + "," +
                    simulatedAverageDistances.get(purpose) + "," +
                    travelDistanceParameters.get(purpose) + "," +
                    impendanceParameters.get(purpose));

        }

    }

    private double getDistanceOfThisTrip(MitoTrip trip) {
        return dataSet.getTravelDistancesAuto().
                getTravelDistance(trip.getTripOrigin().getZoneId(), trip.getTripDestination().getZoneId());
    }


    @Override
    public void run() {
    }

    public Map<Purpose, Double> getTravelDistanceParameters() {
        return travelDistanceParameters;
    }

    public Map<Purpose, Double> getImpendanceParameters() {
        return impendanceParameters;
    }

    public void close(){
        pw.close();
    }
}
