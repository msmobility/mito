package de.tum.bgu.msm.io;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Day;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.EnumMap;

public class DayProbabilitiesReader extends AbstractCsvReader {

    private final EnumMap<Purpose, EnumMap<Day, Double>> dayProbabilitiesByPurpose = new EnumMap<>(Purpose.class);

    private int dayIndex;
    private int hbw_index;
    private int hbe_index;
    private int hbs_index;
    private int hbr_index;
    private int hbo_index;
    private int rrt_index;
    private int nhbo_index;
    private int nhbw_index;

    public DayProbabilitiesReader(DataSet dataSet) {
        super(dataSet);
        for (Purpose purpose : Purpose.values()) {
            dayProbabilitiesByPurpose.put(purpose, new EnumMap<>(Day.class));
        }
    }

    @Override
    protected void processHeader(String[] header) {
        dayIndex = MitoUtil.findPositionInArray("day", header);
        hbw_index = MitoUtil.findPositionInArray("HBW", header);
        hbe_index = MitoUtil.findPositionInArray("HBE", header);
        hbs_index = MitoUtil.findPositionInArray("HBS", header);
        hbr_index = MitoUtil.findPositionInArray("HBR", header);
        hbo_index = MitoUtil.findPositionInArray("HBO", header);
        rrt_index = MitoUtil.findPositionInArray("RRT", header);
        nhbw_index = MitoUtil.findPositionInArray("NHBW", header);
        nhbo_index = MitoUtil.findPositionInArray("NHBO", header);
    }

    @Override
    protected void processRecord(String[] record) {
        Day day = Day.valueOf(record[dayIndex]);
        dayProbabilitiesByPurpose.get(Purpose.HBW).put(day, Double.parseDouble(record[hbw_index]));
        dayProbabilitiesByPurpose.get(Purpose.HBE).put(day, Double.parseDouble(record[hbe_index]));
        dayProbabilitiesByPurpose.get(Purpose.HBS).put(day, Double.parseDouble(record[hbs_index]));
        dayProbabilitiesByPurpose.get(Purpose.HBR).put(day, Double.parseDouble(record[hbr_index]));
        dayProbabilitiesByPurpose.get(Purpose.HBO).put(day, Double.parseDouble(record[hbo_index]));
        dayProbabilitiesByPurpose.get(Purpose.RRT).put(day, Double.parseDouble(record[rrt_index]));
        dayProbabilitiesByPurpose.get(Purpose.NHBW).put(day, Double.parseDouble(record[nhbw_index]));
        dayProbabilitiesByPurpose.get(Purpose.NHBO).put(day, Double.parseDouble(record[nhbo_index]));
    }

    public void read() {
        super.read(Resources.instance.getDayProbabilitiesFilePath(), ",");
    }

    public EnumMap<Purpose, EnumMap<Day, Double>> readCoefficients(){
        read();
        return dayProbabilitiesByPurpose;
    };
}
