package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.data.survey.maryland.MarylandSurveyRecord;
import de.tum.bgu.msm.data.survey.maryland.MarylandTravelSurvey;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

public class TravelSurveyReader extends CSVReader {

    private static final Logger logger = Logger.getLogger(TravelSurveyReader.class);

    private boolean households = true;

    private final MarylandTravelSurvey survey;
    private int posHhsiz;
    private int posHhwrk;
    private int posIncom;
    private int posHhveh;
    private int posRegion;
    private int posId;
    private int posTripHhId;
    private int posPurpose;

    public TravelSurveyReader(DataSet dataSet) {
        super(dataSet);
        survey = new MarylandTravelSurvey();
    }

    @Override
    public void read() {
        // read household travel survey
        logger.info("  Reading household travel survey");
        String surveyHouseholdsPath = MitoUtil.getBaseDirectory() + "/" + Resources.INSTANCE.getString(Properties.TRAVEL_SURVEY_HOUSEHOLDS);
        String surveyTripsPath = MitoUtil.getBaseDirectory() + "/" + Resources.INSTANCE.getString(Properties.TRAVEL_SURVEY_TRIPS);
        super.read(surveyHouseholdsPath, ",");
        households = false;
        super.read(surveyTripsPath, ",");
        dataSet.setSurvey(survey);
    }

    @Override
    protected void processHeader(String[] header) {
        if (households) {
            posHhsiz = MitoUtil.findPositionInArray("hhsiz", header);
            posHhwrk = MitoUtil.findPositionInArray("hhwrk", header);
            posIncom = MitoUtil.findPositionInArray("incom", header);
            posHhveh = MitoUtil.findPositionInArray("hhveh", header);
            posRegion = MitoUtil.findPositionInArray("urbanSuburbanRural", header);
            posId = MitoUtil.findPositionInArray("sampn", header);
        } else {
            posTripHhId = MitoUtil.findPositionInArray("sampn", header);
            posPurpose = MitoUtil.findPositionInArray("mainPurpose", header);
        }
    }

    @Override
    protected void processRecord(String[] record) {
        if (households) {
            int id = Integer.parseInt(record[posId]);
            int householdSize = Integer.parseInt(record[posHhsiz]);
            int workers = Integer.parseInt(record[posHhwrk]);
            int income = Integer.parseInt(record[posIncom]);
            int vehicles = Integer.parseInt(record[posHhveh]);
            int region = Integer.parseInt(record[posRegion]);
            survey.addRecord(new MarylandSurveyRecord(id, householdSize, workers, income, vehicles, region));
        } else {
            int id = Integer.parseInt(record[posTripHhId]);
            Purpose tripPurpose = Purpose.valueOf(record[posPurpose]);
            if(survey.getRecords().containsKey(id)) {
                survey.getRecords().get(id).addTripForPurpose(tripPurpose);
            } else {
                logger.fatal("Trip in survey refers to non-existing household sample " + id + "!", new RuntimeException());
            }
        }
    }
}
