package de.tum.bgu.msm.io.input.readers;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.HashMap;

public class CalibrationDataReader extends AbstractCsvReader {

    private int regionIndex;
    private int purposeIndex;
    private int modeIndex;
    private int shareIndex;
    private int kIndex;


    public CalibrationDataReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    protected void processHeader(String[] header) {
        regionIndex = MitoUtil.findPositionInArray("calibrationRegion", header);
        purposeIndex = MitoUtil.findPositionInArray("purpose", header);
        modeIndex = MitoUtil.findPositionInArray("mode", header);
        shareIndex = MitoUtil.findPositionInArray("share", header);
        kIndex = MitoUtil.findPositionInArray("factor", header);
    }

    @Override
    protected void processRecord(String[] record) {
        String region = record[regionIndex];
        Purpose purpose = Purpose.valueOf(record[purposeIndex]);
        Mode mode = Mode.valueOf(record[modeIndex]);

        double share = Double.parseDouble(record[shareIndex]);
        double factor = Double.parseDouble(record[kIndex]);

        dataSet.getModeChoiceCalibrationData().getObservedModalShare().putIfAbsent(region, new HashMap<>());
        dataSet.getModeChoiceCalibrationData().getObservedModalShare().get(region).putIfAbsent(purpose, new HashMap<>());
        dataSet.getModeChoiceCalibrationData().getObservedModalShare().get(region).get(purpose).put(mode, share);

        dataSet.getModeChoiceCalibrationData().getCalibrationFactors().putIfAbsent(region, new HashMap<>());
        dataSet.getModeChoiceCalibrationData().getCalibrationFactors().get(region).putIfAbsent(purpose, new HashMap<>());
        dataSet.getModeChoiceCalibrationData().getCalibrationFactors().get(region).get(purpose).put(mode, factor);

        //return null;
    }

    @Override
    public void read() {
        super.read(Resources.instance.getCalibrationFactorsPath(), ",");
    }
}
