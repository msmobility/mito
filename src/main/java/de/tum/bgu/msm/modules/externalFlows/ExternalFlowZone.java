package de.tum.bgu.msm.modules.externalFlows;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.shape.random.RandomPointsBuilder;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.opengis.feature.simple.SimpleFeature;

public class ExternalFlowZone {

    private int id;
    private Coord coordinates;
    private ExternalFlowZoneType zoneType;
    private SimpleFeature feature;

    public ExternalFlowZone(int id, Coord coordinates, ExternalFlowZoneType zoneType, SimpleFeature feature) {
        this.id = id;
        this.coordinates = coordinates;
        this.zoneType = zoneType;
        this.feature = feature;
    }

    public Coord getCoordinatesForTripGeneration(){
        if (zoneType.equals(ExternalFlowZoneType.BORDER)){
            return coordinates;
        } else /*if (zoneType.equals(ExternalFlowZoneType.BEZIRKE)) {
            double radii = Math.random() * 2000;
            //math.acos(-1) gets pi?
            double angle = Math.random() * 2 * Math.acos(-1);
            return new Coord( coordinates.getX() + radii * Math.cos(angle), coordinates.getY() + radii * Math.sin(angle));
        } else */{
            return getRandomCoord(feature);
        }

    }

    public ExternalFlowZoneType getZoneType() {
        return zoneType;
    }

    public Coord getRandomCoord(SimpleFeature feature) {
        // alternative and about 10 times faster way to generate random point inside a geometry. Amit Dec'17
        RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(new GeometryFactory());
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent((Geometry) feature.getDefaultGeometry());
        Coordinate coordinate = randomPointsBuilder.getGeometry().getCoordinates()[0];
        Point p = MGC.coordinate2Point(coordinate);
        return new Coord(p.getX(), p.getY());
    }
}
