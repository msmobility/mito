package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.nio.file.Path;

/**
 * Created by Nico on 17.07.2017.
 */
public class HouseholdsReader extends AbstractCsvReader {

    private int posId = -1;
    private int posTaz = -1;
    private int posAutos = -1;

    private static final Logger logger = Logger.getLogger(HouseholdsReader.class);

    public HouseholdsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading household micro data from ascii file");
        Path filePath = Resources.instance.getHouseholdsFilePath();
        super.read(filePath, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("id", header);
        posTaz = MitoUtil.findPositionInArray("zone", header);
        posAutos = MitoUtil.findPositionInArray("autos", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        if (id%Double.parseDouble(Resources.instance.getString(Properties.HOUSEHOLD_SCALER)) == 0) {
            int autos = Integer.parseInt(record[posAutos]);
            MitoHousehold hh = new MitoHousehold(id, 0, autos);
            int AVautos = 0;
            if (MitoUtil.getRandomObject().nextDouble() < Double.parseDouble(Resources.instance.getString(Properties.AV_PRIVATE_PENETRATION_RATE))){
                AVautos = 1;
            }
            hh.setAdditionalAttributes(Resources.instance.getString(de.tum.bgu.msm.resources.Properties.HH_ADDITIONAL_ATTRIBUTE_AV), AVautos);
            dataSet.addHousehold(hh);
        }
    }
}
