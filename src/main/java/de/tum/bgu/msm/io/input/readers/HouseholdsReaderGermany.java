package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

import java.nio.file.Path;

/**
 * Created by Nico on 17.07.2017.
 */
public class HouseholdsReaderGermany extends AbstractCsvReader {

    private int posId = -1;
    private int posTaz = -1;
    private int posHHSize = -1;
    private int posAutos = -1;
    private int posCoordX = -1;
    private int posCoordY = -1;

    private int counter = 0;

    private static final Logger logger = Logger.getLogger(HouseholdsReaderGermany.class);

    public HouseholdsReaderGermany(DataSet dataSet) {
        super(dataSet);
    }

    public int getPosTaz () {
        return this.posTaz;
    }

    @Override
    public void read() {
        logger.info("  Reading household micro data from ascii file");
        Path filePath = Resources.instance.getHouseholdsFilePath();
        super.read(filePath, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("hhid", header);
        posTaz = MitoUtil.findPositionInArray("zone", header);
        posHHSize = MitoUtil.findPositionInArray("hhSize", header);
        posAutos = MitoUtil.findPositionInArray("autos", header);
        posCoordX = MitoUtil.findPositionInArray("coordX", header);
        posCoordY = MitoUtil.findPositionInArray("coordY", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        int autos = Integer.parseInt(record[posAutos]);
        MitoHousehold hh = new MitoHousehold(id, 0, autos);
        /*if (counter == 19){
            counter =0;
        } else {
            counter++;
        }
        if (counter == 3){*/
            dataSet.addHousehold(hh);
        /*}*/



        //int hhId = Integer.parseInt(record[posHHId]);


        //vacant dwellings
        if (id > 0) {
            //MitoHousehold hh = dataSet.getHouseholds().get(id);
            if (hh == null) {
                logger.warn(String.format("Household %d does not exist in mito.", id));
                return;
            }
            int taz = Integer.parseInt(record[posTaz]);
            MitoZone zone = dataSet.getZones().get(taz);
            if(zone == null) {
                logger.warn(String.format("Household %d is supposed to live in zone %d but this zone does not exist.", id, taz));
            }

            //Coordinate homeLocation = new Coordinate().getRandomCoord();
            //Coordinate homeLocation = zone.getRandomCoord(MitoUtil.getRandomObject());

            Coordinate homeLocation = new Coordinate(
            Double.parseDouble(record[posCoordX]), Double.parseDouble(record[posCoordY]));
            hh.setHomeLocation(homeLocation);
            hh.setHomeZone(zone);
            zone.addHousehold();
    }
    }
}
