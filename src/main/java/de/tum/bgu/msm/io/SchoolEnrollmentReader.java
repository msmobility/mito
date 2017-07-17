package de.tum.bgu.msm.io;

import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.Properties;
import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

/**
 * Created by Nico on 17.07.2017.
 */
public class SchoolEnrollmentReader extends AbstractInputReader {

    private static Logger logger = Logger.getLogger(SchoolEnrollmentReader.class);

    public SchoolEnrollmentReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        // read enrollment data
        String fileName = Properties.getString(Properties.SCHOOL_ENROLLMENT);
        TableDataSet enrollmentData = CSVReader.readAsTableDataSet(fileName);
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
}
