package de.tum.bgu.msm.modules.modeChoice.calculators;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;

public class AirportModeChoiceCalculator implements ModeChoiceCalculator {

///////////////////////////////////////////////// AIRPORT Mode Choice /////////////////////////////////////////////////////

    @Override
    public double[] calculateProbabilities(Purpose purpose, MitoHousehold hh, MitoPerson person,
                                           MitoZone originZone, MitoZone destinationZone,
                                           TravelTimes travelTimes, double travelDistanceAuto,
                                           double travelDistanceNMT, double peakHour) {
        if(purpose != Purpose.AIRPORT) {
            throw  new IllegalArgumentException("Airport mode choice calculator can only be used for airport purposes.");
        }

        //Order of variables in the return variable  [AutoD, AutoP, Bicycle, Bus,  Train, TramMetro, Walk]

        double[] utilities = calculateUtilities(purpose, hh,
                person, originZone, destinationZone,
                travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour);

        double sum_u = utilities[0] + utilities[1] + utilities[2] + utilities[3] + utilities[4];


        double probabilityAutoD = (utilities[0] + utilities[2]) / sum_u;
        double probabilityAutoP = utilities[1] / sum_u;
        double probabilityBus = utilities[3] / sum_u;
        double probabilityTrain = utilities[4] / sum_u;

        return new double[]{probabilityAutoD, probabilityAutoP, 0., probabilityBus, probabilityTrain, 0., 0.};
    }

    @Override
    public double[] calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        if(purpose != Purpose.AIRPORT) {
            throw  new IllegalArgumentException("Airport mode choice calculator can only be used for airport purposes.");
        }

        double asc_autoDriver = 0.;
        double asc_autoPassenger = -0.0657;
        double asc_autoOther = -1.37275;
        double asc_bus = 2.246879;
        double asc_train = 2.992159;

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

        //Order of variables in the return variable autoDriver, autoPassenger, autoOther, bus, train

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

        return new double[]{Math.exp(u_autoDriver), Math.exp(u_autoDriver), Math.exp(u_autoOther), Math.exp(u_bus), Math.exp(u_train)};
    }

    @Override
    public double[] calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        throw new RuntimeException("Not implemented!");
    }
}
