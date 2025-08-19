package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;


public class LowEmissionZoneReader extends AbstractCsvReader {
    private static final Logger logger = Logger.getLogger(LowEmissionZoneReader.class);

    private int posId = -1;
    private Set<Integer> lowEmissionZones;

    public LowEmissionZoneReader(DataSet dataSet) {
        super(dataSet);
        lowEmissionZones = new HashSet<>();
    }


    @Override
    public void read() {
        logger.info("Reading low emission zones from csv file");
        Path filePath = Paths.get("////nas.ads.mwn.de/tubv/mob/projects/2021/DatSim/abit/transportpolicypaper/lowEmissionZones.csv");
        super.read(filePath, ",");

        // Set household LEZ status
        setHouseholdLEZStatus();
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("id", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[posId]);
        lowEmissionZones.add(zoneId);
    }

    private void setHouseholdLEZStatus() {
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            if (lowEmissionZones.contains(household.getZoneId())) {
                household.setInsideLEZ(true);
            } else {
                household.setInsideLEZ(false);
            }
        }
    }
}
