package de.tum.bgu.msm.run.scenarios.drtNoise;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.GeometryUtils;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.Collection;

public class CropNetwork {


    public static void main(String[] args) {

        final Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures("serviceArea.shp");
        final SimpleFeature feature = features.iterator().next();
        final Geometry initialGeometry = (Geometry) feature.getDefaultGeometry();

        PreparedGeometry geometry = PreparedGeometryFactory.prepare(initialGeometry);

        Network fullNetwork = NetworkUtils.createNetwork();
        new MatsimNetworkReader(fullNetwork).readFile("network.xml");

        Network croppedNetwork = NetworkUtils.createNetwork();

        for(Link link: fullNetwork.getLinks().values()) {
            if(isWithinArea(geometry, link)) {
                croppedNetwork.addLink(link);
            }
        }

        new NetworkCleaner().run(croppedNetwork);
        new NetworkWriter(croppedNetwork).write("outputfile.xml");
    }

    private static boolean isWithinArea(PreparedGeometry geometry, Link link) {
        return link != null
                && isInArea(geometry, link.getFromNode().getCoord())
                && isInArea(geometry, link.getToNode().getCoord());
    }

    private static boolean isInArea(PreparedGeometry geometry, Coord coord) {
        return geometry.contains(GeometryUtils.createGeotoolsPoint(coord));
    }
}
