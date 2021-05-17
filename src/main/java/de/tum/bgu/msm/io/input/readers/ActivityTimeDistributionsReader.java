package de.tum.bgu.msm.io.input.readers;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.plans.ActivityPurpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ActivityTimeDistributionsReader extends AbstractCsvReader {

    private final EnumMap<ActivityPurpose, DoubleMatrix1D> startingTimeDistribution = new EnumMap<>(ActivityPurpose.class);
    private final EnumMap<ActivityPurpose, DoubleMatrix1D> activityDurationDistribution = new EnumMap<>(ActivityPurpose.class);


    private Map<String, Integer> indexes = new HashMap<>();



    public ActivityTimeDistributionsReader(DataSet dataSet) {
        super(dataSet);
        for (ActivityPurpose activityPurpose : ActivityPurpose.values()) {
            startingTimeDistribution.put(activityPurpose, new DenseDoubleMatrix1D(24 * 60 + 1));
            activityDurationDistribution.put(activityPurpose, new DenseDoubleMatrix1D(24 * 60 + 1));
        }
    }

    @Override
    protected void processHeader(String[] header) {
        putIndex(header, Col.minute);

        putIndex(header, Col.workStart);
        putIndex(header, Col.workDuration);

        putIndex(header, Col.educationStart);
        putIndex(header, Col.educationDuration);

        putIndex(header, Col.otherStart);
        putIndex(header, Col.otherDuration);

        putIndex(header, Col.shoppingStart);
        putIndex(header, Col.shoppingDuration);

        putIndex(header, Col.shoppingStart);
        putIndex(header, Col.shoppingDuration);

        putIndex(header, Col.recreationStart);
        putIndex(header, Col.recreationDuration);

        //add other purposes?
    }

    private void putIndex(String[] header, String col) {
        indexes.put(col, MitoUtil.findPositionInArray(col, header));
    }

    @Override
    protected void processRecord(String[] record) {
        int minute = Integer.parseInt(record[indexes.get(Col.minute)]);
        startingTimeDistribution.get(ActivityPurpose.W).setQuick(minute, Double.parseDouble(record[indexes.get(Col.workStart)]));
        activityDurationDistribution.get(ActivityPurpose.W).setQuick(minute, Double.parseDouble(record[indexes.get(Col.workDuration)]));

        startingTimeDistribution.get(ActivityPurpose.E).setQuick(minute, Double.parseDouble(record[indexes.get(Col.educationStart)]));
        activityDurationDistribution.get(ActivityPurpose.E).setQuick(minute, Double.parseDouble(record[indexes.get(Col.educationDuration)]));

        startingTimeDistribution.get(ActivityPurpose.O).setQuick(minute, Double.parseDouble(record[indexes.get(Col.otherStart)]));
        activityDurationDistribution.get(ActivityPurpose.O).setQuick(minute, Double.parseDouble(record[indexes.get(Col.otherDuration)]));

        startingTimeDistribution.get(ActivityPurpose.S).setQuick(minute, Double.parseDouble(record[indexes.get(Col.shoppingStart)]));
        activityDurationDistribution.get(ActivityPurpose.S).setQuick(minute, Double.parseDouble(record[indexes.get(Col.shoppingDuration)]));

        startingTimeDistribution.get(ActivityPurpose.A).setQuick(minute, Double.parseDouble(record[indexes.get(Col.accopmStart)]));
        activityDurationDistribution.get(ActivityPurpose.A).setQuick(minute, Double.parseDouble(record[indexes.get(Col.accompDuration)]));

        startingTimeDistribution.get(ActivityPurpose.R).setQuick(minute, Double.parseDouble(record[indexes.get(Col.recreationStart)]));
        activityDurationDistribution.get(ActivityPurpose.R).setQuick(minute, Double.parseDouble(record[indexes.get(Col.recreationDuration)]));


    }

    @Override
    public void read() {
        super.read(Resources.instance.getTimeOfDayDistributionsFilePath(), ",");
        dataSet.setActivityStartDistributions(startingTimeDistribution);
        dataSet.setActivityDurationDistributions(activityDurationDistribution);
    }

    private static class Col {
        final static String minute = "minute";

        final static String workStart = "arrival_hbw";
        final static String workDuration = "duration_hbw";

        final static String educationStart = "arrival_hbe";
        final static String educationDuration = "duration_hbe";

        final static String otherStart = "arrival_hbo";
        final static String otherDuration = "duration_hbo";

        final static String shoppingStart = "arrival_hbs";
        final static String shoppingDuration = "duration_hbs";

        final static String recreationStart = "arrival_hbo";
        final static String recreationDuration = "duration_hbo";

        final static String accopmStart = "arrival_hbo";
        final static String accompDuration = "duration_hbo";
    }


}
