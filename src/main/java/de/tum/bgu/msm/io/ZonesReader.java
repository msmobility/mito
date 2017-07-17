package de.tum.bgu.msm.io;

import com.pb.common.datafile.TableDataSet;
import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Zone;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static de.tum.bgu.msm.io.InputManager.PROPERTIES_ZONAL_DATA_FILE;

/**
 * Created by Nico on 17.07.2017.
 */
public class ZonesReader {

    private static Logger logger = Logger.getLogger(ZonesReader.class);

    private final DataSet dataSet;
    private final String zoneFileName;
    private final String reductionNearBorderDamperFileName;
    private final boolean removeTripsAtBorder;
    private final Map<Integer, Zone> zones = new HashMap<>();


    public ZonesReader(DataSet dataSet, ResourceBundle resources) {
        this.dataSet = dataSet;
        this.zoneFileName = ResourceUtil.getProperty(resources, PROPERTIES_ZONAL_DATA_FILE);
        this.reductionNearBorderDamperFileName = resources.getString("reduction.near.outer.border");
        this.removeTripsAtBorder = ResourceUtil.getBooleanProperty(resources, "reduce.trips.at.outer.border", false);
    }

    public void read() {
        readZones();
        readReductionDampers();
    }


    private void readZones() {
        // read in zones from file
        TableDataSet zonalData = CSVReader.readAsTableDataSet(zoneFileName);
        for (int i = 1; i <= zonalData.getRowCount(); i++) {
            Zone zone = new Zone(zonalData.getColumnAsInt("ZoneId")[i - 1], zonalData.getValueAt(i, "ACRES"));
            zones.put(zone.getZoneId(), zone);
        }
        dataSet.setZones(zones);
    }


    private void readReductionDampers() {
        if (removeTripsAtBorder) {
            TableDataSet reductionNearBorder = CSVReader.readAsTableDataSet(reductionNearBorderDamperFileName);
            for (int i = 1; i <= reductionNearBorder.getRowCount(); i++) {
                int id = (int) reductionNearBorder.getValueAt(i, "Zone");
                float damper = reductionNearBorder.getValueAt(i, "damper");
                if (zones.containsKey(id)) {
                    zones.get(id).setReductionAtBorderDamper(damper);
                } else {
                    logger.warn("Damper of " + damper + " refers to non-existing zone " + id + ". Ignoring it.");
                }
            }
        }
    }
}
