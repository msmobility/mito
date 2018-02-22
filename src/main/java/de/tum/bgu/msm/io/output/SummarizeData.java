package de.tum.bgu.msm.io.output;

import com.google.common.math.Stats;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.charts.Histogram;
import de.tum.bgu.msm.util.charts.ScatterPlot;

import java.io.PrintWriter;
import java.util.*;

/**
 * Methods to summarize model results
 * Author: Ana Moreno, Munich
 * Created on 11/07/2017.
 */


public class SummarizeData {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SummarizeData.class);


    public static void writeOutSyntheticPopulationWithTrips(DataSet dataSet) {
        logger.info("  Writing household file");
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

        logger.info("  Writing person file");
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
                    long hhTrips = Arrays.asList(Purpose.values()).stream().flatMap(purpose -> hh.getTripsForPurpose(purpose).stream()).count();
                    pwp.print(hhTrips);
                    pwp.print(",");
                    pwp.println(hhTrips / hh.getHhSize());
                }
            }
        pwp.close();
    }


    private static void writeCharts(DataSet dataSet, Purpose purpose) {
        List<Double> travelTimes = new ArrayList<>();
        List<Double> travelDistances = new ArrayList<>();
        Map<Integer, List<Double>> distancesByZone = new HashMap<>();
        for (MitoTrip trip : dataSet.getTrips().values()) {
            if (trip.getTripPurpose() == purpose && trip.getTripOrigin() != null && trip.getTripDestination() != null) {
                travelTimes.add(dataSet.getTravelTimes("car").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour()));
                double travelDistance = dataSet.getTravelDistancesAuto().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId());
                travelDistances.add(travelDistance);
                if(distancesByZone.containsKey(trip.getTripOrigin().getId())){
                    distancesByZone.get(trip.getTripOrigin().getId()).add(travelDistance);
                } else {
                    List<Double> values = new ArrayList<>();
                    values.add(travelDistance);
                    distancesByZone.put(trip.getTripOrigin().getId(), values);
                }
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

        Map<Double, Double> averageDistancesByZone = new HashMap<>();
        for(Map.Entry<Integer, List<Double>> entry: distancesByZone.entrySet()) {
            averageDistancesByZone.put(Double.valueOf(entry.getKey()), Stats.meanOf(entry.getValue()));
        }
        PrintWriter pw = MitoUtil.openFileForSequentialWriting(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/output/distanceDistribution/averageZoneDistanceTable"+purpose+".csv", false);
        pw.println("id,avTripDistance");
        for(Map.Entry<Double, Double> entry: averageDistancesByZone.entrySet()) {
            pw.println(entry.getKey().intValue()+","+entry.getValue());
        }
        pw.close();
        ScatterPlot.createScatterPlot(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/output/distanceDistribution/averageZoneDistancePlot"+purpose, averageDistancesByZone, "Average Trip Distances by MitoZone", "MitoZone Id", "Average Trip Distance");

    }

    public static void writeHistograms(DataSet dataSet) {
        for(Purpose purpose: Purpose.values()) {
            writeCharts(dataSet, purpose);
        }
    }
}
