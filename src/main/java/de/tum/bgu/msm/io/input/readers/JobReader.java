package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

/**
 * Created by Nico on 17.07.2017.
 */
public class JobReader extends CSVReader {

    private static final Logger logger = Logger.getLogger(JobReader.class);

    private int posId = -1;
    private int posZone = -1;
    private int posWorker = -1;

    public JobReader(DataSet dataSet) {
        super(dataSet);
    }


    @Override
    public void read() {
        logger.info("  Reading job micro data from ascii file");
        String fileName = Resources.INSTANCE.getString(Properties.JOBS);
        super.readLineByLine(fileName, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("id", header);
        posZone = MitoUtil.findPositionInArray("zone", header);
        posWorker = MitoUtil.findPositionInArray("personId", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        int zoneId = Integer.parseInt(record[posZone]);
        int worker = Integer.parseInt(record[posWorker]);
        if (worker > 0) {
            MitoPerson pp = dataSet.getPersons().get(worker);
            if(pp == null) {
                logger.warn(String.format("Job %d refers to non-existing person %d! Ignoring it.", id, worker));
                return;
            }
            if (pp.getWorkplace() != id) {
                logger.warn("Person " + worker + " has workplace " + pp.getWorkplace() + " in person file but workplace "
                        + id + " in job file. Ignoring job.");
                return;
            }
            Zone zone = dataSet.getZones().get(zoneId);
            if (zone == null) {
                logger.warn(String.format("Job %d refers to non-existing zone %d! Ignoring it.", id, zoneId));
                return;
            }
            pp.setWorkzone(zone);
        }
    }
}
