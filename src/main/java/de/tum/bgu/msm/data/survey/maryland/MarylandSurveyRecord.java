package de.tum.bgu.msm.data.survey.maryland;

import de.tum.bgu.msm.data.survey.SurveyRecord;
import de.tum.bgu.msm.data.Purpose;

import java.util.EnumMap;

public class MarylandSurveyRecord implements SurveyRecord {

    private final EnumMap<Purpose, Integer> tripsByPurpose = new EnumMap<Purpose, Integer>(Purpose.class);
    private final int id;
    private final int householdSize;
    private final int workers;
    private final int income;
    private final int vehicleNumber;
    private final int region;

    public MarylandSurveyRecord(int id, int householdSize, int workers, int income, int vehicleNumber, int region) {
        this.id = id;
        this.householdSize = householdSize;
        this.workers = workers;
        this.income = income;
        this.vehicleNumber = vehicleNumber;
        this.region = region;
    }

    @Override
    public void addTripForPurpose(Purpose purpose) {
        if(this.tripsByPurpose.containsKey(purpose)) {
            int temp = this.tripsByPurpose.get(purpose);
            this.tripsByPurpose.put(purpose, temp +1);
        } else {
            this.tripsByPurpose.put(purpose, 1);
        }
    }

    @Override
    public int getTripsForPurpose(Purpose purpose) {
        if(this.tripsByPurpose.containsKey(purpose)) {
            return tripsByPurpose.get(purpose);
        } else {
            return 0;
        }
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getHouseholdSize() {
        return this.householdSize;
    }

    @Override
    public int getWorkers() {
        return this.workers;
    }

    @Override
    public int getIncome() {
        return this.income;
    }

    @Override
    public int getVehicleNumber() {
        return this.vehicleNumber;
    }

    @Override
    public int getRegion() {
        return this.region;
    }
}
