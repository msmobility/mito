package de.tum.bgu.msm.io.input;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.MatrixTravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import org.apache.log4j.Logger;

import java.util.Map;

public class InputManager {

    private static final Logger logger = Logger.getLogger(InputManager.class);

    private final DataSet dataSet;

    public InputManager(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void readAsStandAlone() {
        new ZonesReader(dataSet).read();
        new RegionsReader(dataSet).read();
        new SkimsReader(dataSet).read();
        new HouseholdsReader(dataSet).read();
        new PersonsReader(dataSet).read();
        new JobReader(dataSet).read();
        new EmploymentReader(dataSet).read();
    }

    public void readAdditionalData() {
        new SchoolEnrollmentReader(dataSet).read();
        new RegionsReader(dataSet).read();
        new TripAttractionRatesReader(dataSet).read();
        new TravelSurveyReader(dataSet).read();
    }

    public void readFromFeed(InputFeed feed) {
        for(Zone zone: feed.zones.values()) {
            dataSet.addZone(zone);
        }
        dataSet.setAutoTravelTimes(new MatrixTravelTimes(feed.autoTravelTimes));
        dataSet.setTransitTravelTimes(new MatrixTravelTimes(feed.transitTravelTimes));
        setHouseholdsFromFeed(feed.households);
    }


    private void setHouseholdsFromFeed(Map<Integer, MitoHousehold> households) {
        for (MitoHousehold household : households.values()) {
            if (dataSet.getZones().containsKey(household.getHomeZone())) {
                dataSet.getZones().get(household.getHomeZone()).addHousehold();
            } else {
                logger.error("Fed household " + household.getHhId() + " refers to non-existing home zone "
                        + household.getHomeZone() + ". Household will not be considered in any zone.");
            }
            dataSet.addHousehold(household);
            for(MitoPerson person: household.getPersons().values()) {
                dataSet.addPerson(person);
            }
        }
    }
}
