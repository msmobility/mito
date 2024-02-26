package de.tum.bgu.msm.scenarios.drtNoise;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
class Point
{
    double x, y;

    Point()
    {}

    Point(double p, double q)
    {
        x = p;
        y = q;
    }
}
public class AdjustPopulation {

    public static void main(String[] args ) throws IOException {
//        StreamingPopulationReader reader = new StreamingPopulationReader()

        Point[] polygon = getPolygonFromShape ("/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/cleverShuttleOperationArea/cleverShuttle.shp");
        int n = polygon.length;

//        for (int a =0; a<106;a++) {
//            System.out.println(polygon[a].x+"  "+polygon[a].y);
//        }
        Config config = ConfigUtils.loadConfig( "/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/moia-msm/cleverShuttleOperationArea/mito_assignment.output_config.xml") ;

        Integer numberOfDrtUsers = 0;
        Integer numberOfDrtRides = 0;
        Integer numberOfPlans = 0;
        Scenario scenario = ScenarioUtils.loadScenario( config ) ;

        Boolean insideServiceArea;
        for(Person person : scenario.getPopulation().getPersons().values() ){

            for(Plan plan : person.getPlans() ){
            numberOfPlans++;

//                System.out.println("######"+plan.getPerson()+"######");

                insideServiceArea = true;
//                for( PlanElement planElement : plan.getPlanElements() ){
//////
////
////                    if (planElement instanceof Activity){
////                        Coord coord = ((Activity) planElement).getCoord();
////                        Point origin = new Point(coord.getX(),coord.getY());
////                        if (!isInside(polygon,n,origin)){
////                            insideServiceArea = false;
////                        }
////
////                    }
////                }
                if (insideServiceArea) {
                    for (PlanElement planElement : plan.getPlanElements()) {

                        if (planElement instanceof Leg) {

                            ((Leg) planElement).setMode("drt");
                            numberOfDrtRides++;
                        }
                    }
                    numberOfDrtUsers++;
                }
            }
        }
        System.out.println("numberOfDrtUsers: "+numberOfDrtUsers);
        System.out.println("numberOfDrtRides: "+numberOfDrtRides);
        System.out.println("numberOfPlans: "+numberOfPlans);

//        PopulationWriter populationWriter = new PopulationWriter(scenario.getPopulation(), scenario.getNetwork());
//        populationWriter.write("/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/moia-msm/cleverShuttleOperationArea/drtPopulation.xml.gz");
        System.out.println("done");

    }
    public static Point[] getPolygonFromShape (String shapeLocation) throws IOException {
        File file = new File(shapeLocation);
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        Point polygon2[] = new Point[106];
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                System.out.print(feature.getID());
                System.out.print(": ");
                System.out.println(feature.getDefaultGeometryProperty().getValue());
                MultiPolygon mls = (MultiPolygon) feature.getDefaultGeometryProperty().getValue();
                List<Point> polygon = new LinkedList<Point>();

                Integer i =0;

                for(Coordinate a : mls.getCoordinates()){

                    polygon.add(new Point (mls.getCoordinates()[i].x,mls.getCoordinates()[i].y));
                    polygon2[i] =  new Point(mls.getCoordinates()[i].x, mls.getCoordinates()[i].y);
                    i++;
                }


                System.out.println(polygon.size());
                System.out.println(polygon);



            }
        }
        return polygon2;
    }

    public static boolean onSegment(Point p, Point q, Point r)
    {
        if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x)
                && q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y))
            return true;
        return false;
    }

    public static int orientation(Point p, Point q, Point r)
    {
        double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);

        if (val == 0)
            return 0;
        return (val > 0) ? 1 : 2;
    }

    public static boolean doIntersect(Point p1, Point q1, Point p2, Point q2)
    {

        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        if (o1 != o2 && o3 != o4)
            return true;

        if (o1 == 0 && onSegment(p1, p2, q1))
            return true;

        if (o2 == 0 && onSegment(p1, q2, q1))
            return true;

        if (o3 == 0 && onSegment(p2, p1, q2))
            return true;

        if (o4 == 0 && onSegment(p2, q1, q2))
            return true;

        return false;
    }

    public static boolean isInside(Point polygon[], int n, Point p)
    {
        int INF = 10000;
        if (n < 3)
            return false;

        Point extreme = new Point(INF, p.y);

        int count = 0, i = 0;
        do
        {
            int next = (i + 1) % n;
            if (doIntersect(polygon[i], polygon[next], p, extreme))
            {
                if (orientation(polygon[i], p, polygon[next]) == 0)
                    return onSegment(polygon[i], p, polygon[next]);

                count++;
            }
            i = next;
        } while (i != 0);

        return (count & 1) == 1 ? true : false;
    }

}
