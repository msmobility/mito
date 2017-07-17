package de.tum.bgu.msm.io;

import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.Properties;
import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

/**
 * Created by Nico on 17.07.2017.
 */
public class RegionsReader extends AbstractInputReader {

    private static Logger logger = Logger.getLogger(RegionsReader.class);

    public RegionsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        TableDataSet regionDefinition = CSVReader.readAsTableDataSet(Properties.getString(Properties.REGIONS));
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
