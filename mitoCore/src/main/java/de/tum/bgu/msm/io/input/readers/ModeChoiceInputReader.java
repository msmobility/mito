package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.AreaTypes;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

/**
 * @author Hema
 */
public class ModeChoiceInputReader extends AbstractCsvReader {

    private final static Logger logger = Logger.getLogger(ModeChoiceInputReader.class);

    private int railDistIndex;
    private int zoneIndex;
    private int areaTypeNHBOIndex;

    private int zoneNotFoundCounter = 0;

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
        if(zone != null) {
            zone.setDistanceToNearestRailStop(distToRailStop);
            zone.setAreaTypeR(areaTypeNHBO);
        } else {
            zoneNotFoundCounter++;
        }
    }

    @Override
    public void read() {
        super.read(Resources.instance.getAreaTypesAndRailDistancesFilePath(),",");
        logger.warn(zoneNotFoundCounter + " zones were not present but described in the mode choice input.");
    }
}
