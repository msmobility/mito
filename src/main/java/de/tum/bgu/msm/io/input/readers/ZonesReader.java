package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

public class ZonesReader extends CSVReader {

    private int sizeIndex;
    private int idIndex;

    public ZonesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.ZONES), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MitoUtil.findPositionInArray("ZoneId", header);
        sizeIndex = MitoUtil.findPositionInArray("ACRES", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        float size = Float.parseFloat(record[sizeIndex]);
        Zone zone = new Zone(zoneId, size);
        dataSet.addZone(zone);
    }
}
