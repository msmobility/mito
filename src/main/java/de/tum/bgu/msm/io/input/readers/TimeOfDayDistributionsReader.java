package de.tum.bgu.msm.io.input.readers;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

public class TimeOfDayDistributionsReader extends CSVReader {

    private final DoubleMatrix2D arrivalMinuteCumProbByPurpose = new DenseDoubleMatrix2D(24*60+1, Purpose.values().length);
    private final DoubleMatrix2D durationMinuteCumProvByPurpose = new DenseDoubleMatrix2D(24*60+1, Purpose.values().length);

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
        arrivalMinuteCumProbByPurpose.setQuick(minute, Purpose.HBE.ordinal(), Double.parseDouble(record[hbe_arrival_index]));
        arrivalMinuteCumProbByPurpose.setQuick(minute, Purpose.HBO.ordinal(), Double.parseDouble(record[hbo_arrival_index]));
        arrivalMinuteCumProbByPurpose.setQuick(minute, Purpose.HBS.ordinal(), Double.parseDouble(record[hbs_arrival_index]));
        arrivalMinuteCumProbByPurpose.setQuick(minute, Purpose.HBW.ordinal(), Double.parseDouble(record[hbw_arrival_index]));
        arrivalMinuteCumProbByPurpose.setQuick(minute, Purpose.NHBO.ordinal(), Double.parseDouble(record[nhbo_arrival_index]));
        arrivalMinuteCumProbByPurpose.setQuick(minute, Purpose.NHBW.ordinal(), Double.parseDouble(record[nhbw_arrival_index]));

        durationMinuteCumProvByPurpose.setQuick(minute, Purpose.HBE.ordinal(), Double.parseDouble(record[hbe_duration_index]));
        durationMinuteCumProvByPurpose.setQuick(minute, Purpose.HBO.ordinal(), Double.parseDouble(record[hbo_duration_index]));
        durationMinuteCumProvByPurpose.setQuick(minute, Purpose.HBS.ordinal(), Double.parseDouble(record[hbs_duration_index]));
        durationMinuteCumProvByPurpose.setQuick(minute, Purpose.HBW.ordinal(), Double.parseDouble(record[hbw_duration_index]));

    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.TIME_OF_DAY_DISTRIBUTIONS), ",");
    }

    public DoubleMatrix2D getArrivalMinuteCumProbByPurpose() {
        return arrivalMinuteCumProbByPurpose;
    }

    public DoubleMatrix2D getDurationMinuteCumProvByPurpose() {
        return durationMinuteCumProvByPurpose;
    }


}
