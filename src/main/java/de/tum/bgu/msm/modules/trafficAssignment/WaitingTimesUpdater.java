package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.waitingTimes.StationDependentWaitingTimes;
import de.tum.bgu.msm.data.waitingTimes.WaitingTimes;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class WaitingTimesUpdater {

    private final static Logger logger = Logger.getLogger(WaitingTimes.class);
    private final DataSet dataSet;
    private final Map<String, Map<Integer, List<Double>>> waitingTimesByUAMStationAndTime;
    private final Map<String, Map<Integer, Double>> averageWaitingTimesByUAMStationAndTime_min;
    private static final int INTERVAL_S = 60 * 15;
    private static final int NUMBER_OF_INTERVALS = 24 * 60 * 60 / INTERVAL_S;
    private final Map<Integer, String> zonesToStationMap;
    private final double MINIMUM_WAITING_TIME_S = Resources.INSTANCE.getDouble("uam.boardingTime", 13) * 60;

    public WaitingTimesUpdater(DataSet dataSet) {
        this.dataSet = dataSet;
        waitingTimesByUAMStationAndTime = new LinkedHashMap<>();
        averageWaitingTimesByUAMStationAndTime_min = new LinkedHashMap<>();
        zonesToStationMap = new LinkedHashMap<>();
        new StationToZoneConversionReader(dataSet).read();
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
        }
    }

    private void aggregateMap() {
        for (String station : zonesToStationMap.values()) {
            for (int interval : waitingTimesByUAMStationAndTime.get(station).keySet()) {
                if (!waitingTimesByUAMStationAndTime.get(station).get(interval).isEmpty()) {
                    double averageWaitingTime = waitingTimesByUAMStationAndTime.get(station).get(interval).
                            stream().mapToDouble(a -> a).average().getAsDouble();
                    averageWaitingTime = Math.max(averageWaitingTime, MINIMUM_WAITING_TIME_S);

                    averageWaitingTimesByUAMStationAndTime_min.get(station).put(interval, averageWaitingTime/60);
                } else {
                    averageWaitingTimesByUAMStationAndTime_min.get(station).put(interval, MINIMUM_WAITING_TIME_S/60);
                }
            }
        }
    }

    private void printOutTimes(String outputFileName) {
        try {
            PrintWriter pw = new PrintWriter(new File(outputFileName));
            pw.println("station,zone,time_s,waitingTime_s");
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
        StationDependentWaitingTimes stationDependentWaitingTimes =
                new StationDependentWaitingTimes(dataSet.getAccessAndEgressVariables(),
                        averageWaitingTimesByUAMStationAndTime_min, zonesToStationMap);

        dataSet.setWaitingTimes(stationDependentWaitingTimes);
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
            t0Index = MitoUtil.findPositionInArray("arrivalAtStationTime", header);
            t1Index = MitoUtil.findPositionInArray("takeOffTime", header);
            t2Index = MitoUtil.findPositionInArray("landingTime", header);
            t3Index = MitoUtil.findPositionInArray("departureFromStationTime", header);
            uamTagIndex = MitoUtil.findPositionInArray("uamTrip", header);
        }

        @Override
        protected void processRecord(String[] record) {
            if (Boolean.parseBoolean(record[uamTagIndex])) {
                String origStation = record[origStationIndex];
                double arrivalAtStationTime_s = Double.parseDouble(record[t0Index]);
                double waitingTimeAtOrigStation_s = Double.parseDouble(record[t1Index]) - arrivalAtStationTime_s;
                double landingTime = Double.parseDouble(record[t2Index]);
                double waitingTimeAtDestStation_s = Double.parseDouble(record[t3Index]) - landingTime;
                int interval = 0;
                while (interval < arrivalAtStationTime_s) {
                    interval += INTERVAL_S;
                }
                double totalProcessingTime = waitingTimeAtOrigStation_s + waitingTimeAtDestStation_s;
                waitingTimesByUAMStationAndTime.get(origStation).get(interval).add(totalProcessingTime);
            }
        }

        @Override
        public void read() {
            super.read(fileName, ",");
        }
    }

    /**
     * This class is used to read a conversion between station names (uam_extension) and station zone (mito). Unfortunately
     * it is based on the assumption of at most 1 vertiport per zone.
     */
    private class StationToZoneConversionReader extends AbstractCsvReader {
        int zoneIndex;
        int stationNameIndex;

        public StationToZoneConversionReader(DataSet dataSet) {
            super(dataSet);
        }

        @Override
        protected void processHeader(String[] header) {
            stationNameIndex = MitoUtil.findPositionInArray("originStationId", header);
            zoneIndex = MitoUtil.findPositionInArray("Zone", header);

        }

        @Override
        protected void processRecord(String[] record) {
            String station = record[stationNameIndex];
            int zoneId = Integer.parseInt(record[zoneIndex]);

            if (zonesToStationMap.containsKey(zoneId)) {
                throw new RuntimeException("This version is not compatible with having more that one vertiport per zone");
            } else {
                zonesToStationMap.put(zoneId, station);
            }

        }

        @Override
        public void read() {
            super.read(Resources.INSTANCE.getString(Properties.UAM_VERTIPORT_LIST), ",");
        }
    }
}
