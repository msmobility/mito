package de.tum.bgu.msm.io.input;

import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nico on 14.07.2017.
 */
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
        new DistancesReader(dataSet).read();
        new HouseholdsReader(dataSet).read();
        new PersonsReader(dataSet).read();
        new JobReader(dataSet).read();
        new EmploymentReader(dataSet).read();
    }

    public void readAdditionalData() {
        dataSet.setPurposes(Resources.INSTANCE.getArray(Properties.PURPOSES));
        new SchoolEnrollmentReader(dataSet).read();
        new RegionsReader(dataSet).read();
        new TripAttractionRatesReader(dataSet).read();
        new TravelSurveyReader(dataSet).read();
    }

    public void readFromFeed(InputFeed feed) {
        // Feed data from other program. Need to write new methods to read these data from files if MitoModel is used as
        // stand-alone program.
        setZonesFromFeed(feed.zones, feed.retailEmplByZone, feed.officeEmplByZone, feed.otherEmplByZone, feed.totalEmplByZone, feed.sizeOfZonesInAcre);
        dataSet.setAutoTravelTimes(feed.autoTravelTimes);
        dataSet.setTransitTravelTimes(feed.transitTravelTimes);
        setHouseholdsFromFeed(feed.households);
        setPersonsFromFeed(feed.persons);
        // todo: the household travel survey should not be read every year the model runs, but only in the first year.
        // todo: It was difficult, however, to get this to work with Travis-CI, not sure why (RM, 25-Mar-2017)
    }

    private void setZonesFromFeed(int[] zoneIds, int[] retailEmplByZone, int[] officeEmplByZone, int[] otherEmplByZone, int[] totalEmplByZone, float[] sizeOfZonesInAcre) {
        Map<Integer, Zone> zones = new HashMap<>();
        for (int i = 0; i < zoneIds.length; i++) {
            Zone zone = new Zone(zoneIds[i], sizeOfZonesInAcre[i]);
            zone.setRetailEmpl(retailEmplByZone[i]);
            zone.setOfficeEmpl(officeEmplByZone[i]);
            zone.setOtherEmpl(otherEmplByZone[i]);
            zone.setTotalEmpl(totalEmplByZone[i]);
            zones.put(zone.getZoneId(), zone);
        }
        dataSet.getZones().putAll(zones);
    }

    private void setHouseholdsFromFeed(MitoHousehold[] householdsArray) {
        Map<Integer, MitoHousehold> households = new HashMap<>();
        for (MitoHousehold household : householdsArray) {
            households.put(household.getHhId(), household);
            if (dataSet.getZones().containsKey(household.getHomeZone())) {
                dataSet.getZones().get(household.getHomeZone()).addHousehold();
            } else {
                logger.error("Feeded household " + household.getHhId() + " refers to non-existing home zone "
                        + household.getHomeZone() + ". Household will not be considered in any zone.");
            }
        }
        dataSet.getHouseholds().putAll(households);
    }

    private void setPersonsFromFeed(MitoPerson[] personsArray) {
        Map<Integer, MitoPerson> persons = new HashMap<>();
        for (MitoPerson person : personsArray) {
            persons.put(person.getId(), person);
        }
        dataSet.getPersons().putAll(persons);
    }
}
