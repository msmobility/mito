package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaitingTimesUpdater {


    private final DataSet dataSet;
    private final Map<String, Map<Integer, List<Double>>> waitingTimesByUAMStationAndTime;
    private final Map<String, Integer> stations;
    private static final int INTERVAL_S = 60 * 15;
    private static final int NUMBER_OF_INTERVALS = 24 * 60 * 60 / INTERVAL_S;

    public WaitingTimesUpdater(DataSet dataSet) {
        this.dataSet = dataSet;
        waitingTimesByUAMStationAndTime = new HashMap<>();
        stations = new HashMap<>();

    }


    public void run() {
        initializeMap();
        readWaitingTimes();
        aggregateMap();
        assignToMatrix();

    }

    private void aggregateMap() {
        for (String station : stations.keySet()){
            for (int interval = 0; interval < NUMBER_OF_INTERVALS; interval ++){
                double averageWaitingTime = waitingTimesByUAMStationAndTime.get(station).get(interval).
                        stream().mapToDouble(a -> a).average().getAsDouble();
            }
        }
    }

    private void initializeMap() {
        for (String station : stations.keySet()){
            waitingTimesByUAMStationAndTime.put(station, new HashMap<>());
            for (int interval = 0; interval < NUMBER_OF_INTERVALS; interval ++){
                waitingTimesByUAMStationAndTime.get(station).put(interval * INTERVAL_S, new ArrayList<>());
            }


        }
    }

    private void assignToMatrix() {
    }


    private void readWaitingTimes() {
        String fileName = "";
        new WaitingTImeReader(dataSet, fileName).read();
    }


    private class WaitingTImeReader extends AbstractCsvReader {

        private final String fileName;
        private int origStationIndex;
        private int t0Index;
        private int t1Index;
        private int t2Index;
        private int t3Index;

        protected WaitingTImeReader(DataSet dataSet, String fileName) {
            super(dataSet);
            this.fileName = fileName;
        }

        @Override
        protected void processHeader(String[] header) {
            origStationIndex = MitoUtil.findPositionInArray("originStat", header);
            t0Index = MitoUtil.findPositionInArray("arrivalAtStationTime", header);
            t1Index = MitoUtil.findPositionInArray("takeOffTime", header);
            t2Index = MitoUtil.findPositionInArray("landingTime", header);
            t3Index = MitoUtil.findPositionInArray("departureFromStationTime", header);
        }

        @Override
        protected void processRecord(String[] record) {
            String origStation = record[origStationIndex];

            double arrivalAtStationTime_s = Double.parseDouble(record[t0Index]);
            double waitingTimeAtOrigStation_s = Double.parseDouble(record[t1Index]) - arrivalAtStationTime_s;
            int interval = 0;
            while (interval * INTERVAL_S < arrivalAtStationTime_s){
                interval++;
            }
            waitingTimesByUAMStationAndTime.get(origStation).get(interval).add(waitingTimeAtOrigStation_s);
        }

        @Override
        public void read() {
            super.read(fileName, ",");

        }
    }


}
