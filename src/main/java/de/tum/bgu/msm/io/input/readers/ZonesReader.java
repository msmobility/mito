package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.AreaTypes;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.locationtech.jts.geom.Geometry;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

/**
 * @author Nico
 */
public class ZonesReader extends AbstractCsvReader {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ZonesReader.class);
    private int idIndex;
    private int areaTypeIndex;

    public ZonesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.instance.getZonesInputFile().toAbsolutePath(), ",");
        mapFeaturesToZones(dataSet);
    }

    private static void mapFeaturesToZones(DataSet dataSet) {
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(Resources.instance.getZoneShapesInputFile().toString())) {
            int zoneId = Integer.parseInt(feature.getAttribute(Resources.instance.getString(Properties.ZONE_SHAPEFILE_ID_FIELD)).toString());
            MitoZone zone = dataSet.getZones().get(zoneId);
            if (zone != null){
                zone.setGeometry((Geometry) feature.getDefaultGeometry());
                final Object ags = feature.getAttribute("AGS");
                if(ags != null) {
                    zone.setAGS(Integer.parseInt(ags.toString()));
                }
            }else{
                logger.warn("zoneId " + zoneId + " doesn't exist in mito zone system");
            }
        }
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MitoUtil.findPositionInArray("Zone", header);
        areaTypeIndex = MitoUtil.findPositionInArray("BBSR_type", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        int region = Integer.parseInt(record[areaTypeIndex]);
        AreaTypes.SGType areaType = AreaTypes.SGType.valueOf(region);
        MitoZone zone = new MitoZone(zoneId, areaType);
        dataSet.addZone(zone);
    }
}
