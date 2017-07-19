package de.tum.bgu.msm.io.input.readers;

import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by Nico on 17.07.2017.
 */
public class EmploymentReader extends CSVReader {

    private static Logger logger = Logger.getLogger(EmploymentReader.class);


    public EmploymentReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        // SMZ,State,RET00,OFF00,IND00,OTH00,RET07,OFF07,IND07,OTH07,RET10,OFF10,IND10,OTH10,RET30,OFF30,IND30,OTH30,RET40,OFF40,IND40,OTH40
        String fileName = Resources.INSTANCE.getString(Properties.EMPLOYMENT);
        TableDataSet employment = super.readAsTableDataSet(fileName);
        int[] indEmpl = employment.getColumnAsInt("IND00");
        int[] retEmpl = employment.getColumnAsInt("RET00");
        int[] offEmpl = employment.getColumnAsInt("OFF00");
        int[] othEmpl = employment.getColumnAsInt("OTH00");
        int[] totEmpl = new int[employment.getRowCount()];
        for (int i = 0; i < employment.getRowCount(); i++) {
            totEmpl[i] = indEmpl[i] + retEmpl[i] + offEmpl[i] + othEmpl[i];
            int zoneId = employment.getColumnAsInt("SMZ")[i];
            Map<Integer, Zone> zones = dataSet.getZones();
            if (zones.containsKey(zoneId)) {
                assignEmployeesToZone(indEmpl[i], retEmpl[i], offEmpl[i], othEmpl[i], totEmpl[i], zones.get(zoneId));
            } else {
                logger.warn("Zone " + zoneId + " of employment table not found. Ignoring it.");
            }
        }
    }

    private void assignEmployeesToZone(int indEmpl, int retailEmpl, int officerEmpl, int otherEmpl, int totalEmpl, Zone zone) {
        zone.setIndEmpl(indEmpl);
        zone.setRetailEmpl(retailEmpl);
        zone.setOfficeEmpl(officerEmpl);
        zone.setOtherEmpl(otherEmpl);
        zone.setTotalEmpl(totalEmpl);
    }

    @Override
    protected void processHeader(String[] header) {

    }

    @Override
    protected void processRecord(String[] record) {

    }
}
