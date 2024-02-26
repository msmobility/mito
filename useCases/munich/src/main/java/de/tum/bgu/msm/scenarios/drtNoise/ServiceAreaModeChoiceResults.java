package de.tum.bgu.msm.scenarios.drtNoise;

import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.charts.PieChart;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ServiceAreaModeChoiceResults {


    public static void printServiceAreaModeChoiceResults(DataSet dataSet, Geometry serviceArea, String scenarioName) {

        String outputSubDirectory = "scenOutput/" + scenarioName + "/";

        final PreparedGeometry preparedGeometry = PreparedGeometryFactory.prepare(serviceArea);


        Map<Purpose, List<MitoTrip>> tripsByPurpose = dataSet.getTrips().values().stream()
                .filter(trip -> {
                    try {
                        final Location tripOrigin = trip.getTripOrigin();
                        if (tripOrigin != null) {
                            final Geometry origin = dataSet.getZones().get(tripOrigin.getZoneId()).getGeometry();
                            final Geometry destination = dataSet.getZones().get(trip.getTripDestination().getZoneId()).getGeometry();
                            return trip.getTripMode() != null && preparedGeometry.contains(origin) && preparedGeometry.contains(destination);
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .collect(Collectors.groupingBy(MitoTrip::getTripPurpose));

        tripsByPurpose.forEach((purpose, trips) -> {
                    SortedMultiset<Mode> modes = TreeMultiset.create();
                    final long totalTrips = trips.size();
                    trips.parallelStream()
                            //group number of trips by mode
                            .collect(Collectors.groupingBy(MitoTrip::getTripMode, Collectors.counting()))
                            //calculate and add share to data set table
                            .forEach((mode, count) -> {
                                        modes.add(mode, count.intValue() );
                                    }
                            );
                    PieChart.createPieChart(Resources.instance.getBaseDirectory() + "/" + outputSubDirectory + dataSet.getYear() + "/modeChoice/" + purpose + "_serviceArea", modes, "Mode Choice " + purpose);
                }
        );

        SortedMultiset<Mode> modes = TreeMultiset.create();
        dataSet.getTrips().values().stream().filter(trip -> {
            try {
                final Location tripOrigin = trip.getTripOrigin();
                if (tripOrigin != null) {
                    final Geometry origin = dataSet.getZones().get(tripOrigin.getZoneId()).getGeometry();
                    final Geometry destination = dataSet.getZones().get(trip.getTripDestination().getZoneId()).getGeometry();
                    return trip.getTripMode() != null && preparedGeometry.contains(origin) && preparedGeometry.contains(destination);
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }).collect(Collectors.groupingBy(MitoTrip::getTripMode, Collectors.counting())).forEach((mode, count) -> modes.add(mode, count.intValue() ));
        PieChart.createPieChart(Resources.instance.getBaseDirectory() + "/" + outputSubDirectory + dataSet.getYear() + "/modeChoice/allPurposeServiceArea", modes, "Mode Choice all");
    }
}
