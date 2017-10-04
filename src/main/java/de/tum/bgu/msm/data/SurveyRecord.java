package de.tum.bgu.msm.data;

import de.tum.bgu.msm.resources.Purpose;

public interface SurveyRecord {

    void addTripForPurpose(Purpose purpose);

    int getTripsForPurpose(Purpose purpose);

    int getId();

    int getHouseholdSize();

    int getWorkers();

    int getIncome();

    int getVehicleNumber();

    int getRegion();
}
