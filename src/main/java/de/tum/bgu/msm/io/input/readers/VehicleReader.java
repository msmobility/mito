package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoJob;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.jobTypes.munich.MunichJobType;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.households.Household;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Nico on 17.07.2017.
 */
public class VehicleReader extends AbstractCsvReader {

    private static final Logger logger = Logger.getLogger(VehicleReader.class);

    private int posHhid = -1;
    private int posVehicleType = -1;

    public VehicleReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("Reading vehicles micro data from ascii file");
        Path filePath = Paths.get("C:\\models\\abit_standalone\\input\\newSP\\vv_2011_20230617.csv");
        super.read(filePath, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        posHhid = MitoUtil.findPositionInArray("id", header);
        posVehicleType = MitoUtil.findPositionInArray("type", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int hhid = Integer.parseInt(record[posHhid]);
        String vehicleType = record[posVehicleType];

        MitoHousehold hh = dataSet.getHouseholds().getOrDefault(hhid, null);

        if (hh == null) {
            throw new RuntimeException("The household does not exist");
        }

        if (vehicleType.equals("ELECTRIC")) {
            hh.setHasEV(true);
        }

    }
}
