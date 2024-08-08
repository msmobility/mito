package de.tum.bgu.msm.scenarios.tengos;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.math.Stats;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.output.SummarizeData;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.charts.Histogram;
import de.tum.bgu.msm.util.charts.ScatterPlot;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;

/**
 * Methods to summarize model results
 * Author: Ana Moreno, Munich
 * Created on 11/07/2017.
 */
public class SummarizeData7daysTengos {
    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(SummarizeData.class);

    public static void writeOutSyntheticPopulationWithTrips(DataSet dataSet) {

        LOGGER.info("  Writing household file");
        Path filehh = Resources.instance.getOutputHouseholdPath();
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filehh.toAbsolutePath().toString(), false);
        pwh.println("hh.id,hh.zone,hh.locX,hh.locY,hh.isModelled,hh.size,hh.children,hh.econStatus,hh.cars,hh.autosPerAdult,hh.urban");
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {
            final MitoZone homeZone = hh.getHomeZone();
            if(homeZone == null) {
                LOGGER.warn("Skipping household " + hh.getId() + " as no home zone is defined");
                break;
            }
            pwh.print(hh.getId());
            pwh.print(",");
            pwh.print(homeZone.getZoneId());
            pwh.print(",");
            pwh.print(hh.getHomeLocation().x);
            pwh.print(",");
            pwh.print(hh.getHomeLocation().y);
            pwh.print(",");
            pwh.print(hh.isModelled() ? 1 : 0);
            pwh.print(",");
            pwh.print(hh.getHhSize());
            pwh.print(",");
            pwh.print(hh.getChildrenForHousehold());
            pwh.print(",");
            pwh.print(hh.getEconomicStatus());
            pwh.print(",");
            pwh.print(hh.getAutos());
            pwh.print(",");
            pwh.print(Math.min((double) hh.getAutos() / (hh.getHhSize() - hh.getChildrenForHousehold()) , 1.0));
            pwh.print(",");
            pwh.println(hh.getHomeZone().getAreaTypeR().equals(AreaTypes.RType.RURAL) ? 0:1 );
        }
        pwh.close();

        LOGGER.info("  Writing person file");
        Path filepp = Resources.instance.getOutputPersonsPath();
        PrintWriter pwp = MitoUtil.openFileForSequentialWriting(filepp.toAbsolutePath().toString(), false);
        pwp.println("p.ID,hh.id,p.age,p.female,p.occupationStatus,p.driversLicense,p.ownBicycle,p.modeSet," +
                "p.trips,p.trips_HBW,p.trips_HBE,p.trips_HBS,p.trips_HBR,p.trips_HBO,p.trips_RRT,p.trips_NHBW,p.trips_NHBO,p.trips_AIRPORT");
        for(MitoHousehold hh: dataSet.getHouseholds().values()) {
            for (MitoPerson pp : hh.getPersons().values()) {
                    pwp.print(pp.getId());
                    pwp.print(",");
                    pwp.print(hh.getId());
                    pwp.print(",");
                    pwp.print(pp.getAge());
                    pwp.print(",");
                    pwp.print(pp.getMitoGender().equals(MitoGender.FEMALE) ? 1 : 0);
                    pwp.print(",");
                    pwp.print(pp.getMitoOccupationStatus());
                    pwp.print(",");
                    pwp.print(pp.hasDriversLicense());
                    pwp.print(",");
                    pwp.print(pp.getHasBicycle());
                    pwp.print(",");
                    pwp.print(((MitoPerson7days)pp).getModeSet().toString());
                    pwp.print(",");
                    pwp.print(pp.getTrips().size());
                    pwp.print(",");
                    pwp.print(pp.getTripsForPurpose(Purpose.HBW).size());
                    pwp.print(",");
                    pwp.print(pp.getTripsForPurpose(Purpose.HBE).size());
                    pwp.print(",");
                    pwp.print(pp.getTripsForPurpose(Purpose.HBS).size());
                    pwp.print(",");
                    pwp.print(pp.getTripsForPurpose(Purpose.HBR).size());
                    pwp.print(",");
                    pwp.print(pp.getTripsForPurpose(Purpose.HBO).size());
                    pwp.print(",");
                    pwp.print(pp.getTripsForPurpose(Purpose.RRT).size());
                    pwp.print(",");
                    pwp.print(pp.getTripsForPurpose(Purpose.NHBW).size());
                    pwp.print(",");
                    pwp.print(pp.getTripsForPurpose(Purpose.NHBO).size());
                    pwp.print(",");
                    pwp.println(pp.getTripsForPurpose(Purpose.AIRPORT).size());
                }
            }
        pwp.close();
    }

    public static void writeTrips(DataSet dataSet, PrintWriter pwh, Collection<MitoTrip> tripsToPrint) {
        pwh.println("hh.id,p.ID,t.id,origin,originPAZ,originX,originY,destination,destinationPAZ,destinationX,destinationY," +
                "t.purpose,t.distance,t.distance_auto,time_auto,time_bus,time_train,time_tram_metro,distanceMoped,mode,departure_day," +
                "departure_time,departure_time_return,coordinatedTrip");

        for(MitoTrip trip : tripsToPrint) {
            pwh.print(trip.getPerson().getHousehold().getId());
            pwh.print(",");
            pwh.print(trip.getPerson().getId());
            pwh.print(",");
            pwh.print(trip.getId());
            pwh.print(",");
            Location origin = trip.getTripOrigin();
            String originId = "null";
            if(origin != null) {
                originId = String.valueOf(origin.getZoneId());
            }
            pwh.print(originId);
            pwh.print(",");

            //TODO: moped related features need to be added  trip.getTripOriginMopedZoneId()
            pwh.print("null");
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
            pwh.print("null");
            pwh.print(",");

            if(destination instanceof MicroLocation){
                pwh.print(((MicroLocation) destination).getCoordinate().x);
                pwh.print(",");
                pwh.print(((MicroLocation) destination).getCoordinate().y);
                pwh.print(",");
            }else{
                if (Resources.instance.getBoolean(Properties.FILL_MICRO_DATA_WITH_MICROLOCATION, false) &&
                        destination != null){
                    if(((MitoTripTengos) trip).getDestinationCoord()!= null){
                        pwh.print(((MitoTripTengos) trip).getDestinationCoord().getX());
                        pwh.print(",");
                        pwh.print(((MitoTripTengos) trip).getDestinationCoord().getY());
                        pwh.print(",");
                    } else {
                        Coord coordinate = CoordUtils.createCoord(dataSet.getZones().get(trip.getTripDestination().getZoneId()).getRandomCoord(MitoUtil.getRandomObject()));
                        pwh.print(coordinate.getX());
                        pwh.print(",");
                        pwh.print(coordinate.getY());
                        pwh.print(",");
                    }
                } else {
                    pwh.print("null");
                    pwh.print(",");
                    pwh.print("null");
                    pwh.print(",");
                }
            }

            pwh.print(trip.getTripPurpose());
            pwh.print(",");
            if(origin != null && destination != null) {
                double distance = dataSet.getTravelDistancesNMT().getTravelDistance(origin.getZoneId(), destination.getZoneId());
                pwh.print(distance);
                pwh.print(",");
                double distanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(origin.getZoneId(), destination.getZoneId());
                pwh.print(distanceAuto);
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
                pwh.print("NA,NA,NA,NA,NA,NA");
            }
            pwh.print(",");
            //TODO:trip.getMopedTripDistance()
            pwh.print("null");
            pwh.print(",");
            pwh.print(trip.getTripMode());
            pwh.print(",");
            pwh.print(((MitoTrip7days)trip).getDepartureDay());
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
            pwh.print(",");
            pwh.println(((MitoTripTengos) trip).getCoordinatedTripId());
        }
    }

    public static void writeAllTrips(DataSet dataSet, String scenarioName) {
        String outputSubDirectory = "scenOutput/" + scenarioName + "/";

        LOGGER.info("  Writing trips file");
        String file = Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() + "/microData/trips.csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(file, false);
        Collection<MitoTrip> tripsToPrint = dataSet.getTrips().values(); //.stream().filter(trip -> trip.getTripPurpose().equals(Purpose.HBO)).collect(Collectors.toUnmodifiableList());
        writeTrips(dataSet,pwh,tripsToPrint);
        pwh.close();
    }

    public static void writeOutTripsByDayByMode(DataSet dataSet, String scenarioName, Day day, Mode mode, Collection<MitoTrip> tripsToPrint) {
        String outputSubDirectory = "scenOutput/" + scenarioName + "/";

        LOGGER.info("  Writing trips file (day " + day + ", mode " + mode + ")");
        String file = Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() + "/microData/trips_" + day + "_" + mode + ".csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(file, false);
        writeTrips(dataSet,pwh,tripsToPrint);
        pwh.close();
    }
    private static void writeCharts(DataSet dataSet, Purpose purpose, String scenarioName) {
        String outputSubDirectory = "scenOutput/" + scenarioName + "/";

        List<Double> travelTimes = new ArrayList<>();
//        List<Double> travelDistances = new ArrayList<>();
        Map<Integer, List<Double>> distancesByZone = new HashMap<>();
        Multiset<MitoZone> tripsByZone = HashMultiset.create();

        for (MitoTrip trip : dataSet.getTrips().values()) {
            final Location tripOrigin = trip.getTripOrigin();
            if (trip.getTripPurpose() == purpose && tripOrigin != null && trip.getTripDestination() != null) {
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
        Histogram.createFrequencyHistogram(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() + "/timeDistribution/tripTimeDistribution"+ purpose, travelTimesArray, "Travel Time Distribution " + purpose, "Time", "Frequency", 80, 0, 80);


        Map<Double, Double> averageDistancesByZone = new HashMap<>();
        for(Map.Entry<Integer, List<Double>> entry: distancesByZone.entrySet()) {
            averageDistancesByZone.put(Double.valueOf(entry.getKey()), Stats.meanOf(entry.getValue()));
        }
        PrintWriter pw1 = MitoUtil.openFileForSequentialWriting(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() + "/distanceDistribution/tripsByZone"+purpose+".csv", false);
        pw1.println("id,number_trips");
        for(MitoZone zone: dataSet.getZones().values()) {
            pw1.println(zone.getId()+","+tripsByZone.count(zone));
        }
        pw1.close();
        PrintWriter pw = MitoUtil.openFileForSequentialWriting(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() +  "/distanceDistribution/averageZoneDistanceTable"+purpose+".csv", false);
        pw.println("id,avTripDistance");
        for(Map.Entry<Double, Double> entry: averageDistancesByZone.entrySet()) {
            pw.println(entry.getKey().intValue()+","+entry.getValue());
        }
        pw.close();
        ScatterPlot.createScatterPlot(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() +  "/distanceDistribution/averageZoneDistancePlot"+purpose, averageDistancesByZone, "Average Trip Distances by MitoZone", "MitoZone Id", "Average Trip Distance");

    }

    public static void writeCharts(DataSet dataSet, String scenarioName) {
        for(Purpose purpose: Purpose.values()) {
            writeCharts(dataSet, purpose, scenarioName);
        }
    }

    public static void writeMatsimPlans(DataSet dataSet, String scenarioName) {
        LOGGER.info("  Writing matsim plans file");

        String outputSubDirectory = Resources.instance.getBaseDirectory() + "/scenOutput/" + scenarioName + "/"+ dataSet.getYear()+"/";

        Map<Day, Population> populationByDay = new HashMap<>();
        for (Person person : dataSet.getPopulation().getPersons().values()){
            Day day = Day.valueOf((String)person.getAttributes().getAttribute("day"));
            if(populationByDay.get(day)==null){
                populationByDay.put(day,PopulationUtils.createPopulation(ConfigUtils.createConfig()));
            }
            populationByDay.get(day).addPerson(person);
        }

        for (Day day : Day.values()){
            new PopulationWriter(populationByDay.get(day)).write(outputSubDirectory + "matsimPlans_" + day.toString() + ".xml.gz");
        }
    }
}
