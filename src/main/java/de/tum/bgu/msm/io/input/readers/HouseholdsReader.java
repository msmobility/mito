package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nico on 17.07.2017.
 */
public class HouseholdsReader extends CSVReader {

    private int posId = -1;
    private int posDwell = -1;
    private int posTaz = -1;
    private int posSize = -1;
    private int posAutos = -1;

    private static Logger logger = Logger.getLogger(HouseholdsReader.class);

    private final Map<Integer, MitoHousehold> households = new HashMap<>();

    public HouseholdsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading household micro data from ascii file");
        String fileName = Resources.INSTANCE.getString(Properties.HOUSEHOLDS);
        super.readLineByLine(fileName, ",");
        dataSet.setHouseholds(households);
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("id", header);
        posDwell = MitoUtil.findPositionInArray("dwelling", header);
        posTaz = MitoUtil.findPositionInArray("zone", header);
        posSize = MitoUtil.findPositionInArray("hhSize", header);
        posAutos = MitoUtil.findPositionInArray("autos", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        int taz = Integer.parseInt(record[posTaz]);
        int hhSize = Integer.parseInt(record[posSize]);
        int autos = Integer.parseInt(record[posAutos]);
        MitoHousehold household = new MitoHousehold(id, hhSize, 0, 0, 0, 0, 0, 0, 0, 0, autos, taz);
        households.put(household.getHhId(), household);
        try {
            dataSet.getZones().get(household.getHomeZone()).addHousehold();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }
}
