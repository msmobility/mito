package de.tum.bgu.msm.io.input.readers;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.modules.externalFlows.ExternalFlowZone;
import de.tum.bgu.msm.modules.externalFlows.ExternalFlowZoneType;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ExternalZonesReader extends AbstractCsvReader {

    private Map<Integer, ExternalFlowZone> zones;

    private Path fileNameZones = Resources.instance.getExternalZonesListFilePath();
    private String shapeFileZones = Resources.instance.getString(Properties.EXTERNAL_ZONES_SHAPEFILE);
    private String idFieldName = Resources.instance.getString(Properties.EXTERNAL_ZONES_SHAPE_ID_FIELD);

    private int positionId;
    private int positionName;
    private int positionType;
    private int positionX;
    private int positionY;

    private Map<Integer, SimpleFeature> features;

    public ExternalZonesReader(DataSet dataSet) {
        super(dataSet);
        zones = new HashMap<>();
        features = new HashMap<>();
        //read the shapefile
        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(shapeFileZones)){
            int zoneId = Integer.parseInt(feature.getAttribute(idFieldName).toString());
            features.put(zoneId, feature);
        }
    }

    @Override
    protected void processHeader(String[] header) {
        positionId = MitoUtil.findPositionInArray("NO", header);
        positionName = MitoUtil.findPositionInArray("NAME", header);
        positionType = MitoUtil.findPositionInArray("TYPENO", header);
        positionX = MitoUtil.findPositionInArray("POINT_X", header);
        positionY = MitoUtil.findPositionInArray("POINT_Y", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[positionId]);
        String name = record[positionName];
        ExternalFlowZoneType type = ExternalFlowZoneType.getExternalFlowZoneTypeFromInt(Integer.parseInt(record[positionType]));
        Coord coordinates = new Coord(Float.parseFloat(record[positionX]),Float.parseFloat(record[positionY]));
        SimpleFeature feature;
        if (!ExternalFlowZoneType.BORDER.equals(type)) {
            feature = features.get(id);
        } else {
            feature = null;
        }
        zones.put(id, new ExternalFlowZone(id, coordinates, type, feature));
    }

    @Override
    public void read() {
        super.read(fileNameZones,",");
    }

    public Map<Integer, ExternalFlowZone> getZones(){
        return zones;
    }
}
