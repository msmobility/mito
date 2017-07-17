package de.tum.bgu.msm.io;

import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

import static de.tum.bgu.msm.io.InputManager.PROPERTIES_SCHOOL_ENROLLMENT_FILE;

/**
 * Created by Nico on 17.07.2017.
 */
public class SchoolEnrollmentReader {

    private static Logger logger = Logger.getLogger(SchoolEnrollmentReader.class);

    private final DataSet dataSet;
    private final String fileName;

    public SchoolEnrollmentReader(DataSet dataSet, ResourceBundle resources) {
        this.dataSet = dataSet;
        this.fileName = resources.getString(PROPERTIES_SCHOOL_ENROLLMENT_FILE);
    }

    public void read() {
        // read enrollment data
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
