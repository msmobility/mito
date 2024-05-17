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
        posTimeMetro = headerList.indexOf("time_tram_metro");
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
            double trips = pp.getAdditionalAttributes().get("p.HBW_trips") + 1;
            pp.getAdditionalAttributes().put("p.HBW_trips", trips);

            final String modeStr = record[posMode];
            if (modeStr.equals("autoDriver")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBW_car","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_car")+
                        2*Double.parseDouble(record[posTimeAuto]);
                pp.getAdditionalAttributes().put("p.isMobile_HBW_car",travelTime);
            } else if (modeStr.equals("autoPassenger")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBW_car","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_car")+
                        2*Double.parseDouble(record[posTimeAuto]);
                pp.getAdditionalAttributes().put("p.isMobile_HBW_car",travelTime);
            } else if (modeStr.equals("bicycle")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBW_cycle","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_cycle")+
                        2*Double.parseDouble(record[posDistance])/ Properties.SPEED_BICYCLE_M_MIN / 1000;
                pp.getAdditionalAttributes().put("p.TTB_HBW_cycle",travelTime);
            } else if (modeStr.equals("bus")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBW_PT","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_PT")+
                        2*Double.parseDouble(record[posTimeBus]);
                pp.getAdditionalAttributes().put("p.TTB_HBW_PT",travelTime);
            } else if (modeStr.equals("train")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBW_PT","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_PT")+
                        2*Double.parseDouble(record[posTimeTrain]);
                pp.getAdditionalAttributes().put("p.TTB_HBW_PT",travelTime);
            } else if (modeStr.equals("tramOrMetro")){
                //logger.info(pp.getId() + "   trip id " + id);
                pp.getAdditionalStringAttributes().put("p.isMobile_HBW_PT","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_PT")+
                        2*Double.parseDouble(record[posTimeMetro]);
                pp.getAdditionalAttributes().put("p.TTB_HBW_PT",travelTime);
            } else if (modeStr.equals("walk")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBW_walk","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBW_walk")+
                        2*Double.parseDouble(record[posDistance])/ Properties.SPEED_WALK_M_MIN / 1000;
                pp.getAdditionalAttributes().put("p.TTB_HBW_walk",travelTime);
            }
        }

        if (purposeStr.equals("HBE")){
            final int travelerId = Integer.parseInt(record[posPerson]);
            MitoPerson pp = dataSet.getPersons().get(travelerId);

            double trips = pp.getAdditionalAttributes().get("p.HBE_trips") + 1;
            pp.getAdditionalAttributes().put("p.HBE_trips", trips);

            final String modeStr = record[posMode];
            if (modeStr.equals("autoDriver")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBE_car","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBE_car")+
                        2*Double.parseDouble(record[posTimeAuto]);
                pp.getAdditionalAttributes().put("p.isMobile_HBE_car",travelTime);
            } else if (modeStr.equals("autoPassenger")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBE_car","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBE_car")+
                        2*Double.parseDouble(record[posTimeAuto]);
                pp.getAdditionalAttributes().put("p.isMobile_HBE_car",travelTime);
            } else if (modeStr.equals("bicycle")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBE_cycle","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBE_cycle")+
                        2*Double.parseDouble(record[posDistance])/ Properties.SPEED_BICYCLE_M_MIN / 1000;;
                pp.getAdditionalAttributes().put("p.TTB_HBE_cycle",travelTime);
            } else if (modeStr.equals("bus")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBE_PT","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBE_PT")+
                        2*Double.parseDouble(record[posTimeBus]);
                pp.getAdditionalAttributes().put("p.TTB_HBE_PT",travelTime);
            } else if (modeStr.equals("train")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBE_PT","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBE_PT")+
                        2*Double.parseDouble(record[posTimeTrain]);
                pp.getAdditionalAttributes().put("p.TTB_HBE_PT",travelTime);
            } else if (modeStr.equals("tramOrMetro")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBE_PT","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBE_PT")+
                        2*Double.parseDouble(record[posTimeMetro]);
                pp.getAdditionalAttributes().put("p.TTB_HBE_PT",travelTime);
            } else if (modeStr.equals("walk")){
                pp.getAdditionalStringAttributes().put("p.isMobile_HBE_walk","yes");
                double travelTime = pp.getAdditionalAttributes().get("p.TTB_HBE_walk")+
                        2 * Double.parseDouble(record[posDistance])/ Properties.SPEED_WALK_M_MIN / 1000;;
                pp.getAdditionalAttributes().put("p.TTB_HBE_walk",travelTime);
            }
        }

        if(purposeStr.equals("HBS")){
            final int travelerId = Integer.parseInt(record[posPerson]);
            MitoPerson pp = dataSet.getPersons().get(travelerId);
            double trips = pp.getAdditionalAttributes().get("p.HBS_trips") + 1;
            pp.getAdditionalAttributes().put("p.HBS_trips", trips);
        }

        if(purposeStr.equals("HBO")){
            final int travelerId = Integer.parseInt(record[posPerson]);
            MitoPerson pp = dataSet.getPersons().get(travelerId);
            double trips = pp.getAdditionalAttributes().get("p.HBO_trips") + 1;
            pp.getAdditionalAttributes().put("p.HBO_trips", trips);
        }

        if(purposeStr.equals("HBR")){
            final int travelerId = Integer.parseInt(record[posPerson]);
            MitoPerson pp = dataSet.getPersons().get(travelerId);
            double trips = pp.getAdditionalAttributes().get("p.HBR_trips") + 1;
            pp.getAdditionalAttributes().put("p.HBR_trips", trips);
        }

        if(purposeStr.equals("NHBW")){
            final int travelerId = Integer.parseInt(record[posPerson]);
            MitoPerson pp = dataSet.getPersons().get(travelerId);
            double trips = pp.getAdditionalAttributes().get("p.NHBW_trips") + 1;
            pp.getAdditionalAttributes().put("p.NHBW_trips", trips);
        }

        if(purposeStr.equals("NHBO")){
            final int travelerId = Integer.parseInt(record[posPerson]);
            MitoPerson pp = dataSet.getPersons().get(travelerId);
            double trips = pp.getAdditionalAttributes().get("p.NHBO_trips") + 1;
            pp.getAdditionalAttributes().put("p.NHBO_trips", trips);
        }



    }
}
