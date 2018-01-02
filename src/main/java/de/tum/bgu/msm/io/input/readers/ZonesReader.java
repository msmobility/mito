package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.AreaType;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

public class ZonesReader extends CSVReader {

    private int sizeIndex;
    private int idIndex;
    private int areaTypeIndex;

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
        areaTypeIndex = MitoUtil.findPositionInArray("BBSR", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        float size = Float.parseFloat(record[sizeIndex]);
        int region = Integer.parseInt(record[areaTypeIndex]);
        AreaType areaType = AreaType.valueOf(region);
        MitoZone zone = new MitoZone(zoneId, size, areaType);
        dataSet.addZone(zone);
    }
}
