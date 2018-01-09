package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

public final class BorderDampersReader extends CSVReader {

    private static final Logger logger = Logger.getLogger(BorderDampersReader.class);

    private int zoneIndex;
    private int damperIndex;

    public BorderDampersReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read( Resources.INSTANCE.getString(Properties.REDUCTION_NEAR_BORDER_DAMPERS), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        zoneIndex = MitoUtil.findPositionInArray("Zone", header);
        damperIndex = MitoUtil.findPositionInArray("damper", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zone = Integer.parseInt(record[zoneIndex]);
        float damper = Float.parseFloat(record[damperIndex]);
        if (dataSet.getZones().containsKey(zone)) {
            dataSet.getZones().get(zone).setReductionAtBorderDamper(damper);
        } else {
            logger.warn("Damper of " + damper + " refers to non-existing zone " + zone + ". Ignoring it.");
        }
    }
}
