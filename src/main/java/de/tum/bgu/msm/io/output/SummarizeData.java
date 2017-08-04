package de.tum.bgu.msm.io.output;


import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.resources.Resources;

import java.io.PrintWriter;
import java.util.List;

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
            for(List<MitoTrip> trips: hh.getTripsByPurpose().values()) {
                totalNumber += trips.size();
            }
            pwh.print(totalNumber);
            pwh.print(",");
            if(hh.getTripsByPurpose().containsKey(0)) {
                pwh.println(hh.getTripsByPurpose().get(0).size());
            } else {
                pwh.println(0);
            }
        }
        pwh.close();

        logger.info("  Writing person file");
        String filepp = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + Resources.INSTANCE.getString(Properties.PERSONS) + "_t.csv";
        PrintWriter pwp = MitoUtil.openFileForSequentialWriting(filepp, false);
        pwp.println("id,hhID,hhSize,hhTrips,avTrips");
        for (MitoPerson pp : dataSet.getPersons().values()) {
            pwp.print(pp.getId());
            pwp.print(",");
            int hhId = pp.getHhId();
            pwp.print(hhId);
            pwp.print(",");
            pwp.print(dataSet.getHouseholds().get(hhId).getHhSize());
            pwp.print(",");
            pwp.print(dataSet.getHouseholds().get(hhId).getTripsByPurpose().size());
            pwp.print(",");
            pwp.println(dataSet.getHouseholds().get(hhId).getTripsByPurpose().size() / dataSet.getHouseholds().get(hhId).getHhSize());
        }
        pwp.close();
    }


}
