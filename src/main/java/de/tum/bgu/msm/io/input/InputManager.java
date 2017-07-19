package de.tum.bgu.msm.io.input;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.io.input.readers.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nico on 14.07.2017.
 */
public class InputManager {

    private static Logger logger = Logger.getLogger(InputManager.class);

    private final DataSet dataSet;

    public InputManager(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void readAsStandAlone() {
        new TravelSurveyReader(dataSet).read();
        new ZonesReader(dataSet).read();
        new RegionsReader(dataSet).read();
        new SkimsReader(dataSet).read();
        new DistancesReader(dataSet).read();
        new HouseholdsReader(dataSet).read();
        new PersonsReader(dataSet).read();
        new JobReader(dataSet).read();
        new EmploymentReader(dataSet).read();
        new TripAttractionRatesReader(dataSet).read();
    }

    public void readAdditionalData() {
        dataSet.setPurposes(Properties.getArray(Properties.PURPOSES));
        new SchoolEnrollmentReader(dataSet).read();
        new RegionsReader(dataSet).read();
    }

    public void readFromFeed(int[] zones, Matrix autoTravelTimes, Matrix transitTravelTimes, MitoHousehold[] households, MitoPerson[] persons, int[] retailEmplByZone, int[] officeEmplByZone, int[] otherEmplByZone, int[] totalEmplByZone, float[] sizeOfZonesInAcre) {
        // Feed data from other program. Need to write new methods to read these data from files if MitoModel is used as
        // stand-alone program.
        setZonesFromFeed(zones, retailEmplByZone, officeEmplByZone, otherEmplByZone, totalEmplByZone, sizeOfZonesInAcre);
        dataSet.setAutoTravelTimes(autoTravelTimes);
        dataSet.setTransitTravelTimes(transitTravelTimes);
        setHouseholdsFromFeed(households);
        setPersonsFromFeed(persons);
        // todo: the household travel survey should not be read every year the model runs, but only in the first year.
        // todo: It was difficult, however, to get this to work with Travis-CI, not sure why (RM, 25-Mar-2017)
        new TravelSurveyReader(dataSet).read();
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
        dataSet.setZones(zones);
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
        dataSet.setHouseholds(households);
    }

    private void setPersonsFromFeed(MitoPerson[] personsArray) {
        Map<Integer, MitoPerson> persons = new HashMap<>();
        for (MitoPerson person : personsArray) {
            persons.put(person.getId(), person);
        }
        dataSet.setPersons(persons);
    }
}
