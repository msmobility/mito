package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.AreaTypes;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.CSVReader;
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
        mapFeaturesToZones(dataSet);
    }

    public static void mapFeaturesToZones(DataSet dataSet) {
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(Resources.INSTANCE.getString(Properties.ZONE_SHAPEFILE))) {
            int zoneId = Integer.parseInt(feature.getAttribute(Resources.INSTANCE.getString(Properties.ZONE_SHAPEFILE_ID_FIELD)).toString());
            if (dataSet.getZones().containsKey(zoneId)){
                dataSet.getZones().get(zoneId).setShapeFeature(feature);
            } else {
                System.out.println("Zone not found in shapefile: " + zoneId + ". No feature will be added.");
            }
            }
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
        AreaTypes.SGType areaType = AreaTypes.SGType.valueOf(region);
        MitoZone zone = new MitoZone(zoneId, size, areaType);
        dataSet.addZone(zone);
    }
}
