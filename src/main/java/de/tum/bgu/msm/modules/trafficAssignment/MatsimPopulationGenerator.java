package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import org.apache.log4j.Logger;
import com.vividsolutions.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MatsimPopulationGenerator {

    private static final Logger logger = Logger.getLogger(MatsimPopulationGenerator.class);

    Set<Mode> modeSet = new HashSet<>();

    public MatsimPopulationGenerator() {
        String[] networkModes = Resources.INSTANCE.getArray(Properties.MATSIM_NETWORK_MODES, new String[]{"autoDriver"});
        String[] teleportedModes = Resources.INSTANCE.getArray(Properties.MATSIM_TELEPORTED_MODES, new String[]{});
        for (String mode : networkModes){
            modeSet.add(Mode.valueOf(mode));
        }
        for (String mode : teleportedModes){
            modeSet.add(Mode.valueOf(mode));
        }
    }
    //private Map<Integer,SimpleFeature> zoneFeatureMap = new HashMap<>();


    public static Map<Integer,SimpleFeature> loadZoneShapeFile(){
        Map<Integer,SimpleFeature> zoneFeatureMap = new HashMap<>();
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(Resources.INSTANCE.getString(Properties.ZONE_SHAPEFILE))) {
            int zoneId = Integer.parseInt(feature.getAttribute(Resources.INSTANCE.getString(Properties.ZONE_SHAPEFILE_ID_FIELD)).toString());
            zoneFeatureMap.put(zoneId,feature);
        }
        return zoneFeatureMap;
    }

    public Population generateMatsimPopulation(DataSet dataSet, Config config){
        Population population = PopulationUtils.createPopulation(config);
        PopulationFactory factory = population.getFactory();
        AtomicInteger assignedTripCounter = new AtomicInteger(0);
        AtomicInteger nonAssignedTripCounter = new AtomicInteger(0);
        dataSet.getTripSubsample().values().forEach(trip ->{
            try {
                if (modeSet.contains(trip.getTripMode())) {
                    Person person = factory.createPerson(Id.createPersonId(trip.getId()));
                    trip.setMatsimPerson(person);

                    Plan plan = factory.createPlan();
                    person.addPlan(plan);
                    population.addPerson(person);

                    String activityTypeAtOrigin = getOriginActivity(trip);

                    Coord originCoord;
                    if(trip.getTripOrigin() instanceof MicroLocation) {
                        originCoord = CoordUtils.createCoord(((MicroLocation) trip.getTripOrigin()).getCoordinate().x,
                        		((MicroLocation) trip.getTripOrigin()).getCoordinate().y);
                    } else {
                    	Coordinate randCoord = dataSet.getZones().get(trip.getTripOrigin().getZoneId()).getRandomCoord();
                        originCoord = CoordUtils.createCoord(randCoord.x, randCoord.y);
                    }

                    Activity originActivity = factory.createActivityFromCoord(activityTypeAtOrigin, originCoord);
                    originActivity.setEndTime(trip.getDepartureInMinutes() * 60 + MitoUtil.getRandomObject().nextDouble() * 60);
                    plan.addActivity(originActivity);

                    //plan.addLeg(factory.createLeg(Mode.getMatsimMode(trip.getTripMode())));
                    plan.addLeg(factory.createLeg("uam")); // TODO FOR TESTING ONLY REMOVE LATER - RR

                    String activityTypeAtDestination = getDestinationActivity(trip);

                    Coord destinationCoord;
                    if(trip.getTripDestination() instanceof MicroLocation) {
                    	Coordinate rand = ((MicroLocation) trip.getTripDestination()).getCoordinate();
                        destinationCoord = CoordUtils.createCoord(rand.x, rand.y);
                    } else {
                    	Coordinate rand = dataSet.getZones().get(trip.getTripDestination().getZoneId()).getRandomCoord();
                        destinationCoord = CoordUtils.createCoord(rand.x, rand.y);
                    }
                    Activity destinationActivity = factory.createActivityFromCoord(activityTypeAtDestination, destinationCoord);

                    if (trip.isHomeBased()) {
                        destinationActivity.setEndTime(trip.getDepartureInMinutesReturnTrip() * 60 + MitoUtil.getRandomObject().nextDouble() * 60);
                        plan.addActivity(destinationActivity);
                        plan.addLeg(factory.createLeg(Mode.getMatsimMode(trip.getTripMode())));
                        plan.addActivity(factory.createActivityFromCoord(activityTypeAtOrigin, originCoord));
                    } else {
                        plan.addActivity(destinationActivity);
                    }

                }
            } catch (Exception e){
                nonAssignedTripCounter.incrementAndGet();
            }

            if (ConcurrencyUtils.isPowerOf2(assignedTripCounter.incrementAndGet())){
                logger.warn( assignedTripCounter.get()  + " MATSim agents created");
            }

        });
        logger.warn( nonAssignedTripCounter.get()  + " trips do not have trip origin, destination or mode and cannot be assigned in MATSim");
        return population;
    }


    public static String getOriginActivity(MitoTrip trip){
        Purpose purpose = trip.getTripPurpose();
        if (purpose.equals(Purpose.NHBW)){
            return "work";
        } else if (purpose.equals(Purpose.NHBO)){
            return "other";
        } else if (purpose.equals(Purpose.AIRPORT)) {
            if (trip.getTripOrigin().getZoneId() == Resources.INSTANCE.getInt(Properties.AIRPORT_ZONE)){
                return "airport";
            } else {
                return "home";
            }
        } else {
            return "home";
        }
    }

    public static String getDestinationActivity(MitoTrip trip){
        Purpose purpose = trip.getTripPurpose();
        if (purpose.equals(Purpose.HBW)){
            return "work";
        } else if (purpose.equals(Purpose.HBE)){
            return "education";
        } else if (purpose.equals(Purpose.HBS)){
            return "shopping";
        } else if (purpose.equals(Purpose.AIRPORT)) {
            if (trip.getTripDestination().getZoneId() == Resources.INSTANCE.getInt(Properties.AIRPORT_ZONE)) {
                return "airport";
            } else {
                return "home";
            }
        } else {
            return "other";
        }
    }






}
