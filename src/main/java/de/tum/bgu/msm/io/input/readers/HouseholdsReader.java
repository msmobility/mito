package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

import java.nio.file.Path;

/**
 * Created by Nico on 17.07.2017.
 */
public class HouseholdsReader extends AbstractCsvReader {

    private int posId = -1;
    private int posTaz = -1;
    private int posAutos = -1;
    private int posNursingHome = -1;

    private static final Logger logger = Logger.getLogger(HouseholdsReader.class);

    public HouseholdsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading household micro data from ascii file");
        Path filePath = Resources.instance.getHouseholdsFilePath();
        super.read(filePath, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("id", header);
        posTaz = MitoUtil.findPositionInArray("zone", header);
        posAutos = MitoUtil.findPositionInArray("autos", header);
        //posNursingHome = MitoUtil.findPositionInArray("nursingHome", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        int autos = Integer.parseInt(record[posAutos]);
        //int taz = Integer.parseInt(record[posTaz]);
        //int nursingHome = Integer.parseInt(record[posNursingHome]);
        MitoHousehold hh = new MitoHousehold(id, 0, autos);
//        hh.setNursingHome(nursingHome>=0);
//        if(nursingHome>=0){
//            hh.setHomeLocation(dataSet.getZones().get(taz).getGeometry().getCoordinate());
//            hh.setHomeZone(dataSet.getZones().get(taz));
//        }
        dataSet.addHousehold(hh);
    }
}
