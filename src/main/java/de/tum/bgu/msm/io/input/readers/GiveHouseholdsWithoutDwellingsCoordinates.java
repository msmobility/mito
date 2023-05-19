package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordUtils;

import java.nio.file.Path;

public class GiveHouseholdsWithoutDwellingsCoordinates extends AbstractCsvReader {

    private int posId = -1;
    private int posTaz = -1;

    private static final Logger logger = Logger.getLogger(GiveHouseholdsWithoutDwellingsCoordinates.class);

    public GiveHouseholdsWithoutDwellingsCoordinates(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Re-Reading household micro data from ascii file to give households without dwellings coordinates");
        System.out.println("setting zone and coordinates for households without dwellings"); 
        Path filePath = Resources.instance.getHouseholdsFilePath();
        super.read(filePath, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("id", header);
        posTaz = MitoUtil.findPositionInArray("zone", header);

    }

    @Override
    protected void processRecord(String[] record) {


        int hhId = Integer.parseInt(record[posId]);

        MitoHousehold hh = dataSet.getHouseholds().get(hhId);

        if (hh.getHomeZone() == null) {
            logger.warn(String.format("Household %d has no home zone.", hhId));
            int taz = Integer.parseInt(record[posTaz]);
            MitoZone zone = dataSet.getZones().get(taz);
            if (zone == null) {
                logger.warn(String.format("Household %d is supposed to live in zone %d but this zone does not exist.", hhId, taz));
            }
            hh.setHomeZone(zone);
            zone.addHousehold();

            Coord coordinate = CoordUtils.createCoord(zone.getRandomCoord(MitoUtil.getRandomObject()));
            Coordinate homeLocation = new Coordinate(coordinate.getX(), coordinate.getY());
            hh.setHomeLocation(homeLocation);

        }



        }
    }

