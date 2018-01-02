package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

/**
 * Created by Nico on 17.07.2017.
 */
public class HouseholdsReader extends CSVReader {

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
        String fileName = Resources.INSTANCE.getString(Properties.HOUSEHOLDS);
        super.read(fileName, ",");
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
        int taz = Integer.parseInt(record[posTaz]);
        int autos = Integer.parseInt(record[posAutos]);
        MitoZone zone = dataSet.getZones().get(taz);
        if (zone == null) {
            logger.warn(String.format("Household %d refers to non-existing zone %d! Ignoring it.", id, taz));
            return;
        }
        dataSet.addHousehold(new MitoHousehold(id, 0, autos, zone));
        zone.addHousehold();
    }
}
