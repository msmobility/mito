package de.tum.bgu.msm.modules.trafficAssignment;

import com.vividsolutions.jts.geom.*;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.Map;

public class MatsimPopulationGenerator {

    private static final Logger logger = Logger.getLogger(MatsimPopulationGenerator.class);
    //private Map<Integer,SimpleFeature> zoneFeatureMap = new HashMap<>();


    public static Map<Integer,SimpleFeature> loadZoneShapeFile(){
        Map<Integer,SimpleFeature> zoneFeatureMap = new HashMap<>();
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(Resources.INSTANCE.getString(Properties.ZONE_SHAPEFILE))) {
            int zoneId = Integer.parseInt(feature.getAttribute(Resources.INSTANCE.getString(Properties.ZONE_SHAPEFILE_ID_FIELD)).toString());
            zoneFeatureMap.put(zoneId,feature);
        }

        return zoneFeatureMap;

    }

    public static Population generateMatsimPopulation(DataSet dataSet, Config config,  Map<Integer,SimpleFeature> zoneFeatureMap){
        Population population = PopulationUtils.createPopulation(config);
        PopulationFactory factory = population.getFactory();
        dataSet.getTripSubsample().values().forEach(trip ->{
            try {
                if (trip.getTripMode().equals(Mode.autoDriver)) {
                    Person person = factory.createPerson(Id.createPersonId(trip.getId()));
                    trip.setMatsimPerson(person);

                    Plan plan = factory.createPlan();
                    person.addPlan(plan);
                    population.addPerson(person);

                    String activityTypeAtOrigin = getOriginActivity(trip.getTripPurpose());
                    Coord originCoordinates = getRandomCoordinateInZone(zoneFeatureMap.get(trip.getTripOrigin().getId()));
                    Activity originActivity = factory.createActivityFromCoord(activityTypeAtOrigin, originCoordinates);
                    originActivity.setEndTime(trip.getDepartureInMinutes() * 60 + MitoUtil.getRandomObject().nextDouble() * 60);
                    plan.addActivity(originActivity);

                    plan.addLeg(factory.createLeg(TransportMode.car));

                    String activityTypeAtDestination = getDestinationActivity(trip.getTripPurpose());
                    Coord destinationCoordinates = getRandomCoordinateInZone(zoneFeatureMap.get(trip.getTripDestination().getId()));
                    Activity destinationActivity = factory.createActivityFromCoord(activityTypeAtDestination, destinationCoordinates);

                    if (trip.isHomeBased()) {
                        destinationActivity.setEndTime(trip.getDepartureInMinutesReturnTrip() * 60 + MitoUtil.getRandomObject().nextDouble() * 60);
                        plan.addActivity(destinationActivity);
                        plan.addLeg(factory.createLeg(TransportMode.car));
                        plan.addActivity(factory.createActivityFromCoord(activityTypeAtOrigin,originCoordinates));
                    } else {
                        plan.addActivity(destinationActivity);
                    }

                }
            } catch (Exception e){
                logger.info("The trip " + trip.getId() + " does not have trip mode.");
            }
        });
        return population;
    }


    public static String getOriginActivity(Purpose purpose){
        if (purpose.equals(Purpose.NHBW)){
            return "work";
        } else if (purpose.equals(Purpose.NHBO)){
            return "other";
        } else {
            return "home";
        }
    }

    public static String getDestinationActivity(Purpose purpose){
        if (purpose.equals(Purpose.HBW)){
            return "work";
        } else if (purpose.equals(Purpose.HBE)){
            return "education";
        } else if (purpose.equals(Purpose.HBS)){
            return "shopping";
        } else {
            return "other";
        }
    }

    //taken from silo matsim integration
    private final static GeometryFactory geometryFactory = new GeometryFactory();

    public static final Coord getRandomCoordinateInZone(SimpleFeature feature) {
        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        Envelope envelope = geometry.getEnvelopeInternal();
        while (true) {
            Point point = getRandomPointInEnvelope(envelope);
            if (point.within(geometry)) {
                return new Coord(point.getX(), point.getY());
            }
        }
    }

    public static final Point getRandomPointInEnvelope(Envelope envelope) {
        double x = envelope.getMinX() + MitoUtil.getRandomObject().nextDouble() * envelope.getWidth();
        double y = envelope.getMinY() + MitoUtil.getRandomObject().nextDouble() * envelope.getHeight();
        return geometryFactory.createPoint(new Coordinate(x,y));
    }


}
