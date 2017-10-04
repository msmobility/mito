package de.tum.bgu.msm.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TravelSurvey {

    private final Map<Integer, SurveyRecord> records = new HashMap<>();

    public void addRecord(SurveyRecord record) {
        this.records.put(record.getId(), record);
    }

    public Map<Integer, SurveyRecord> getRecords() {
        return Collections.unmodifiableMap(records);
    }
}
