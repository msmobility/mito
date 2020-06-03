package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.nio.file.Paths;

public class CalibrationRegionMapReader extends AbstractCsvReader {

    private int zoneIndex;
    private int regionIndex;

    public CalibrationRegionMapReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    protected void processHeader(String[] header) {
        zoneIndex = MitoUtil.findPositionInArray("zone", header);
        regionIndex = MitoUtil.findPositionInArray("calibrationRegion", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zone = Integer.parseInt(record[zoneIndex]);
        String region = record[regionIndex];

        dataSet.getModeChoiceCalibrationData().getZoneToRegionMap().put(zone, region);
    }

    @Override
    public void read() {
       super.read(Resources.instance.getCalibrationRegionsPath(), ",");
    }
}
