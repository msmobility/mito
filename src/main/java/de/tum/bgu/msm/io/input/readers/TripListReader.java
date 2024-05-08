package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TripListReader extends AbstractCsvReader {

    private static final Logger logger = Logger.getLogger(TripListReader.class);

    private int posId = -1;
    private int posOrigin = -1;
    private int posDestination = -1;
    private int posPurpose = -1;
    private int posPerson = -1;
    private int posDistance = -1;
    private int posMode = -1;
    private int posTimeAuto = -1;
    private int posTimeBus = -1;

    private int posTimeMetro = -1;

    private int posTimeTrain = -1;

    private int posDepartureTime = -1;

    private int posDepartureTimeReturn = -1;




    public TripListReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading trip micro data from ascii file");
        Path filePath = Resources.instance.getTripsFilePath();
        super.read(filePath, ",");

    }

    @Override
    public void processHeader(String[] header) {
        List<String> headerList = Arrays.asList(header);
        posId = headerList.indexOf("id");
        posOrigin = headerList.indexOf("origin");
        posDestination = headerList.indexOf("destination");
        posPurpose = headerList.indexOf("purpose");
        posPerson = headerList.indexOf("person");
        posDistance = headerList.indexOf("distance");
        posTimeBus = headerList.indexOf("time_bus");
        posMode = headerList.indexOf("mode");
        posTimeAuto = headerList.indexOf("time_auto");
        posTimeMetro = headerList.indexOf("time_metro");
        posTimeTrain = headerList.indexOf("time_train");
        posDepartureTime = headerList.indexOf("departure_time");
        posDepartureTimeReturn = headerList.indexOf("departure_time_return");
    }

    @Override
    public void processRecord(String[] record) {

        final int id = Integer.parseInt(record[posId]);
        final String purposeStr = record[posPurpose];


        if (purposeStr.equals("HBW")){
            final int travelerId = Integer.parseInt(record[posPerson]);
            MitoPerson pp = dataSet.getPersons().get(travelerId);
            final String modeStr = record[posMode];
            if (modeStr.equals("autoDriver")){
                pp.getAdditionalAttributes().put("p.isMobile_HBW_car",1.);
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_car")+
                        2*Double.parseDouble(record[posTimeAuto]);
                pp.getAdditionalAttributes().put("p.isMobile_HBW_car",travelTime);
            } else if (modeStr.equals("autoPassenger")){
                pp.getAdditionalAttributes().put("p.isMobile_HBW_car",1.);
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_car")+
                        2*Double.parseDouble(record[posTimeAuto]);
                pp.getAdditionalAttributes().put("p.isMobile_HBW_car",travelTime);
            } else if (modeStr.equals("bicycle")){
                pp.getAdditionalAttributes().put("p.isMobile_HBW_cycle",1.);
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_cycle")+
                        2*Double.parseDouble(record[posDistance])/ Properties.SPEED_BICYCLE_M_MIN;;
                pp.getAdditionalAttributes().put("p.TTB_HBW_cycle",travelTime);
            } else if (modeStr.equals("bus")){
                pp.getAdditionalAttributes().put("p.isMobile_HBW_PT",1.);
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_PT")+
                        2*Double.parseDouble(record[posTimeBus]);
                pp.getAdditionalAttributes().put("p.TTB_HBW_PT",travelTime);
            } else if (modeStr.equals("train")){
                pp.getAdditionalAttributes().put("p.isMobile_HBW_PT",1.);
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_PT")+
                        2*Double.parseDouble(record[posTimeTrain]);
                pp.getAdditionalAttributes().put("p.TTB_HBW_PT",travelTime);
            } else if (modeStr.equals("tramOrMetro")){
                pp.getAdditionalAttributes().put("p.isMobile_HBW_PT",1.);
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_PT")+
                        2*Double.parseDouble(record[posTimeMetro]);
                pp.getAdditionalAttributes().put("p.TTB_HBW_PT",travelTime);
            } else if (modeStr.equals("walk")){
                pp.getAdditionalAttributes().put("p.isMobile_HBW_walk",1.);
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_walk")+
                        2*Double.parseDouble(record[posDistance])/ Properties.SPEED_WALK_M_MIN;;
                pp.getAdditionalAttributes().put("p.TTB_HBW_walk",travelTime);
            }
        }




    }
}
