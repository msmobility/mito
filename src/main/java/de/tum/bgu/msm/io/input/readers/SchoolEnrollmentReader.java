package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

public class SchoolEnrollmentReader extends CSVReader {

    private static final Logger logger = Logger.getLogger(SchoolEnrollmentReader.class);
    private int zoneIndex;
    private int enrolmentIndex;

    public SchoolEnrollmentReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.SCHOOL_ENROLLMENT), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        zoneIndex = MitoUtil.findPositionInArray("Zone", header);
        enrolmentIndex = MitoUtil.findPositionInArray("Enrolment", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zone = Integer.parseInt(record[zoneIndex]);
        int enrolment = Integer.parseInt(record[enrolmentIndex]);
        if (dataSet.getZones().containsKey(zone)) {
            dataSet.getZones().get(zone).setSchoolEnrollment(enrolment);
        } else {
            logger.warn("School enrollment of " + enrolment + " refers to non existing zone " + zone + ". Ignoring it.");
        }
    }
}
