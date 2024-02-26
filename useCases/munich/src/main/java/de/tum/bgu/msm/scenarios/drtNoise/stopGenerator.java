package de.tum.bgu.msm.scenarios.drtNoise;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.pt.transitSchedule.TransitScheduleFactoryImpl;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
class Stop {
    double lat;
    double lon;
    double bearing;

    public Stop(double lat,double lon, double bearing) {
        this.lat = lat;
        this.lon = lon;
        this.bearing = bearing;

    }

}

public class stopGenerator {
    ArrayList<Stop> Stops;
    public static void main(String args[]) throws IOException {

        File file = new File("/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/moia-msm/pt_stops_SA_DHDN.shp");
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")
        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84,
                "EPSG:31464");
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new MatsimNetworkReader(scenario.getNetwork()).readFile("/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/moia-msm/cleverShuttleOperationArea/croppedDenseNetwork.xml.gz");

        TransitScheduleFactory f = new TransitScheduleFactoryImpl();
        TransitSchedule schedule = f.createTransitSchedule();
        Integer counterCapacityBelowThousand = 0;
        Integer counter = 0;


        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                System.out.print(feature.getDefaultGeometryProperty().getBounds().getMinX());
                System.out.print("    ");
                System.out.println(feature.getDefaultGeometryProperty().getBounds().getMinY());
                System.out.println(feature.getAttribute(4));


                Double xCoord = feature.getDefaultGeometryProperty().getBounds().getMinX();
                Double yCoord = feature.getDefaultGeometryProperty().getBounds().getMinY();
                String stopName = feature.getAttribute(4).toString();
                Coord stopCoord = new Coord(xCoord, yCoord);
                stopCoord= ct.transform(stopCoord);
                Link forwardLink = NetworkUtils.getNearestLink(scenario.getNetwork(), stopCoord);
                System.out.print(forwardLink.getToNode().getCoord().getX());
                System.out.println(forwardLink.getToNode().getCoord().getY());

                Link backwardLink = NetworkUtils.findLinkInOppositeDirection(forwardLink);


                Id<TransitStopFacility> forwardKey = Id.create(stopName, TransitStopFacility.class);
                Id<TransitStopFacility> backwardKey = Id.create(stopName+"_back", TransitStopFacility.class);

                if (!schedule.getFacilities().containsKey(forwardKey)) {
                    TransitStopFacility forwardStop = f.createTransitStopFacility(forwardKey, forwardLink.getCoord(),
                            false);
                    forwardStop.setLinkId(forwardLink.getId());
                    schedule.addStopFacility(forwardStop);
                    counter++;
                    if(forwardLink.getCapacity() <800){
                        counterCapacityBelowThousand++;
                    }
                }
                if (backwardLink != null) {
                    if (!schedule.getFacilities().containsKey(backwardKey)) {
                    TransitStopFacility backwardStop = f.createTransitStopFacility(backwardKey, backwardLink.getCoord(),
                            false);
                    backwardStop.setLinkId(backwardLink.getId());

                    schedule.addStopFacility(backwardStop);
                    counter++;

                    if(backwardLink.getCapacity() <800){
                        counterCapacityBelowThousand++;
                    }
                    }

                }
            }
        }


        System.out.println("below 800: "+counterCapacityBelowThousand);
        System.out.println("total: "+ counter);
        new TransitScheduleWriter(schedule).writeFile("/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/moia-msm/cleverShuttleOperationArea/stops_pt.xml");
        System.out.println("Stops written");


    }

}
