package de.tum.bgu.msm.data;

import de.tum.bgu.msm.data.travelTimes.TravelTimes;

import java.util.HashMap;
import java.util.Map;

public class DataSetAbm {

    private final Map<Integer, MitoHousehold> households;
    private final Map<Integer, MitoPerson> persons;
    private final TravelTimes travelTimes;


    public DataSetAbm(TravelTimes travelTimes) {
        this.travelTimes = travelTimes;
        persons = new HashMap<>();
        households = new HashMap<>();
    }

    public Map<Integer, MitoHousehold> getHouseholds() {
        return households;
    }

    public Map<Integer, MitoPerson> getPersons() {
        return persons;
    }
}
