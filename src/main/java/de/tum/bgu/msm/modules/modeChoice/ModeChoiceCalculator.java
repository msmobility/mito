package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;

public interface ModeChoiceCalculator {

    /**
     * For the time being implementations of this interface should adhere to the following order in the result array:
     * [0] probability Auto driver
     * [1] probability Auto passenger
     * [2] probability bicyle
     * [3] probability bus
     * [4] probability train
     * [5] probability tram or metro
     * [6] probability walk
     */
    double[] calculateProbabilities(Purpose purpose,
                                    MitoHousehold household,
                                    MitoPerson person,
                                    MitoZone originZone,
                                    MitoZone destinationZone,
                                    TravelTimes travelTimes,
                                    double travelDistanceAuto,
                                    double travelDistanceNMT,
                                    double peakHour_s);

    /**
     * For the time being implementations of this interface should adhere to the following order in the result array:
     * [0] probability Auto driver
     * [1] probability Auto passenger
     * [2] probability bicyle
     * [3] probability bus
     * [4] probability train
     * [5] probability tram or metro
     * [6] probability walk
     */
    double[] calculateUtilities(Purpose purpose,
                                    MitoHousehold household,
                                    MitoPerson person,
                                    MitoZone originZone,
                                    MitoZone destinationZone,
                                    TravelTimes travelTimes,
                                    double travelDistanceAuto,
                                    double travelDistanceNMT,
                                    double peakHour_s);


    /**
     * For the time being implementations of this interface should adhere to the following order in the result array:
     * [0] probability Auto driver
     * [1] probability Auto passenger
     * [2] probability bicyle
     * [3] probability bus
     * [4] probability train
     * [5] probability tram or metro
     * [6] probability walk
     */
    double[] calculateGeneralizedCosts(Purpose purpose,
                                MitoHousehold household,
                                MitoPerson person,
                                MitoZone originZone,
                                MitoZone destinationZone,
                                TravelTimes travelTimes,
                                double travelDistanceAuto,
                                double travelDistanceNMT,
                                double peakHour_s);
}
