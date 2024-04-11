package de.tum.bgu.msm;

import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TripCSVToMATSimPlan {

    private static final double SPEED_WALK_KMH = 5.;
    private static final double SPEED_BICYCLE_KMH = 12.;
    // This class will read trip lists in CSV from MITO and creates a MATSim XML
    // plan file
    private static String delimiter = ",";

    private static String filename;
    private static PopulationFactory factory;
    private static Network carNetwork;

    private static double scaleFactor = 0.20;
    private static Random random = new Random(0);

    private static int posId;
    private static int posOriginX;
    private static int posOriginY;
    private static int posDestinationX;
    private static int posDestinationY;
    private static int posPurpose;
    private static int posPersonId;
    private static int posMode;
    private static int posDistance;
    private static int posTimeCar;
    private static int posTimeTrain;
    private static int posTimeMetroTram;
    private static int posTimeBus;
    private static int posDepartureTime;
    private static int posDepartureTimeReturn;

    private static Set<String> modesCar = new HashSet<>();
    private static Set<String> modesAssignment = new HashSet<>();

    public static void main(String[] args) {
        // TODO add logging
        filename = args[0];
        //String networkFile = args[1];

        Config config = ConfigUtils.createConfig();
        //config.network().setInputFile(networkFile);



        Scenario scenario = ScenarioUtils.loadScenario(config);
        TransportModeNetworkFilter filter = new TransportModeNetworkFilter(scenario.getNetwork());

        modesCar.add("car");
        modesAssignment.add("car");
        carNetwork = NetworkUtils.createNetwork();
        filter.filter(carNetwork, modesCar);

        Population population = PopulationUtils.createPopulation(config);
        factory = population.getFactory();

        try {
            FileReader in = null;
            BufferedReader br = null;
            try {
                in = new FileReader(filename);
                br = new BufferedReader(in);

                String line;
                int i = 0;
                String[] header = br.readLine().split(delimiter); // read CSV header to find names and positions

                posId = MitoUtil.findPositionInArray("id", header);
                posOriginX = MitoUtil.findPositionInArray("originX", header);
                posOriginY = MitoUtil.findPositionInArray("originY", header);
                posDestinationX = MitoUtil.findPositionInArray("destinationX", header);
                posDestinationY = MitoUtil.findPositionInArray("destinationY", header);
                posPurpose = MitoUtil.findPositionInArray("purpose", header);
                posPersonId = MitoUtil.findPositionInArray("person", header);
                posMode = MitoUtil.findPositionInArray("mode", header);
                posDistance = MitoUtil.findPositionInArray("distance", header);
                posTimeCar = MitoUtil.findPositionInArray("time_auto", header);
                posTimeTrain = MitoUtil.findPositionInArray("time_train", header);
                posTimeMetroTram = MitoUtil.findPositionInArray("time_tram_metro", header);
                posTimeBus = MitoUtil.findPositionInArray("time_bus", header);
                posDepartureTime = MitoUtil.findPositionInArray("departure_time", header);
                posDepartureTimeReturn = MitoUtil.findPositionInArray("departure_time_return", header);


                while ((line = br.readLine()) != null) {
                    Person p = createPersonFromTrip(i++, line);
                    if (p != null) {
                        population.addPerson(p);
                    }
                }
            } finally {
                if (br != null) {
                    br.close();
                }

                if (in != null) {
                    in.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        PopulationWriter popwriter = new PopulationWriter(population);
        popwriter.write("externalDemand/sd_trips" + ".xml.gz");

        System.out.println("done.");
    }

    private static Person createPersonFromTrip(int i, String line) {

        Trip t = new Trip(line);

        String mode = decodeMode(t.mode);

        if (mode.equals("null")) {
            return null;
            //not a valid trip record
        }

        if (random.nextDouble() > scaleFactor){
            return null;
        }

        if (!modesAssignment.contains(mode)){
            return null;
        }

        Id<Person> matsimId = Id.createPersonId(t.person + "_" + i);

        Person p = factory.createPerson(Id.createPersonId(matsimId));
        Plan plan = factory.createPlan();

        Purpose purpose = Purpose.valueOf(t.purpose);
        boolean roundTrip = !(purpose.equals(Purpose.NHBW) || purpose.equals(Purpose.NHBO));

        String firstActivityType = getOriginActivity(purpose);
        Coord firstCoord = new Coord(t.originX, t.originY);

        Activity firstAct = factory.createActivityFromCoord(firstActivityType, firstCoord);
        //firstAct.setLinkId(NetworkUtils.getNearestLink(carNetwork, firstCoord).getId());

        firstAct.setEndTime(t.departure_time);
        plan.addActivity(firstAct);

        Leg firstLeg = factory.createLeg(mode);
        firstLeg.setDepartureTime(t.departure_time);
        plan.addLeg(firstLeg);

        String secondActivityType = getDestinationActivity(purpose);
        Coord secondCoord = new Coord(t.destinationX, t.destinationY);

        Activity secondAct = factory.createActivityFromCoord(secondActivityType, secondCoord);
        //secondAct.setLinkId(NetworkUtils.getNearestLink(carNetwork, secondCoord).getId());
        double arrivalTime = t.departure_time + Math.min(getEstimatedTravelTime(t), 3600 * 4);
        secondAct.setStartTime(arrivalTime);
        plan.addActivity(secondAct);

        if (roundTrip) {
            double departure_time_return = t.departure_time_return;
            //make sure the arrival time is earlier than the departure time
            departure_time_return = Math.min(departure_time_return, arrivalTime + 1);

            secondAct.setEndTime(departure_time_return);
            Leg secondLeg = factory.createLeg(mode);
            secondLeg.setDepartureTime(departure_time_return);
            plan.addLeg(secondLeg);

            Activity thirdAct = factory.createActivityFromCoord(firstActivityType, firstCoord);
            //thirdAct.setLinkId(NetworkUtils.getNearestLink(carNetwork, firstCoord).getId());
            thirdAct.setStartTime(departure_time_return + Math.min(getEstimatedTravelTime(t), 3600 * 4));
            plan.addActivity(thirdAct);
        }

        p.addPlan(plan);
        p.setSelectedPlan(plan);
        return p;
    }

    private static String getOriginActivity(Purpose purpose){

        if (purpose.equals(Purpose.NHBW)){
            return "work";
        } else if (purpose.equals(Purpose.NHBO)){
            return "other";
        } else {
            return "home";
        }
    }

    private static String getDestinationActivity(Purpose purpose){

        if (purpose.equals(Purpose.HBW)){
            return "work";
        } else if (purpose.equals(Purpose.HBE)){
            return "education";
        } else if (purpose.equals(Purpose.HBS)){
            return "shopping";
        }  else {
            return "other";
        }
    }

    private static String decodeMode(String encodedMode) {
        switch (encodedMode) {
            case "autoDriver":
            case "auto":
                return "car";
            case "autoPassenger":
                return "car_passenger";
            case "train":
            case "bus":
            case "tramOrMetro":
                return "pt";
            case "bicycle":
                return "bike";
            default:
                return encodedMode;
        }
    }

    private static double getEstimatedTravelTime(Trip trip) {
        switch (trip.mode) {
            case "autoDriver":
            case "auto":
            case "autoPassenger":
                return trip.timeCar_s;
            case "train":
                return trip.timeTrain_s;
            case "tramOrMetro":
                return trip.timeTramMetro_s;
            case "bus":
                return trip.timeBus_s;
            case "walk":
                return trip.distance / SPEED_WALK_KMH * 3600;
            case "bicycle":
                return trip.distance / SPEED_BICYCLE_KMH * 3600;
            default:
                throw new RuntimeException("The mode " + trip.mode + " is not recognized");
        }
    }

    public final static class Trip {
        public final long id;
        public final double originX;
        public final double originY;
        public final double destinationX;
        public final double destinationY;
        public final String purpose;
        public final String person;
        public final double distance;
        public final String mode;
        public final double timeCar_s;
        public final double timeBus_s;
        public final double timeTramMetro_s;
        public final double timeTrain_s;
        public double departure_time;
        public double departure_time_return;


        public Trip(String line) {
            String[] data = line.split(delimiter);
            this.originX = Double.parseDouble(data[posOriginX]);
            this.originY = Double.parseDouble(data[posOriginY]);
            this.destinationX = Double.parseDouble(data[posDestinationX]);
            this.destinationY = Double.parseDouble(data[posDestinationY]);
            this.purpose = data[posPurpose];
            this.person = data[posPersonId];
            this.distance = Double.parseDouble(data[posDistance]);
            this.mode = data[posMode];
            try {
                this.departure_time = Double.parseDouble(data[posDepartureTime]) * 60;
            } catch (NumberFormatException e) {
                this.departure_time = 0.;
            }
            try {
                this.departure_time_return = Double.parseDouble(data[posDepartureTimeReturn]) * 60;
            } catch (NumberFormatException e) {
                this.departure_time_return = -1.;
            }
            this.timeCar_s = Double.parseDouble(data[posTimeCar]) * 60;
            this.timeTrain_s = Double.parseDouble(data[posTimeTrain]) * 60;
            this.timeTramMetro_s = Double.parseDouble(data[posTimeMetroTram]) * 60;
            this.timeBus_s = Double.parseDouble(data[posTimeBus]) * 60;
            this.id = Long.parseLong(data[posId]);
        }


    }
}
