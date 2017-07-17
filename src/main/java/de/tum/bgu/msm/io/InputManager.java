package de.tum.bgu.msm.io;

import com.pb.common.matrix.Matrix;
import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Zone;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by Nico on 14.07.2017.
 */
public class InputManager {

    private static Logger logger = Logger.getLogger(InputManager.class);

    public static final String PROPERTIES_ZONAL_DATA_FILE = "zonal.data.file";
    public static final String PROPERTIES_AUTO_PEAK_SKIM = "auto.peak.sov.skim";
    public static final String PROPERTIES_TRANSIT_PEAK_SKIM = "transit.peak.time";
    public static final String PROPERTIES_HH_FILE_ASCII = "household.file.ascii";
    public static final String PROPERTIES_PP_FILE_ASCII = "person.file.ascii";
    public static final String PROPERTIES_JJ_FILE_ASCII = "job.file.ascii";
    public static final String PROPERTIES_EMPLOYMENT_FILE = "employment.forecast";
    public static final String PROPERTIES_SCHOOL_ENROLLMENT_FILE = "school.enrollment.data";
    public static final String PROPERTIES_DISTANCE_SKIM = "distanceODmatrix";

    private final DataSet dataSet;
    private final ResourceBundle resources;

    public InputManager(DataSet dataSet, ResourceBundle resources) {
        this.dataSet = dataSet;
        this.resources = resources;
    }

    public void readAsStandAlone() {
        new TravelSurveyReader(dataSet, resources).read();
        new ZonesReader(dataSet, resources).read();
        new RegionsReader(dataSet, resources).read();
        new SkimsReader(dataSet, resources).read();
        new DistancesReader(dataSet, resources).read();
        new HouseholdsReader(dataSet, resources).read();
        new PersonsReader(dataSet, resources).read();
        new JobReader(dataSet, resources).read();
        new EmploymentReader(dataSet, resources).read();
    }

    public void readAdditionalData() {
        dataSet.setPurposes(ResourceUtil.getArray(resources, "trip.purposes"));
        new SchoolEnrollmentReader(dataSet, resources).read();
        new RegionsReader(dataSet, resources).read();
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
        new TravelSurveyReader(dataSet, resources).read();
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
