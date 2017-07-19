package de.tum.bgu.msm.io.input.readers;

import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

/**
 * Created by Nico on 17.07.2017.
 */
public class SchoolEnrollmentReader extends CSVReader {

    private static Logger logger = Logger.getLogger(SchoolEnrollmentReader.class);

    public SchoolEnrollmentReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        // read enrollment data
        String fileName = Resources.INSTANCE.getString(Properties.SCHOOL_ENROLLMENT);
        TableDataSet enrollmentData = super.readAsTableDataSet(fileName);
        for (int i = 1; i <= enrollmentData.getRowCount(); i++) {
            int zoneId = enrollmentData.getColumnAsInt("Zone")[i - 1];
            int enrollment = enrollmentData.getColumnAsInt("Enrolment")[i - 1];
            try {
                dataSet.getZones().get(zoneId).setSchoolEnrollment(enrollment);
            } catch (Exception e) {
                logger.warn("School enrollment of " + enrollment + " refers to non existing zone " + zoneId + ". Ignoring it.");
            }
        }
    }

    @Override
    protected void processHeader(String[] header) {

    }

    @Override
    protected void processRecord(String[] record) {

    }
}
