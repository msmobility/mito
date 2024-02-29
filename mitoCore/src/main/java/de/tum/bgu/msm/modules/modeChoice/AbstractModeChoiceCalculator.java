package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.LogitTools;
import org.matsim.core.utils.collections.Tuple;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

public abstract class AbstractModeChoiceCalculator implements ModeChoiceCalculator{

    private static final LogitTools<Mode> logitTools = new LogitTools<>(Mode.class);
    private List<Tuple<EnumSet<Mode>, Double>> nests = null;

    protected void setNests(List<Tuple<EnumSet<Mode>, Double>> nests) {
        this.nests = nests;
    }

    public List<Tuple<EnumSet<Mode>, Double>> getNests() {
        return nests;
    }

    @Override
    public EnumMap<Mode, Double> calculateProbabilities(
            Purpose purpose,
            MitoHousehold household,
            MitoPerson person,
            MitoZone originZone,
            MitoZone destinationZone,
            TravelTimes travelTimes,
            double travelDistanceAuto,
            double travelDistanceNMT,
            double peakHour_s) {

        EnumMap<Mode, Double> utilities = calculateUtilities(
                purpose, household, person, originZone, destinationZone, travelTimes
                , travelDistanceAuto, travelDistanceNMT, peakHour_s);

        if(utilities == null) return null;
        else return logitTools.getProbabilities(utilities, nests);
    }

    public abstract EnumMap<Mode, Double> calculateUtilities(Purpose purpose,
                                                             MitoHousehold household,
                                                             MitoPerson person,
                                                             MitoZone originZone,
                                                             MitoZone destinationZone,
                                                             TravelTimes travelTimes,
                                                             double travelDistanceAuto,
                                                             double travelDistanceNMT,
                                                             double peakHour_s);

    public abstract EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose,
                                                    MitoHousehold household,
                                                    MitoPerson person,
                                                    MitoZone originZone,
                                                    MitoZone destinationZone,
                                                    TravelTimes travelTimes,
                                                    double travelDistanceAuto,
                                                    double travelDistanceNMT,
                                                    double peakHour_s);
}
