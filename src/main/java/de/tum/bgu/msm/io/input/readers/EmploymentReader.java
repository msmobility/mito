package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

public class EmploymentReader extends CSVReader {

    private static final Logger logger = Logger.getLogger(EmploymentReader.class);
    private int indEmpl;
    private int retEmpl;
    private int offEmpl;
    private int othEmpl;
    private int zoneIndex;


    public EmploymentReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        String fileName = Resources.INSTANCE.getString(Properties.EMPLOYMENT);
        super.read(fileName, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        indEmpl = MitoUtil.findPositionInArray("IND00", header);
        retEmpl = MitoUtil.findPositionInArray("RET00", header);
        offEmpl = MitoUtil.findPositionInArray("OFF00", header);
        othEmpl = MitoUtil.findPositionInArray("OTH00", header);
        zoneIndex = MitoUtil.findPositionInArray("SMZ", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int industryEmployees = Integer.parseInt(record[indEmpl]);
        int retailEmployees = Integer.parseInt(record[retEmpl]);
        int officeEmployees = Integer.parseInt(record[offEmpl]);
        int otherEmployees = Integer.parseInt(record[othEmpl]);
        int totalEmployees = industryEmployees + retailEmployees + officeEmployees + otherEmployees;
        int zoneId = Integer.parseInt(record[zoneIndex]);
        if (dataSet.getZones().containsKey(zoneId)) {
            assignEmployeesToZone(industryEmployees, retailEmployees, officeEmployees, otherEmployees, totalEmployees, dataSet.getZones().get(zoneId));
        } else {
            logger.warn("Zone " + zoneId + " of employment table not found. Ignoring it.");
        }
    }

    private void assignEmployeesToZone(int indEmpl, int retailEmpl, int officerEmpl, int otherEmpl, int totalEmpl, Zone zone) {
        zone.setIndEmpl(indEmpl);
        zone.setRetailEmpl(retailEmpl);
        zone.setOfficeEmpl(officerEmpl);
        zone.setOtherEmpl(otherEmpl);
        zone.setTotalEmpl(totalEmpl);
    }
}
