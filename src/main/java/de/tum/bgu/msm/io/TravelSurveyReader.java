package de.tum.bgu.msm.io;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * Created by Nico on 17.07.2017.
 */
public class TravelSurveyReader {

    private static Logger logger = Logger.getLogger(TravelSurveyReader.class);

    private final DataSet dataSet;
    private final String tripsFileName;
    private final String householdsFileName;

    public TravelSurveyReader(DataSet dataSet, ResourceBundle resources) {
        this.dataSet = dataSet;
        this.householdsFileName = MitoUtil.getBaseDirectory() + "/" + resources.getString("household.travel.survey.hh");
        this.tripsFileName = MitoUtil.getBaseDirectory() + "/" + resources.getString("household.travel.survey.trips");
    }

    public void read() {
        // read household travel survey
        logger.info("  Reading household travel survey");
        dataSet.setTravelSurveyHouseholdTable(CSVReader.readAsTableDataSet(householdsFileName));
        dataSet.setTravelsurveyTripsTable(CSVReader.readAsTableDataSet(tripsFileName));
    }
}
