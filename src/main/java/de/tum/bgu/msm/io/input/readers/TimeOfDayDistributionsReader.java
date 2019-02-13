package de.tum.bgu.msm.io.input.readers;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.EnumMap;

public class TimeOfDayDistributionsReader extends AbstractCsvReader {

    private final EnumMap<Purpose, DoubleMatrix1D> arrivalTimeCumProbByPurpose = new EnumMap<>(Purpose.class);
    private final EnumMap<Purpose, DoubleMatrix1D> durationCumProbByPurpose = new EnumMap<>(Purpose.class);
    //private final DoubleMatrix2D arrivalMinuteCumProbByPurpose = new DenseDoubleMatrix2D(24*60+1, Purpose.values().length);
    //private final DoubleMatrix2D durationMinuteCumProvByPurpose = new DenseDoubleMatrix2D(24*60+1, Purpose.values().length);

    private int minuteIndex;
    private int hbe_arrival_index;
    private int hbe_duration_index;
    private int hbo_arrival_index;
    private int hbo_duration_index;
    private int hbs_arrival_index;
    private int hbs_duration_index;
    private int hbw_arrival_index;
    private int hbw_duration_index;
    private int nhbo_arrival_index;
    private int nhbw_arrival_index;



    public TimeOfDayDistributionsReader(DataSet dataSet) {

        super(dataSet);
        for (Purpose purpose : Purpose.values()) {
            arrivalTimeCumProbByPurpose.put(purpose, new DenseDoubleMatrix1D(24 * 60 + 1));
            durationCumProbByPurpose.put(purpose, new DenseDoubleMatrix1D(24 * 60 + 1));
        }
    }

    @Override
    protected void processHeader(String[] header) {
        minuteIndex = MitoUtil.findPositionInArray("minute", header);
        hbe_arrival_index = MitoUtil.findPositionInArray("arrival_hbe", header);
        hbe_duration_index = MitoUtil.findPositionInArray("duration_hbe", header);
        hbo_arrival_index = MitoUtil.findPositionInArray("arrival_hbo", header);
        hbo_duration_index = MitoUtil.findPositionInArray("duration_hbo", header);
        hbs_arrival_index = MitoUtil.findPositionInArray("arrival_hbs", header);
        hbs_duration_index = MitoUtil.findPositionInArray("duration_hbs", header);
        hbw_arrival_index = MitoUtil.findPositionInArray("arrival_hbw", header);
        hbw_duration_index = MitoUtil.findPositionInArray("duration_hbw", header);
        nhbo_arrival_index = MitoUtil.findPositionInArray("arrival_nhbo", header);
        nhbw_arrival_index = MitoUtil.findPositionInArray("arrival_nhbw", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int minute = Integer.parseInt(record[minuteIndex]);
        arrivalTimeCumProbByPurpose.get(Purpose.HBE).setQuick(minute, Double.parseDouble(record[hbe_arrival_index]));
        arrivalTimeCumProbByPurpose.get(Purpose.HBO).setQuick(minute, Double.parseDouble(record[hbo_arrival_index]));
        arrivalTimeCumProbByPurpose.get(Purpose.HBS).setQuick(minute, Double.parseDouble(record[hbs_arrival_index]));
        arrivalTimeCumProbByPurpose.get(Purpose.HBW).setQuick(minute, Double.parseDouble(record[hbw_arrival_index]));
        arrivalTimeCumProbByPurpose.get(Purpose.NHBO).setQuick(minute, Double.parseDouble(record[nhbo_arrival_index]));
        arrivalTimeCumProbByPurpose.get(Purpose.NHBW).setQuick(minute, Double.parseDouble(record[nhbw_arrival_index]));

        durationCumProbByPurpose.get(Purpose.HBE).setQuick(minute,Double.parseDouble(record[hbe_duration_index]));
        durationCumProbByPurpose.get(Purpose.HBO).setQuick(minute,Double.parseDouble(record[hbo_duration_index]));
        durationCumProbByPurpose.get(Purpose.HBS).setQuick(minute,Double.parseDouble(record[hbs_duration_index]));
        durationCumProbByPurpose.get(Purpose.HBW).setQuick(minute,Double.parseDouble(record[hbw_duration_index]));

    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.TIME_OF_DAY_DISTRIBUTIONS), ",");
    }

    public EnumMap<Purpose, DoubleMatrix1D> getArrivalMinuteCumProbByPurpose() {
        return arrivalTimeCumProbByPurpose;
    }

    public EnumMap<Purpose, DoubleMatrix1D> getDurationMinuteCumProbByPurpose() {
        return durationCumProbByPurpose;
    }


}
