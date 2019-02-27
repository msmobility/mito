package de.tum.bgu.msm.io.input.readers;

import com.vividsolutions.jts.geom.Coordinate;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoSchool;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

public class SchoolsReader extends AbstractCsvReader {

    private static final Logger logger = Logger.getLogger(JobReader.class);

    private int posId = -1;
    private int posZone = -1;
    private int posOccupancy = -1;
    private int posCoordX = -1;
    private int posCoordY = -1;

    public SchoolsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("id", header);
        posZone = MitoUtil.findPositionInArray("zone", header);
        posOccupancy = MitoUtil.findPositionInArray("occupancy", header);
        posCoordX = MitoUtil.findPositionInArray("coordX", header);
        posCoordY = MitoUtil.findPositionInArray("coordY", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        int zoneId = Integer.parseInt(record[posZone]);
        MitoZone zone = dataSet.getZones().get(zoneId);
        int occupancy = Integer.parseInt(record[posOccupancy]);
        Coordinate coordinate = (new Coordinate(Double.parseDouble(record[posCoordX]),
                Double.parseDouble(record[posCoordY])));
        MitoSchool school = new MitoSchool(zone, coordinate, id);
        dataSet.addSchool(school);
        zone.addSchoolEnrollment(occupancy);
    }

    @Override
    public void read() {
        logger.info("Reading school micro data from ascii file");
        String fileName = Resources.INSTANCE.getString(Properties.SCHOOLS);
        super.read(fileName, ",");
    }
}
