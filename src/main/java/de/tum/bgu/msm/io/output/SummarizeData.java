package de.tum.bgu.msm.io.output;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.math.Stats;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.charts.Histogram;
import de.tum.bgu.msm.util.charts.ScatterPlot;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.PrintWriter;
import java.nio.file.Path;
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
        Path filehh = Resources.instance.getOutputHouseholdPath();
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filehh.toAbsolutePath().toString(), false);
        pwh.println("id,zone,homeX,homeY,hhSize,autos,trips,workTrips");
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {
            final MitoZone homeZone = hh.getHomeZone();
            if(homeZone == null) {
                LOGGER.warn("Skipping household " + hh.getId() + " as no home zone is defined");
                break;
            }
            pwh.print(hh.getId());
            pwh.print(",");
            pwh.print(homeZone);
            pwh.print(",");
            pwh.print(hh.getHomeLocation().x);
            pwh.print(",");
            pwh.print(hh.getHomeLocation().y);
            pwh.print(",");
            pwh.print(hh.getHhSize());
            pwh.print(",");
            pwh.print(hh.getAutos());
            pwh.print(",");
            int totalNumber = 0;
            for(Purpose activityPurpose: Purpose.values()) {
                totalNumber += hh.getTripsForPurpose(activityPurpose).size();
            }
            pwh.print(totalNumber);
            pwh.print(",");
            pwh.println(hh.getTripsForPurpose(Purpose.HBW).size());
        }
        pwh.close();

        LOGGER.info("  Writing person file");
        Path filepp = Resources.instance.getOutputPersonsPath();
        PrintWriter pwp = MitoUtil.openFileForSequentialWriting(filepp.toAbsolutePath().toString(), false);
        pwp.println("id,hhID,hhSize,hhTrips,avTrips");
        for(MitoHousehold hh: dataSet.getHouseholds().values()) {
            for (MitoPerson pp : hh.getPersons().values()) {
                    pwp.print(pp.getId());
                    pwp.print(",");
                    pwp.print(hh.getId());
                    pwp.print(",");
                    pwp.print(hh.getHhSize());
                    pwp.print(",");
                    long hhTrips = Arrays.stream(Purpose.values()).mapToLong(activityPurpose -> hh.getTripsForPurpose(activityPurpose).size()).sum();
                    pwp.print(hhTrips);
                    pwp.print(",");
                    pwp.println(hhTrips / hh.getHhSize());
                }
            }
        pwp.close();
    }

    public static void writeOutTrips(DataSet dataSet, String scenarioName) {
        String outputSubDirectory = "scenOutput/" + scenarioName + "/";

        LOGGER.info("  Writing trips file");
        String file = Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() + "/microData/trips.csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(file, false);
        pwh.println("id,origin,originX,originY,destination,destinationX,destinationY,activityPurpose,person,distance,time_auto,time_bus,time_train,time_tram_metro,mode,departure_time,departure_time_return");
        for (MitoTrip trip : dataSet.getTrips().values()) {
            pwh.print(trip.getId());
            pwh.print(",");
            Location origin = trip.getTripOrigin();
            String originId = "null";
            if(origin != null) {
                originId = String.valueOf(origin.getZoneId());
            }
            pwh.print(originId);
            pwh.print(",");

            if(origin instanceof MicroLocation){
                pwh.print(((MicroLocation) origin).getCoordinate().x);
                pwh.print(",");
                pwh.print(((MicroLocation) origin).getCoordinate().y);
                pwh.print(",");
            } else{
                if (Resources.instance.getBoolean(Properties.FILL_MICRO_DATA_WITH_MICROLOCATION, false) &&
                        origin != null){
                    Coord coordinate = CoordUtils.createCoord(dataSet.getZones().get(trip.getTripOrigin().getZoneId()).getRandomCoord(MitoUtil.getRandomObject()));
                    pwh.print(coordinate.getX());
                    pwh.print(",");
                    pwh.print(coordinate.getY());
                    pwh.print(",");
                } else {
                    pwh.print("null");
                    pwh.print(",");
                    pwh.print("null");
                    pwh.print(",");
                }
            }

            Location destination = trip.getTripDestination();
            String destinationId = "null";
            if(destination != null) {
                destinationId = String.valueOf(destination.getZoneId());
            }
            pwh.print(destinationId);
            pwh.print(",");
            if(destination instanceof MicroLocation){
                pwh.print(((MicroLocation) destination).getCoordinate().x);
                pwh.print(",");
                pwh.print(((MicroLocation) destination).getCoordinate().y);
                pwh.print(",");
            }else{
                if (Resources.instance.getBoolean(Properties.FILL_MICRO_DATA_WITH_MICROLOCATION, false) &&
                        destination != null){
                    Coord coordinate = CoordUtils.createCoord(dataSet.getZones().get(trip.getTripDestination().getZoneId()).getRandomCoord(MitoUtil.getRandomObject()));
                    pwh.print(coordinate.getX());
                    pwh.print(",");
                    pwh.print(coordinate.getY());
                    pwh.print(",");
                } else {
                    pwh.print("null");
                    pwh.print(",");
                    pwh.print("null");
                    pwh.print(",");
                }
            }

            pwh.print(trip.getTripPurpose());
            pwh.print(",");
            pwh.print(trip.getPerson().getId());
            pwh.print(",");
            if(origin != null && destination != null) {
                double distance = dataSet.getTravelDistancesAuto().getTravelDistance(origin.getZoneId(), destination.getZoneId());
                pwh.print(distance);
                pwh.print(",");
                double timeAuto = dataSet.getTravelTimes().getTravelTime(origin, destination, dataSet.getPeakHour(), "car");
                pwh.print(timeAuto);
                pwh.print(",");
                double timeBus = dataSet.getTravelTimes().getTravelTime(origin, destination, dataSet.getPeakHour(), "bus");
                pwh.print(timeBus);
                pwh.print(",");
                double timeTrain = dataSet.getTravelTimes().getTravelTime(origin, destination, dataSet.getPeakHour(), "train");
                pwh.print(timeTrain);
                pwh.print(",");
                double timeTramMetro = dataSet.getTravelTimes().getTravelTime(origin, destination, dataSet.getPeakHour(), "tramMetro");
                pwh.print(timeTramMetro);
            } else {
                pwh.print("NA,NA,NA,NA,NA");
            }
            pwh.print(",");
            pwh.print(trip.getTripMode());
            pwh.print(",");
            pwh.print(trip.getDepartureInMinutes());
            int departureOfReturnTrip = trip.getDepartureInMinutesReturnTrip();
            if (departureOfReturnTrip != -1){
                pwh.print(",");
                pwh.println(departureOfReturnTrip);
            } else {
                pwh.print(",");
                pwh.println("NA");
            }

        }
        pwh.close();
    }


    private static void writeCharts(DataSet dataSet, Purpose activityPurpose, String scenarioName) {
        String outputSubDirectory = "scenOutput/" + scenarioName + "/";

        List<Double> travelTimes = new ArrayList<>();
//        List<Double> travelDistances = new ArrayList<>();
        Map<Integer, List<Double>> distancesByZone = new HashMap<>();
        Multiset<MitoZone> tripsByZone = HashMultiset.create();

        for (MitoTrip trip : dataSet.getTrips().values()) {
            final Location tripOrigin = trip.getTripOrigin();
            if (trip.getTripPurpose() == activityPurpose && tripOrigin != null && trip.getTripDestination() != null) {
                travelTimes.add(dataSet.getTravelTimes().getTravelTime(tripOrigin, trip.getTripDestination(), dataSet.getPeakHour(), "car"));
                double travelDistance = dataSet.getTravelDistancesAuto().getTravelDistance(tripOrigin.getZoneId(), trip.getTripDestination().getZoneId());
//                travelDistances.add(travelDistance);
                tripsByZone.add(dataSet.getZones().get(tripOrigin.getZoneId()));
                if(distancesByZone.containsKey(tripOrigin.getZoneId())){
                    distancesByZone.get(tripOrigin.getZoneId()).add(travelDistance);
                } else {
                    List<Double> values = new ArrayList<>();
                    values.add(travelDistance);
                    distancesByZone.put(tripOrigin.getZoneId(), values);
                }
            }
        }

        double[] travelTimesArray = new double[travelTimes.size()];
        int i = 0;
        for (Double value : travelTimes) {
            travelTimesArray[i] = value;
            i++;
        }

//        double[] travelDistancesArray = new double[travelTimes.size()];
//        i= 0;
//        for(Double value: travelDistances) {
//            travelDistancesArray[i] = value;
//            i++;
//        }
        Histogram.createFrequencyHistogram(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() + "/timeDistribution/tripTimeDistribution"+ activityPurpose, travelTimesArray, "Travel Time Distribution " + activityPurpose, "Time", "Frequency", 80, 0, 80);


        Map<Double, Double> averageDistancesByZone = new HashMap<>();
        for(Map.Entry<Integer, List<Double>> entry: distancesByZone.entrySet()) {
            averageDistancesByZone.put(Double.valueOf(entry.getKey()), Stats.meanOf(entry.getValue()));
        }
        PrintWriter pw1 = MitoUtil.openFileForSequentialWriting(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() + "/distanceDistribution/tripsByZone"+activityPurpose+".csv", false);
        pw1.println("id,number_trips");
        for(MitoZone zone: dataSet.getZones().values()) {
            pw1.println(zone.getId()+","+tripsByZone.count(zone));
        }
        pw1.close();
        PrintWriter pw = MitoUtil.openFileForSequentialWriting(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() +  "/distanceDistribution/averageZoneDistanceTable"+activityPurpose+".csv", false);
        pw.println("id,avTripDistance");
        for(Map.Entry<Double, Double> entry: averageDistancesByZone.entrySet()) {
            pw.println(entry.getKey().intValue()+","+entry.getValue());
        }
        pw.close();
        ScatterPlot.createScatterPlot(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() +  "/distanceDistribution/averageZoneDistancePlot"+activityPurpose, averageDistancesByZone, "Average Trip Distances by MitoZone", "MitoZone Id", "Average Trip Distance");

    }

    public static void writeCharts(DataSet dataSet, String scenarioName) {
        for(Purpose activityPurpose: Purpose.values()) {
            writeCharts(dataSet, activityPurpose, scenarioName);
        }
    }

    public static void writeMatsimPlans(DataSet dataSet, String scenarioName) {
        LOGGER.info("  Writing matsim plans file");

        String outputSubDirectory = Resources.instance.getBaseDirectory() + "/scenOutput/" + scenarioName + "/"+ dataSet.getYear()+"/";
        new PopulationWriter(dataSet.getPopulation()).write(outputSubDirectory + "matsimPlans.xml.gz");
    }
}
