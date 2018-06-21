package de.tum.bgu.msm.io.input;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.Map;

public class Input {

    private static final Logger logger = Logger.getLogger(Input.class);

    private final DataSet dataSet;

    public Input(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void readAsStandAlone() {
        dataSet.setTravelTimes(new SkimTravelTimes());
        new ZonesReader(dataSet).read();
        if (Resources.INSTANCE.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER)) {
            new BorderDampersReader(dataSet).read();
        }
        new SkimsReader(dataSet).read();
        new HouseholdsReader(dataSet).read();
        new PersonsReader(dataSet).read();
        new JobReader(dataSet).read();
    }

    public void readAdditionalData() {
        new SchoolEnrollmentReader(dataSet).read();
        new TripAttractionRatesReader(dataSet).read();
        new ModeChoiceInputReader(dataSet).read();
        new EconomicStatusReader(dataSet).read();
    }

    public void readFromFeed(InputFeed feed) {
        for(MitoZone zone: feed.zones.values()) {
            dataSet.addZone(zone);
        }
        dataSet.setTravelTimes(feed.travelTimes);
        setHouseholdsFromFeed(feed.households);
        readAdditionalData();
        readTravelDistances();
        ZonesReader.mapFeaturesToZones(dataSet);
    }


    public void readTravelDistances() {
        new SkimsReader(dataSet).readSkimDistances();
    }

    private void setHouseholdsFromFeed(Map<Integer, MitoHousehold> households) {
        for (MitoHousehold household : households.values()) {
            if (dataSet.getZones().containsKey(household.getHomeZone().getId())) {
                dataSet.getZones().get(household.getHomeZone().getId()).addHousehold();
            } else {
                throw new RuntimeException("Fed household " + household.getId() + " refers to non-existing home zone "
                        + household.getHomeZone());
            }
            dataSet.addHousehold(household);
            for(MitoPerson person: household.getPersons().values()) {
                dataSet.addPerson(person);
            }
        }
    }

    public final static class InputFeed {

        private final Map<Integer, MitoZone> zones;
        private final TravelTimes travelTimes;
        private final Map<Integer, MitoHousehold> households;
        private Map<Integer, SimpleFeature> zoneFeatureMap;

        public InputFeed(Map<Integer, MitoZone> zones, TravelTimes travelTimes, Map<Integer, MitoHousehold> households,
                         Map<Integer,SimpleFeature> zoneFeatureMap) {
            this.zones = zones;
            this.travelTimes = travelTimes;
            this.households = households;
            this.zoneFeatureMap = zoneFeatureMap;
        }
    }
}
