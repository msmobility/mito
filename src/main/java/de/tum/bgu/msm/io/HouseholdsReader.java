package de.tum.bgu.msm.io;

import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static de.tum.bgu.msm.io.InputManager.PROPERTIES_HH_FILE_ASCII;

/**
 * Created by Nico on 17.07.2017.
 */
public class HouseholdsReader {

    private static Logger logger = Logger.getLogger(HouseholdsReader.class);

    private final String fileName;
    private final DataSet dataSet;
    private final Map<Integer, MitoHousehold> households = new HashMap<>();
    private final HouseholdsCSVAdapter adapter = new HouseholdsCSVAdapter();

    public HouseholdsReader(DataSet dataSet, ResourceBundle resources) {
        this.dataSet = dataSet;
        this.fileName = ResourceUtil.getProperty(resources, PROPERTIES_HH_FILE_ASCII);
    }

    public void read() {
        logger.info("  Reading household micro data from ascii file");
        CSVReader reader = new CSVReader(fileName, ",", adapter);
        reader.read();
        dataSet.setHouseholds(households);
        }


    class HouseholdsCSVAdapter implements CSVAdapter {

        private int posId = -1;
        private int posDwell = -1;
        private int posTaz = -1;
        private int posSize = -1;
        private int posAutos = -1;

        @Override
        public void processHeader(String[] header) {
            posId = MitoUtil.findPositionInArray("id", header);
            posDwell = MitoUtil.findPositionInArray("dwelling", header);
            posTaz = MitoUtil.findPositionInArray("zone", header);
            posSize = MitoUtil.findPositionInArray("hhSize", header);
            posAutos = MitoUtil.findPositionInArray("autos", header);
        }

        @Override
        public void processRecord(String[] record) {
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
}
