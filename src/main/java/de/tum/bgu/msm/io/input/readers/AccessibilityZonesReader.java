package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.AreaTypes;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nico
 */
public class AccessibilityZonesReader extends AbstractCsvReader {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AccessibilityZonesReader.class);
    private int idIndex;
    private int HBWindex;
    private int HBEindex;
    private int HBSindex;
    private int HBOindex;
    private int NHBWindex;
    private int NHBOindex;

    public AccessibilityZonesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.ACCESSIBILITY_BY_PURPOSE), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MitoUtil.findPositionInArray("Zone", header);
        HBWindex = MitoUtil.findPositionInArray("HBW", header);
        HBEindex = MitoUtil.findPositionInArray("HBE", header);
        HBSindex = MitoUtil.findPositionInArray("HBS", header);
        HBOindex = MitoUtil.findPositionInArray("HBO", header);
        NHBWindex = MitoUtil.findPositionInArray("NHBW", header);
        NHBOindex = MitoUtil.findPositionInArray("NHBO", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        Map<Purpose, Float> accessibilityByPurpose = new HashMap<>();
        accessibilityByPurpose.put(Purpose.HBW, Float.parseFloat(record[HBWindex]));
        accessibilityByPurpose.put(Purpose.HBE, Float.parseFloat(record[HBEindex]));
        accessibilityByPurpose.put(Purpose.HBS, Float.parseFloat(record[HBSindex]));
        accessibilityByPurpose.put(Purpose.HBO, Float.parseFloat(record[HBOindex]));
        accessibilityByPurpose.put(Purpose.NHBW, Float.parseFloat(record[NHBWindex]));
        accessibilityByPurpose.put(Purpose.NHBO, Float.parseFloat(record[NHBOindex]));
        if (dataSet.getZones().containsKey(zoneId)) {
            dataSet.getZones().get(zoneId).setAccessibility(accessibilityByPurpose);
        } else {
            logger.warn("Accessibility of non-existing zone " + zoneId + ". Ignoring it.");
        }


    }
}
