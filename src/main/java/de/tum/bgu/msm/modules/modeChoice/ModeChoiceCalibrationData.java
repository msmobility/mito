package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ModeChoiceCalibrationData {

    private Map<String, Map<Purpose, Map<Mode, Double>>> observedModalShare;
    private Map<String, Map<Purpose, Map<Mode, Integer>>> simulatedTripsByRegionPurposeAndMode;
    private Map<String, Map<Purpose, Map<Mode, Double>>> calibrationFactors;
    private Map<Integer, String> zoneToRegionMap;

    private PrintWriter pw;

    private static Logger logger = Logger.getLogger(ModeChoiceCalibrationData.class);

    public ModeChoiceCalibrationData() {
        this.observedModalShare = new HashMap<>();
        this.calibrationFactors = new HashMap<>();
        this.simulatedTripsByRegionPurposeAndMode = new HashMap<>();
        zoneToRegionMap = new HashMap<>();

        try {
            pw = new PrintWriter(new File("mode_choice_calibration.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.println("iteration,region,purpose,mode,observed_share,sim_share,k,trips");

    }


    public double[] getCalibrationFactorsAsArray(Purpose tripPurpose, Location tripOrigin) {

        double[] factors = new double[Mode.values().length];
        for (Mode mode : Mode.values()) {
            factors[mode.getId()] = calibrationFactors.get(zoneToRegionMap.get(tripOrigin.getZoneId())).get(tripPurpose).getOrDefault(mode, 0.);
        }
        return factors;
    }

    public Map<String, Map<Purpose, Map<Mode, Double>>> getObservedModalShare() {
        return observedModalShare;
    }

    public Map<String, Map<Purpose, Map<Mode, Double>>> getCalibrationFactors() {
        return calibrationFactors;
    }

    public Map<Integer, String> getZoneToRegionMap() {
        return zoneToRegionMap;
    }

    public void updateCalibrationCoefficients(DataSet dataSet, int iteration) {

        simulatedTripsByRegionPurposeAndMode.clear();

        for (MitoTrip trip : dataSet.getTrips().values()) {
            if (trip.getTripMode() != null) {
                String region = zoneToRegionMap.get(trip.getTripOrigin().getZoneId());
                Purpose purpose = trip.getTripPurpose();
                Mode mode = trip.getTripMode();

                simulatedTripsByRegionPurposeAndMode.putIfAbsent(region, new HashMap<>());
                simulatedTripsByRegionPurposeAndMode.get(region).putIfAbsent(purpose, new HashMap<>());
                simulatedTripsByRegionPurposeAndMode.get(region).get(purpose).putIfAbsent(mode, 0);
                int newValue = simulatedTripsByRegionPurposeAndMode.get(region).get(purpose).get(mode) + 1;
                simulatedTripsByRegionPurposeAndMode.get(region).get(purpose).put(mode, newValue);
            }
        }


        for (String region : observedModalShare.keySet()) {
            for (Purpose purpose : Purpose.values()) {
                for (Mode mode : Mode.values()) {
                    double observedShare = observedModalShare.get(region).get(purpose).getOrDefault(mode, 0.);
                    double tripAtRegionAndPurpose = simulatedTripsByRegionPurposeAndMode.get(region).get(purpose).values().stream().mapToInt(Integer::intValue).sum();
                    double simulatedShare;
                    if (tripAtRegionAndPurpose != 0) {
                        simulatedShare = simulatedTripsByRegionPurposeAndMode.get(region).get(purpose).getOrDefault(mode, 0) / tripAtRegionAndPurpose;
                    } else {
                        simulatedShare = 0.;
                    }

                    double difference = observedShare - simulatedShare;
                    double existingFactor = calibrationFactors.get(region).get(purpose).getOrDefault(mode, 0.);
                    double newFactor = existingFactor + difference;
                    calibrationFactors.get(region).get(purpose).put(mode, newFactor);

                    double tripCount = tripAtRegionAndPurpose * simulatedShare;
                    pw.println(iteration + "," + region + "," + purpose + "," + mode + "," +
                            observedShare + "," + simulatedShare + "," + newFactor + "," + tripCount);
                }
            }
        }


    }

    public void close() {
        pw.close();
        logger.info("Finished mode choice calibration");
    }


}
