package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

public class RegionsReader extends CSVReader {

    private static final Logger logger = Logger.getLogger(RegionsReader.class);
    private int zoneIndex;
    private int regionIndex;

    public RegionsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.REGIONS), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        zoneIndex = MitoUtil.findPositionInArray("Zone", header);
        regionIndex = MitoUtil.findPositionInArray("Region", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zone = Integer.parseInt(record[zoneIndex]);
        int region = Integer.parseInt(record[regionIndex]);
        if (dataSet.getZones().containsKey(zone)) {
            dataSet.getZones().get(zone).setRegion(region);
        } else {
            logger.warn("Region " + region + " referring to non-existing zone " + zone + ". Ignoring it.");
        }
    }
}
