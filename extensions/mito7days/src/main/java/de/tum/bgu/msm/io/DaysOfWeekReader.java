package de.tum.bgu.msm.io;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.*;

public class DaysOfWeekReader extends AbstractCsvReader {

    private final EnumMap<Purpose, ArrayList<LinkedHashMap<String,Integer>>> probabilities = new EnumMap<>(Purpose.class);

    private final List<Purpose> purposes;

    private int purposeIndex;
    private int tripsIndex;

    private int daysIndex;
    private int freqIndex;

    public DaysOfWeekReader(DataSet dataSet) {
        super(dataSet);
        purposes = Purpose.getListedPurposes(Resources.instance.getString(Properties.TRIP_PURPOSES));
        for (Purpose purpose : purposes) {
            probabilities.put(purpose, new ArrayList<>());
        }
    }

    @Override
    protected void processHeader(String[] header) {
        purposeIndex = MitoUtil.findPositionInArray("purpose", header);
        tripsIndex = MitoUtil.findPositionInArray("trips", header);
        daysIndex = MitoUtil.findPositionInArray("days", header);
        freqIndex = MitoUtil.findPositionInArray("frequency", header);
    }

    @Override
    protected void processRecord(String[] record) {

        Purpose purpose = Purpose.valueOf(record[purposeIndex]);
        if(purposes.contains(purpose)) {
            ArrayList<LinkedHashMap<String,Integer>> probabilitiesForPurpose = probabilities.get(purpose);

            // If trips exceeds current number, add them
            int trips = Integer.parseInt(record[tripsIndex]);
            for(int i = probabilitiesForPurpose.size() ; i <= trips ; i++) {
                probabilitiesForPurpose.add(i,new LinkedHashMap<>());
            }

            // Add to map, throw runtime exception if it's a duplicate
            if (probabilitiesForPurpose.get(trips).put(record[daysIndex], Integer.parseInt(record[freqIndex])) != null) {
                throw new RuntimeException("Duplicate row in days of week file!");
            }
        }
    }

    public void read() {
        super.read(Resources.instance.getDayProbabilitiesFilePath(), ",");
    }

    public void check() {
        for (Purpose purpose : purposes) {
            ArrayList<LinkedHashMap<String,Integer>> probs = probabilities.get(purpose);
            if(probs.size() < 8) {
                throw new RuntimeException("Need candidate sequences for at least seven trips of each purpose! Purpose " + purpose +
                        " only supports " + (probs.size() - 1) + " trips per week!");
            }
            for(int i = 1 ; i < probs.size() ;i++) {
                if(probs.get(i).size() < 2) {
                    throw new RuntimeException("Need at least 2 candidates sequences for each trip count. Purpose " + purpose +
                            " only has " + probs.get(i).size() + " candidates for " + i + " trips per week!");
                }
            }
        }
    }

    public EnumMap<Purpose, ArrayList<LinkedHashMap<String,Integer>>> readCoefficients(){
        read();
        check();
        return probabilities;
    }
}
