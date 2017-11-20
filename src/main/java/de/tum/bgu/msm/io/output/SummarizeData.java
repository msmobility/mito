package de.tum.bgu.msm.io.output;

import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Methods to summarize model results
 * Author: Ana Moreno, Munich
 * Created on 11/07/2017.
 */


public class SummarizeData {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SummarizeData.class);


    public static void writeOutSyntheticPopulationWithTrips(DataSet dataSet) {
        //write out files with synthetic population and the number of trips

        logger.info("  Writing household file");
        String filehh = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + Resources.INSTANCE.getString(Properties.HOUSEHOLDS) + "_t.csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filehh, false);
        pwh.println("id,zone,hhSize,autos,trips,workTrips");
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {
            pwh.print(hh.getHhId());
            pwh.print(",");
            pwh.print(hh.getHomeZone());
            pwh.print(",");
            pwh.print(hh.getHhSize());
            pwh.print(",");
            pwh.print(hh.getAutos());
            pwh.print(",");
            int totalNumber = 0;
            for(Purpose purpose: Purpose.values()) {
                totalNumber += hh.getTripsForPurpose(purpose).size();
            }
            pwh.print(totalNumber);
            pwh.print(",");
            pwh.println(hh.getTripsForPurpose(Purpose.HBW).size());
        }
        pwh.close();

        logger.info("  Writing person file");
        String filepp = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + Resources.INSTANCE.getString(Properties.PERSONS) + "_t.csv";
        PrintWriter pwp = MitoUtil.openFileForSequentialWriting(filepp, false);
        pwp.println("id,hhID,hhSize,hhTrips,avTrips");
        for(MitoHousehold hh: dataSet.getHouseholds().values()) {
            for (MitoPerson pp : hh.getPersons().values()) {
                    pwp.print(pp.getId());
                    pwp.print(",");
                    pwp.print(hh.getHhId());
                    pwp.print(",");
                    pwp.print(hh.getHhSize());
                    pwp.print(",");
                    long hhTrips = Arrays.asList(Purpose.values()).stream().flatMap(purpose -> hh.getTripsForPurpose(purpose).stream()).count();
                    pwp.print(hhTrips);
                    pwp.print(",");
                    pwp.println(hhTrips / hh.getHhSize());
                }
            }
        pwp.close();
    }


}
