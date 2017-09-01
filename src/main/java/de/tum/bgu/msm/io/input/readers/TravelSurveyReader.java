package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

/**
 * Created by Nico on 17.07.2017.
 */
public class TravelSurveyReader extends CSVReader {

    private static final Logger logger = Logger.getLogger(TravelSurveyReader.class);

    public TravelSurveyReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        // read household travel survey
        logger.info("  Reading household travel survey");
        String surveyHouseholdsPath = MitoUtil.getBaseDirectory() + "/" + Resources.INSTANCE.getString(Properties.TRAVEL_SURVEY_HOUSEHOLDS);
        dataSet.setTravelSurveyHouseholdTable(super.readAsTableDataSet(surveyHouseholdsPath));
        String surveyTripsPath =  MitoUtil.getBaseDirectory() + "/" + Resources.INSTANCE.getString(Properties.TRAVEL_SURVEY_TRIPS);
        dataSet.setTravelSurveyTripsTable(super.readAsTableDataSet(surveyTripsPath));
    }

    @Override
    protected void processHeader(String[] header) {

    }

    @Override
    protected void processRecord(String[] record) {

    }
}
