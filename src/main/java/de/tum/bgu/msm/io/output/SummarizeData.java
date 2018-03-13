package de.tum.bgu.msm.io.output;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import com.google.common.math.Stats;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.charts.Histogram;
import de.tum.bgu.msm.util.charts.PieChart;
import de.tum.bgu.msm.util.charts.ScatterPlot;

import java.io.PrintWriter;
import java.util.*;

/**
 * Methods to summarize model results
 * Author: Ana Moreno, Munich
 * Created on 11/07/2017.
 */
public class SummarizeData {
    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(SummarizeData.class);


    public static void writeOutSyntheticPopulationWithTrips(DataSet dataSet) {
        LOGGER.info("  Writing household file");
        String filehh = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + Resources.INSTANCE.getString(Properties.HOUSEHOLDS) + "_t.csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filehh, false);
        pwh.println("id,zone,hhSize,autos,trips,workTrips");
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {
            pwh.print(hh.getId());
            pwh.print(",");
            pwh.print(hh.getHomeZone());
            pwh.print(",");
            pwh.print(hh.getHhSize());
            pwh.print(",");
            pwh.print(hh.getAutos());
            pwh.print(",");
            int totalNumber = 0;
            for(Purpose purpose: Purpose.values()) {
                totalNumber += hh.getTripsForPurpose(purpose).size();
            }
            pwh.print(totalNumber);
            pwh.print(",");
            pwh.println(hh.getTripsForPurpose(Purpose.HBW).size());
        }
        pwh.close();

        LOGGER.info("  Writing person file");
        String filepp = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + Resources.INSTANCE.getString(Properties.PERSONS) + "_t.csv";
        PrintWriter pwp = MitoUtil.openFileForSequentialWriting(filepp, false);
        pwp.println("id,hhID,hhSize,hhTrips,avTrips");
        for(MitoHousehold hh: dataSet.getHouseholds().values()) {
            for (MitoPerson pp : hh.getPersons().values()) {
                    pwp.print(pp.getId());
                    pwp.print(",");
                    pwp.print(hh.getId());
                    pwp.print(",");
                    pwp.print(hh.getHhSize());
                    pwp.print(",");
                    long hhTrips = Arrays.stream(Purpose.values()).mapToLong(purpose -> hh.getTripsForPurpose(purpose).size()).sum();
                    pwp.print(hhTrips);
                    pwp.print(",");
                    pwp.println(hhTrips / hh.getHhSize());
                }
            }
        pwp.close();
    }

    public static void writeOutTrips(DataSet dataSet) {
        LOGGER.info("  Writing trips file");
        String file = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/trips.csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(file, false);
        pwh.println("id,origin,destination,purpose,person,distance,time_auto,time_bus,time_train,time_tram_metro,mode");
        for (MitoTrip trip : dataSet.getTrips().values()) {
            pwh.print(trip.getId());
            pwh.print(",");
            MitoZone origin = trip.getTripOrigin();
            String originId = "null";
            if(origin != null) {
                originId = String.valueOf(origin.getId());
            }
            pwh.print(originId);
            pwh.print(",");
            MitoZone destination = trip.getTripDestination();
            String destinationId = "null";
            if(destination != null) {
                destinationId = String.valueOf(destination.getId());
            }
            pwh.print(destinationId);
            pwh.print(",");
            pwh.print(trip.getTripPurpose());
            pwh.print(",");
            pwh.print(trip.getPerson().getId());
            pwh.print(",");
            if(origin != null && destination != null) {
                double distance = dataSet.getTravelDistancesAuto().getTravelDistance(origin.getId(), destination.getId());
                pwh.print(distance);
                pwh.print(",");
                double time_auto = dataSet.getTravelTimes("car").getTravelTime(origin.getId(), destination.getId(), 0);
                pwh.print(time_auto);
                pwh.print(",");
                double time_bus = dataSet.getTravelTimes("bus").getTravelTime(origin.getId(), destination.getId(), 0);
                pwh.print(time_bus);
                pwh.print(",");
                double time_train = dataSet.getTravelTimes("train").getTravelTime(origin.getId(), destination.getId(), 0);
                pwh.print(time_train);
                pwh.print(",");
                double time_tram_metro = dataSet.getTravelTimes("tramMetro").getTravelTime(origin.getId(), destination.getId(), 0);
                pwh.print(time_tram_metro);
            } else {
                pwh.print("NA");
            }
            pwh.print(",");
            pwh.println(trip.getTripMode());
        }
        pwh.close();
    }


    private static void writeCharts(DataSet dataSet, Purpose purpose) {
        List<Double> travelTimes = new ArrayList<>();
        List<Double> travelDistances = new ArrayList<>();
        Map<Integer, List<Double>> distancesByZone = new HashMap<>();
        Multiset<MitoZone> tripsByZone = HashMultiset.create();
        SortedMultiset<Mode> modes = TreeMultiset.create();
        for (MitoTrip trip : dataSet.getTrips().values()) {
            if (trip.getTripPurpose() == purpose && trip.getTripOrigin() != null && trip.getTripDestination() != null) {
                travelTimes.add(dataSet.getTravelTimes("car").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour()));
                double travelDistance = dataSet.getTravelDistancesAuto().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId());
                travelDistances.add(travelDistance);
                tripsByZone.add(trip.getTripOrigin());
                if(distancesByZone.containsKey(trip.getTripOrigin().getId())){
                    distancesByZone.get(trip.getTripOrigin().getId()).add(travelDistance);
                } else {
                    List<Double> values = new ArrayList<>();
                    values.add(travelDistance);
                    distancesByZone.put(trip.getTripOrigin().getId(), values);
                }
                modes.add(trip.getTripMode());
            }
        }

        double[] travelTimesArray = new double[travelTimes.size()];
        int i = 0;
        for (Double value : travelTimes) {
            travelTimesArray[i] = value;
            i++;
        }

        double[] travelDistancesArray = new double[travelTimes.size()];
        i= 0;
        for(Double value: travelDistances) {
            travelDistancesArray[i] = value / 1000.;
            i++;
        }
        Histogram.createFrequencyHistogram(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/output/timeDistribution/tripTimeDistribution"+ purpose, travelTimesArray, "Travel Time Distribution " + purpose, "Time", "Frequency", 80, 1, 80);
        Histogram.createFrequencyHistogram(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/output/distanceDistribution/tripDistanceDistribution"+ purpose, travelDistancesArray, "Travel Distances Distribution " + purpose, "Distance", "Frequency", 100, 1, 100);

        PieChart.createPieChart(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/output/modeChoice/" + purpose, modes, "Mode Choice " + purpose);

        Map<Double, Double> averageDistancesByZone = new HashMap<>();
        for(Map.Entry<Integer, List<Double>> entry: distancesByZone.entrySet()) {
            averageDistancesByZone.put(Double.valueOf(entry.getKey()), Stats.meanOf(entry.getValue()));
        }
        PrintWriter pw1 = MitoUtil.openFileForSequentialWriting(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/output/distanceDistribution/tripsByZone"+purpose+".csv", false);
        pw1.println("id,number_trips");
        for(MitoZone zone: dataSet.getZones().values()) {
            pw1.println(zone.getId()+","+tripsByZone.count(zone));
        }
        pw1.close();
        PrintWriter pw = MitoUtil.openFileForSequentialWriting(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/output/distanceDistribution/averageZoneDistanceTable"+purpose+".csv", false);
        pw.println("id,avTripDistance");
        for(Map.Entry<Double, Double> entry: averageDistancesByZone.entrySet()) {
            pw.println(entry.getKey().intValue()+","+entry.getValue());
        }
        pw.close();
        ScatterPlot.createScatterPlot(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/output/distanceDistribution/averageZoneDistancePlot"+purpose, averageDistancesByZone, "Average Trip Distances by MitoZone", "MitoZone Id", "Average Trip Distance");

    }

    public static void writeCharts(DataSet dataSet) {
        for(Purpose purpose: Purpose.values()) {
            writeCharts(dataSet, purpose);
        }
    }
}
