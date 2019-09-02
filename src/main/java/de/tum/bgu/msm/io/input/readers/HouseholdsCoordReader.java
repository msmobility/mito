package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

/**
 * Created by Qin on 02.07.2018.
 */
public class HouseholdsCoordReader extends AbstractCsvReader {

    private int posHHId = -1;
    private int posCoordX = -1;
    private int posCoordY = -1;

    private static final Logger logger = Logger.getLogger(HouseholdsCoordReader.class);

    public HouseholdsCoordReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("Reading household microlocation coordinate from dwelling file");
        String fileName = Resources.instance.getString(Properties.DWELLINGS);
        super.read(fileName, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        posHHId = MitoUtil.findPositionInArray("hhID", header);
        posCoordX = MitoUtil.findPositionInArray("coordX", header);
        posCoordY = MitoUtil.findPositionInArray("coordY", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int hhId = Integer.parseInt(record[posHHId]);

        //vacant dwellings
        if (hhId > 0) {
            MitoHousehold hh = dataSet.getHouseholds().get(hhId);
            if (hh == null) {
                logger.warn(String.format("Household %d does not exist in mito.", hhId));
                return;
            }
            Coordinate homeLocation = new Coordinate(
            		Double.parseDouble(record[posCoordX]), Double.parseDouble(record[posCoordY]));
            hh.setHomeLocation(homeLocation);
        }
    }
}
