package de.tum.bgu.msm.data;



import de.tum.bgu.msm.MitoData;
import de.tum.bgu.msm.MitoUtil;

import java.io.PrintWriter;
import java.util.ResourceBundle;

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



    public static void writeOutSyntheticPopulationWithTrips(ResourceBundle rb, DataSet dataSet){
        //write out files with synthetic population and the number of trips

        logger.info("  Writing household file");
        String filehh = rb.getString(PROPERTIES_BASE_DIRECTORY) + "/" + rb.getString(PROPERTIES_FILENAME_HH_MICRODATA) + "_t.csv";
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
            pwh.print(hh.getTrips().size());
            pwh.print(",");
            pwh.println(dataSet.getTripDataManager().getTotalNumberOfTripsGeneratedByPurposeByHousehold(0,hh));
        }
        pwh.close();

        logger.info("  Writing person file");
        String filepp = rb.getString(PROPERTIES_BASE_DIRECTORY) + "/" + rb.getString(PROPERTIES_FILENAME_PP_MICRODATA) + "_t.csv";
        PrintWriter pwp = MitoUtil.openFileForSequentialWriting(filepp, false);
        pwp.println("id,hhID,hhSize,hhTrips,avTrips");
        for (MitoPerson pp : dataSet.getPersons().values()) {
            pwp.print(pp.getId());
            pwp.print(",");
            pwp.print(pp.getHh().getHhId());
            pwp.print(",");
            pwp.print(pp.getHh().getHhSize());
            pwp.print(",");
            pwp.print(pp.getHh().getTrips().size());
            pwp.print(",");
            pwp.println(pp.getHh().getTrips().size() / pp.getHh().getHhSize());
        }
        pwp.close();
    }
}
