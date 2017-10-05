package de.tum.bgu.msm.data.survey;

import java.util.Map;

public interface TravelSurvey<T extends SurveyRecord>{

    void addRecord(T record);

    Map<Integer, T> getRecords();
}
