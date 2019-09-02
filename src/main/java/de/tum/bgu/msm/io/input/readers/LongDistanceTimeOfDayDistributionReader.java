package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.HashMap;
import java.util.Map;

public class LongDistanceTimeOfDayDistributionReader extends AbstractCsvReader {

    private int posHour;
    private int posProbability;
    private Map<Integer,Double> departureTimeProbabilityByHour;

    public LongDistanceTimeOfDayDistributionReader(DataSet dataSet) {
        super(dataSet);
        departureTimeProbabilityByHour = new HashMap<>();
    }

    @Override
    protected void processHeader(String[] header) {
        posHour = MitoUtil.findPositionInArray("hour", header);
        posProbability = MitoUtil.findPositionInArray("probability", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int hour = Integer.parseInt(record[posHour]);
        double probability = Double.parseDouble(record[posProbability]);
        departureTimeProbabilityByHour.put(hour, probability);
    }

    @Override
    public void read() {
        super.read(Resources.instance.getExternalDepartureTimeFilePath(),",");
    }


    public Map<Integer, Double> getDepartureTimeDistribution() {
        return departureTimeProbabilityByHour;
    }
}
