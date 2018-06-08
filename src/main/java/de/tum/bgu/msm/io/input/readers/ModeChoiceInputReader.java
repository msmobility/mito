package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.AreaTypes;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

/**
 * @author Hema
 */
public class ModeChoiceInputReader extends CSVReader {

    private int railDistIndex;
    private int zoneIndex;
    private int areaTypeNHBOIndex;

    public ModeChoiceInputReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    protected void processHeader(String[] header) {
        zoneIndex = MitoUtil.findPositionInArray("zoneID", header);
        railDistIndex = MitoUtil.findPositionInArray("distToRailStop", header);
        areaTypeNHBOIndex = MitoUtil.findPositionInArray("areaTypeNHBO", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneID = Integer.parseInt(record[zoneIndex]);
        float distToRailStop = Float.parseFloat(record[railDistIndex]);
        AreaTypes.RType areaTypeNHBO = AreaTypes.RType.valueOf(Integer.parseInt(record[areaTypeNHBOIndex]));
        MitoZone zone = dataSet.getZones().get(zoneID);
        zone.setDistanceToNearestRailStop(distToRailStop);

        zone.setAreaTypeR(areaTypeNHBO);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.AREA_TYPES_AND_RAIL_DISTANCE),",");
    }
}
