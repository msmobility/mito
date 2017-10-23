package de.tum.bgu.msm.data.survey.maryland;

import de.tum.bgu.msm.data.survey.TravelSurvey;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MarylandTravelSurvey implements TravelSurvey<MarylandSurveyRecord>{

    private final Map<Integer, MarylandSurveyRecord> records = new HashMap<>();

    @Override
    public void addRecord(MarylandSurveyRecord record) {
        this.records.put(record.getId(), record);
    }

    @Override
    public Map<Integer, MarylandSurveyRecord> getRecords() {
        return Collections.unmodifiableMap(records);
    }
}
