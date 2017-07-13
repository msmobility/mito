package de.tum.bgu.msm.data;

import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.MitoUtil;

import java.io.PrintWriter;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Methods to summarize model results
 * Author: Ana Moreno, Munich
 * Created on 11/07/2017.
 */


public class SummarizeData {
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SummarizeData.class);
    protected static final String PROPERTIES_FILENAME_HH_MICRODATA        = "household.file.ascii";
    protected static final String PROPERTIES_FILENAME_PP_MICRODATA        = "person.file.ascii";
    protected static final String PROPERTIES_BASE_DIRECTORY               = "base.directory";



    public static void writeOutSyntheticPopulationWithTrips (ResourceBundle rb){
        //write out files with synthetic population and the number of trips

        logger.info("  Writing household file");
        String filehh = rb.getString(PROPERTIES_BASE_DIRECTORY) + "/" + rb.getString(PROPERTIES_FILENAME_HH_MICRODATA) + "_t.csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filehh, false);
        pwh.println("id,zone,hhSize,autos,trips,workTrips");
        MitoHousehold[] hhs = MitoHousehold.getHouseholdArray();
        for (MitoHousehold hh : hhs) {
            pwh.print(hh.getHhId());
            pwh.print(",");
            pwh.print(hh.getHomeZone());
            pwh.print(",");
            pwh.print(hh.getHhSize());
            pwh.print(",");
            pwh.print(hh.getAutos());
            pwh.print(",");
            pwh.print(hh.getTrips().length);
            pwh.print(",");
            pwh.println(TripDataManager.getTotalNumberOfTripsGeneratedByPurposeByHousehold(0,hh));
        }
        pwh.close();

        logger.info("  Writing person file");
        String filepp = rb.getString(PROPERTIES_BASE_DIRECTORY) + "/" + rb.getString(PROPERTIES_FILENAME_PP_MICRODATA) + "_t.csv";
        PrintWriter pwp = MitoUtil.openFileForSequentialWriting(filepp, false);
        pwp.println("id,hhID,hhSize,hhTrips,avTrips");
        MitoPerson[] pps = MitoPerson.getMitoPersons();
        for (MitoPerson pp : pps) {
            pwp.print(pp.getId());
            pwp.print(",");
            pwp.print(pp.getHh().getHhId());
            pwp.print(",");
            pwp.print(pp.getHh().getHhSize());
            pwp.print(",");
            pwp.print(pp.getHh().getTrips().length);
            pwp.print(",");
            pwp.println(pp.getHh().getTrips().length / pp.getHh().getHhSize());
        }
        pwp.close();


    }



}
