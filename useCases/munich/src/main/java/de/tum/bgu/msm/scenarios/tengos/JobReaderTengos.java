package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoJob;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;
import de.tum.bgu.msm.data.jobTypes.MunichJobType;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

import java.nio.file.Path;

/**
 * Created by Nico on 17.07.2017.
 */
public class JobReaderTengos extends AbstractCsvReader {

    private static final Logger logger = Logger.getLogger(JobReaderTengos.class);
    private final JobTypeFactory factory;

    private int posId = -1;
    private int posZone = -1;
    private int posWorker = -1;
    private int posType = -1;
    private int posJobCoordX = -1;
    private int posJobCoordY = -1;

    public JobReaderTengos(DataSet dataSet, JobTypeFactory factory) {
        super(dataSet);
        this.factory = factory;
    }

    @Override
    public void read() {
        logger.info("Reading job micro data from ascii file");
        Path filePath = Resources.instance.getJobsFilePath();
        super.read(filePath, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("id", header);
        posZone = MitoUtil.findPositionInArray("zone", header);
        posWorker = MitoUtil.findPositionInArray("personId", header);
        posType = MitoUtil.findPositionInArray("type", header);
        posJobCoordX = MitoUtil.findPositionInArray("coordX", header);
        posJobCoordY = MitoUtil.findPositionInArray("coordY", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        int zoneId = Integer.parseInt(record[posZone]);
        int worker = Integer.parseInt(record[posWorker]);
        String type = record[posType];
        if (worker > 0) {
            MitoZone zone = dataSet.getZones().get(zoneId);
            if (zone == null) {
                logger.warn(String.format("Job %d refers to non-existing zone %d! Ignoring it.", id, zoneId));
                //return null;
            }

            try {
                zone.addEmployeeForType(factory.getType(type.toUpperCase().replaceAll("\"","")));
            } catch (IllegalArgumentException e) {
                //logger.error("Job Type " + type + " used in job microdata but is not defined");
            }

            Coordinate coordinate = (new Coordinate(Double.parseDouble(record[posJobCoordX]),
            		Double.parseDouble(record[posJobCoordY])));

            MitoJob job = new MitoJobTengos(zone, coordinate, id);
            ((MitoJobTengos) job).setJobType((MunichJobTypeTengos)factory.getType(type.toUpperCase().replaceAll("\"","")));
            dataSet.addJob(job);
        }
    }
}
