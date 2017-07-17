package de.tum.bgu.msm.io;

import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * Created by Nico on 17.07.2017.
 */
public class RegionsReader {

    private static Logger logger = Logger.getLogger(RegionsReader.class);

    private final DataSet dataSet;
    private final String regionFileName;

    public RegionsReader(DataSet dataSet, ResourceBundle resources) {
        this.dataSet = dataSet;
        this.regionFileName = resources.getString("household.travel.survey.reg");
    }

    public void read() {
        TableDataSet regionDefinition = CSVReader.readAsTableDataSet(regionFileName);
        for (int i = 1; i < regionDefinition.getRowCount(); i++) {
            int id = (int) regionDefinition.getValueAt(i, "Zone");
            int[] regions = regionDefinition.getColumnAsInt("Region");
            int region = regions[i - 1];
            if (dataSet.getZones().containsKey(id)) {
                dataSet.getZones().get(id).setRegion(region);
            } else {
                logger.warn("Region " + region + " referring to non-existing zone " + id + ". Ignoring it.");
            }
        }
    }
}
