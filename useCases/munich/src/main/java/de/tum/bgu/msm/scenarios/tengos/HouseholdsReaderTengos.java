package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.io.input.readers.HouseholdsReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

public class HouseholdsReaderTengos extends HouseholdsReader {

    private int posId = -1;
    private int posTaz = -1;
    private int posAutos = -1;
    private int posNursingHome = -1;

    private static final double scaleFactorForTripGeneration = Resources.instance.getDouble(Properties.SCALE_FACTOR_FOR_TRIP_GENERATION, 1.0);


    public HouseholdsReaderTengos(DataSet dataSet) {
        super(dataSet);
    }

    private static final Logger logger = Logger.getLogger(HouseholdsReaderTengos.class);


    @Override
    protected void processHeader(String[] header) {
        super.processHeader(header);
        posNursingHome = MitoUtil.findPositionInArray("nursingHome", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        int autos = Integer.parseInt(record[posAutos]);
        int taz = Integer.parseInt(record[posTaz]);
        int nursingHome = Integer.parseInt(record[posNursingHome]);

        // is the household modelled? (depends on scale factor)
        boolean isModelled = MitoUtil.getRandomObject().nextDouble() < scaleFactorForTripGeneration;

        MitoHouseholdTengos hh = new MitoHouseholdTengos(id, 0, autos, isModelled);
        hh.setNursingHome(nursingHome>=0);
        if(nursingHome>=0){
            hh.setHomeLocation(dataSet.getZones().get(taz).getGeometry().getCoordinate());
            hh.setHomeZone(dataSet.getZones().get(taz));
        }
        dataSet.addHousehold(hh);
    }
}
