package de.tum.bgu.msm.io;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.Properties;
import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

/**
 * Created by Nico on 17.07.2017.
 */
public class TravelSurveyReader extends AbstractInputReader {

    private static Logger logger = Logger.getLogger(TravelSurveyReader.class);

    public TravelSurveyReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        // read household travel survey
        logger.info("  Reading household travel survey");
        String surveyHouseholdsPath = MitoUtil.getBaseDirectory() + "/" + Properties.getString(Properties.TRAVEL_SURVEY_HOUSEHOLDS);
        dataSet.setTravelSurveyHouseholdTable(CSVReader.readAsTableDataSet(surveyHouseholdsPath));
        String surveyTripsPath =  MitoUtil.getBaseDirectory() + "/" + Properties.getString(Properties.TRAVEL_SURVEY_TRIPS);
        dataSet.setTravelsurveyTripsTable(CSVReader.readAsTableDataSet(surveyTripsPath));
    }
}
