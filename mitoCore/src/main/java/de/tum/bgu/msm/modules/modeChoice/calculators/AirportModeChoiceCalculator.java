package de.tum.bgu.msm.modules.modeChoice.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;

import java.util.EnumMap;

public class AirportModeChoiceCalculator implements ModeChoiceCalculator {

///////////////////////////////////////////////// AIRPORT Mode Choice /////////////////////////////////////////////////////

    @Override
    public EnumMap<Mode, Double> calculateProbabilities(Purpose purpose, MitoHousehold hh, MitoPerson person,
                                                        MitoZone originZone, MitoZone destinationZone,
                                                        TravelTimes travelTimes, double travelDistanceAuto,
                                                        double travelDistanceNMT, double peakHour) {
        if(purpose != Purpose.AIRPORT) {
            throw  new IllegalArgumentException("Airport mode choice calculator can only be used for airport purposes.");
        }

        //Order of variables in the return variable
        //Auto driver, Auto passenger, bicyle, bus, train, tram or metro, walk


        EnumMap<Mode, Double> utilities = calculateUtilities(purpose, hh,
                person, originZone, destinationZone,
                travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour);

        double sum_u = utilities.get(Mode.autoDriver) + utilities.get(Mode.autoPassenger)
                + utilities.get(Mode.bus) + utilities.get(Mode.train);


        double probabilityAutoD = utilities.get(Mode.autoDriver) / sum_u;
        double probabilityAutoP = utilities.get(Mode.autoPassenger) / sum_u;
        double probabilityBus = utilities.get(Mode.bus) / sum_u;
        double probabilityTrain = utilities.get(Mode.train) / sum_u;

        EnumMap<Mode, Double> probabilities = new EnumMap<>(Mode.class);
        probabilities.put(Mode.autoDriver, probabilityAutoD);
        probabilities.put(Mode.autoPassenger, probabilityAutoP);
        probabilities.put(Mode.bus, probabilityBus);
        probabilities.put(Mode.train, probabilityTrain);
        probabilities.put(Mode.walk, 0.);
        probabilities.put(Mode.bicycle, 0.);
        probabilities.put(Mode.tramOrMetro, 0.);
        return probabilities;
    }

    @Override
    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        if(purpose != Purpose.AIRPORT) {
            throw  new IllegalArgumentException("Airport mode choice calculator can only be used for airport purposes.");
        }

        double asc_autoDriver = 0.;
        double asc_autoPassenger = -0.0657 - 1.120;
        double asc_autoOther = -1.37275 - 2.606;
        double asc_bus = 2.246879 - 5.946;
        double asc_train = 2.992159 - 4.392;

        //times are in minutes
        double beta_time = -0.0002 * 60;
        double exp_time_autoDriver = 10.44896;
        double exp_time_autoPassenger = 10.44896;
        double exp_time_autoOther = 10.44896;
        double exp_time_bus = 0;
        double exp_time_train = 3.946016;

        //distance is in minutes
        double beta_distance = -0.00002;
        double exp_distance_autoDriver = 0;
        double exp_distance_autoPassenger = 1.939056;
        double exp_distance_autoOther = 4.649129;
        double exp_distance_bus = 9.662964;
        double exp_distance_train = 7.706087;

        //Order of variables in the return variable Auto driver, Auto passenger, bicyle, bus, train, tram or metro, walk

        double u_autoDriver = asc_autoDriver + exp_time_autoDriver * Math.exp(beta_time * travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car")) +
                exp_distance_autoDriver * Math.exp(beta_distance * travelDistanceAuto);
        double u_autoPassenger = asc_autoPassenger + exp_time_autoPassenger * Math.exp(beta_time * travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car")) +
                exp_distance_autoPassenger * Math.exp(beta_distance * travelDistanceAuto);
        double u_autoOther = asc_autoOther + exp_time_autoOther * Math.exp(beta_time * travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car")) +
                exp_distance_autoOther * Math.exp(beta_distance * travelDistanceAuto);
        double u_bus = asc_bus + exp_time_bus * Math.exp(beta_time * travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "bus")) +
                exp_distance_bus * Math.exp(beta_distance * travelDistanceAuto);
        double u_train = asc_train + exp_time_train * Math.exp(beta_time * travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "train")) +
                exp_distance_train * Math.exp(beta_distance * travelDistanceAuto);

        //Auto driver, Auto passenger, bicyle, bus, train, tram or metro, walk

        //TODO: returned Airport utilities are actually exponentiated utilities
        EnumMap<Mode, Double> utilities = new EnumMap<>(Mode.class);
        utilities.put(Mode.autoDriver, Math.exp(u_autoDriver) + Math.exp(u_autoOther));
        utilities.put(Mode.autoPassenger, Math.exp(u_autoPassenger));
        utilities.put(Mode.bicycle, 0.);
        utilities.put(Mode.bus, Math.exp(u_bus));
        utilities.put(Mode.train, Math.exp(u_train));
        utilities.put(Mode.tramOrMetro, 0.);
        utilities.put(Mode.walk, 0.);

        return utilities;
    }

    @Override
    public EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        throw new RuntimeException("Not implemented!");
    }
}
