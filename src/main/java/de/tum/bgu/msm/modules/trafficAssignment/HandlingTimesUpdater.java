package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.waitingTimes.StationDependentTotalHandlingTimes;
import de.tum.bgu.msm.data.waitingTimes.TotalHandlingTimes;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Updates the waiting time object after collecting data from the uam_extension. Works with the uam_extention only, as it needs
 * the uam_demand.csv files of the last MATSim iteration.
 */
public class HandlingTimesUpdater {

    private final static Logger logger = Logger.getLogger(TotalHandlingTimes.class);
    private static final double PROCESSING_TIME_FOR_INCOMPLETE_TRIPS_S = 10000.;
    private final DataSet dataSet;
    private final Map<String, Map<Integer, List<Double>>> waitingTimesByUAMStationAndTime;
    private final Map<String, Map<Integer, Double>> averageWaitingTimesByUAMStationAndTime_min;
    private static final int INTERVAL_S = 60 * 15;
    private static final int NUMBER_OF_INTERVALS = 30 * 60 * 60 / INTERVAL_S;
    private final Map<Integer, String> zonesToStationMap;
    private final double MINIMUM_WAITING_TIME_S = Resources.INSTANCE.getDouble(Properties.UAM_BOARDINGTIME, 13) * 60;

    public HandlingTimesUpdater(DataSet dataSet) {
        this.dataSet = dataSet;
        waitingTimesByUAMStationAndTime = new LinkedHashMap<>();
        averageWaitingTimesByUAMStationAndTime_min = new LinkedHashMap<>();
        Map<UAMStation, MitoZone> stationToZoneMap = dataSet.getStationToZoneMap();
        zonesToStationMap = convertMap(stationToZoneMap);
    }

    /**
     * reverts the station to zone into a zone to station map
     *
     * @param stationToZoneMap
     * @return
     */
    private static Map<Integer, String> convertMap(Map<UAMStation, MitoZone> stationToZoneMap) {
        Map<Integer, String> zonesToStationMap = new LinkedHashMap<>();

        for (UAMStation uamStation : stationToZoneMap.keySet()) {
            zonesToStationMap.put(stationToZoneMap.get(uamStation).getId(), uamStation.getId().toString());
        }

        return zonesToStationMap;

    }


    public void run(String inputFileName, String outputFileName) {
        initializeMap();
        readWaitingTimes(inputFileName);
        aggregateMap();
        printOutTimes(outputFileName);
        updateWaitingTimes();

    }


    private void initializeMap() {
        for (String station : zonesToStationMap.values()) {
            waitingTimesByUAMStationAndTime.put(station, new TreeMap<>());
            averageWaitingTimesByUAMStationAndTime_min.put(station, new TreeMap<>());
            for (int intervalIndex = 0; intervalIndex < NUMBER_OF_INTERVALS; intervalIndex++) {
                waitingTimesByUAMStationAndTime.get(station).put(intervalIndex * INTERVAL_S, new ArrayList<>());
                averageWaitingTimesByUAMStationAndTime_min.get(station).put(intervalIndex * INTERVAL_S, 0.);
            }
            waitingTimesByUAMStationAndTime.get(station).put(NUMBER_OF_INTERVALS * INTERVAL_S, new ArrayList<>());
            averageWaitingTimesByUAMStationAndTime_min.get(station).put(NUMBER_OF_INTERVALS * INTERVAL_S, 0.);
        }
    }

    private void aggregateMap() {
        for (String station : zonesToStationMap.values()) {
            for (int interval : waitingTimesByUAMStationAndTime.get(station).keySet()) {
                if (!waitingTimesByUAMStationAndTime.get(station).get(interval).isEmpty()) {
                    double averageWaitingTime = waitingTimesByUAMStationAndTime.get(station).get(interval).
                            stream().mapToDouble(a -> a).average().getAsDouble();
                    averageWaitingTime = Math.max(averageWaitingTime, MINIMUM_WAITING_TIME_S);

                    averageWaitingTimesByUAMStationAndTime_min.get(station).put(interval, averageWaitingTime / 60);
                } else {
                    averageWaitingTimesByUAMStationAndTime_min.get(station).put(interval, MINIMUM_WAITING_TIME_S / 60);
                }
            }
        }
    }

    private void printOutTimes(String outputFileName) {
        try {
            PrintWriter pw = new PrintWriter(new File(outputFileName));
            pw.println("station,zone,time_s,waitingTime_min");
            for (int zone : zonesToStationMap.keySet()) {
                String station = zonesToStationMap.get(zone);
                for (int interval : averageWaitingTimesByUAMStationAndTime_min.get(station).keySet()) {
                    pw.print(station);
                    pw.print(",");
                    pw.print(zone);
                    pw.print(",");
                    pw.print(interval);
                    pw.print(",");
                    pw.print(averageWaitingTimesByUAMStationAndTime_min.get(station).get(interval));
                    pw.println();
                }
            }
            pw.close();
            logger.info("Print out average waiting times at vertiports");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateWaitingTimes() {
        StationDependentTotalHandlingTimes stationDependentWaitingTimes =
                new StationDependentTotalHandlingTimes(dataSet.getAccessAndEgressVariables(),
                        averageWaitingTimesByUAMStationAndTime_min, zonesToStationMap);

        dataSet.setTotalHandlingTimes(stationDependentWaitingTimes);
        logger.info("Watiting times updated after running the MATSim uam_extension");

    }


    private void readWaitingTimes(String fileName) {
        new WaitingTimeReader(dataSet, fileName).read();

    }


    /**
     * this inner class reads the uam demand file stored by the uam extension
     */
    private class WaitingTimeReader extends AbstractCsvReader {

        private final String fileName;
        private int origStationIndex;
        private int tStartIndex;
        private int t0Index;
        private int t1Index;
        private int t2Index;
        private int t3Index;
        private int uamTagIndex;

        protected WaitingTimeReader(DataSet dataSet, String fileName) {
            super(dataSet);
            this.fileName = fileName;
        }

        @Override
        protected void processHeader(String[] header) {
            origStationIndex = MitoUtil.findPositionInArray("originStationId", header);
            tStartIndex = MitoUtil.findPositionInArray("startTime", header);
            t0Index = MitoUtil.findPositionInArray("arrivalAtStationTime", header);
            t1Index = MitoUtil.findPositionInArray("takeOffTime", header);
            t2Index = MitoUtil.findPositionInArray("landingTime", header);
            t3Index = MitoUtil.findPositionInArray("departureFromStationTime", header);
            uamTagIndex = MitoUtil.findPositionInArray("uamTrip", header);
        }

        @Override
        protected void processRecord(String[] record) {
            boolean isUamTrip = Boolean.parseBoolean(record[uamTagIndex]);
            double startTime = Double.parseDouble(record[tStartIndex]);
            String origStation = record[origStationIndex];
            int interval = 0;
            while (interval < startTime) {
                interval += INTERVAL_S;
            }

            if (isUamTrip) {
                double arrivalAtStationTime_s = Double.parseDouble(record[t0Index]);
                double waitingTimeAtOrigStation_s = Double.parseDouble(record[t1Index]) - arrivalAtStationTime_s;
                double landingTime = Double.parseDouble(record[t2Index]);
                double waitingTimeAtDestStation_s = Double.parseDouble(record[t3Index]) - landingTime;
                double totalProcessingTime = waitingTimeAtOrigStation_s + waitingTimeAtDestStation_s;
                waitingTimesByUAMStationAndTime.get(origStation).get(interval).add(totalProcessingTime);
            } else if (!origStation.equals("null")) {
                try {
                    waitingTimesByUAMStationAndTime.get(origStation).get(interval).add(PROCESSING_TIME_FOR_INCOMPLETE_TRIPS_S);
                } catch (NullPointerException e){
                    logger.info("Something went wrong!");
                }
            }
        }

        @Override
        public void read() {
            super.read(fileName, ",");
        }
    }

}
