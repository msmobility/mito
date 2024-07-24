package uk.cam.mrc.phm.io;

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

import java.util.HashMap;

/**
 * @author Nico
 */
public class PoiWeightsReader extends AbstractCsvReader {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PoiWeightsReader.class);
    private int idIndex;
    private HashMap<String, Integer> poiIndex = new HashMap<>();

    public PoiWeightsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.instance.getZonesInputFile().toAbsolutePath(), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MitoUtil.findPositionInArray("oaID", header);
        poiIndex.put("EYA",MitoUtil.findPositionInArray("EYA", header));
        poiIndex.put("EDU",MitoUtil.findPositionInArray("EDU", header));
        poiIndex.put("FR",MitoUtil.findPositionInArray("FR", header));
        poiIndex.put("RSPF",MitoUtil.findPositionInArray("RSPF", header));
        poiIndex.put("SCL",MitoUtil.findPositionInArray("SCL", header));
        poiIndex.put("CHR",MitoUtil.findPositionInArray("CHR", header));
        poiIndex.put("EE",MitoUtil.findPositionInArray("EE", header));
        poiIndex.put("FIN",MitoUtil.findPositionInArray("FIN", header));
        poiIndex.put("PHC",MitoUtil.findPositionInArray("PHC", header));
        poiIndex.put("POS",MitoUtil.findPositionInArray("POS", header));
        poiIndex.put("SER",MitoUtil.findPositionInArray("SER", header));
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        for(String poiType : poiIndex.keySet()){
            float weights = Float.parseFloat(record[poiIndex.get(poiType)]);
            dataSet.getZones().get(zoneId).getPoiWeightsByType().put(poiType,weights);
        }

    }
}
