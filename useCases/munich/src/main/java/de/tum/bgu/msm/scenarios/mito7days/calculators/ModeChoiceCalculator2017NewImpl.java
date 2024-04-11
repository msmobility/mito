package de.tum.bgu.msm.scenarios.mito7days.calculators;

import de.tum.bgu.msm.data.MitoPerson7days;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
import de.tum.bgu.msm.modules.modeChoice.AbstractModeChoiceCalculator;
import de.tum.bgu.msm.resources.Resources;
import org.matsim.core.utils.collections.Tuple;

import java.util.*;

import static de.tum.bgu.msm.data.Mode.*;

public class ModeChoiceCalculator2017NewImpl extends AbstractModeChoiceCalculator {

    private final Map<Mode, Map<String, Double>> coef;

    public ModeChoiceCalculator2017NewImpl(Purpose purpose, DataSet dataSet) {
        super();
        coef = new ModeChoiceCoefficientReader(dataSet, purpose, Resources.instance.getModeChoiceCoefficients(purpose)).readCoefficientsForThisPurpose();
        setNests();
    }

    private void setNests() {
        List<Tuple<EnumSet<Mode>, Double>> nests = new ArrayList<>();
        nests.add(new Tuple<>(EnumSet.of(autoDriver,autoPassenger), 0.25));
        nests.add(new Tuple<>(EnumSet.of(train, tramOrMetro, bus, taxi), 0.25));
        nests.add(new Tuple<>(EnumSet.of(walk,bicycle), 1.));
        super.setNests(nests);
    }

    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        int age = person.getAge();
        boolean female = person.getMitoGender().equals(MitoGender.FEMALE);
        int hhSize = Math.min(household.getHhSize(),5);
        int hhChildren = Math.min(household.getChildrenForHousehold(),3);
        int hhAutos = household.getAutos();
        boolean driversLicense = person.hasDriversLicense();
        double hhKmToTransit = household.getHomeZone().getDistanceToNearestRailStop();
        AreaTypes.SGType region = household.getHomeZone().getAreaTypeSG();

        EnumMap<Mode, Double> generalizedCosts = calculateGeneralizedCosts(purpose, household, person,
                originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);


        EnumMap<Mode, Double> utilities = new EnumMap<>(Mode.class);

        for (Mode mode : ((MitoPerson7days)person).getModeSet().getModes()){
            final Map<String, Double> modeCoef = coef.get(mode);

            // Intercept
            double utility = modeCoef.get("asc");

            // Driver's License
            if (driversLicense) {
                utility += modeCoef.getOrDefault("p.driversLicense",0.);
            }

            // Sex
            if (female) {
                utility += modeCoef.getOrDefault("p.female",0.);
            }

            // Age
            utility += age * modeCoef.getOrDefault("p.age",0.);

            // Household Distance to Transit
            utility += hhKmToTransit * modeCoef.getOrDefault("hh.transit_km",0.);

            // Household size
            utility += hhSize * modeCoef.getOrDefault("hh.size",0.);

            // Household Children
            utility += hhChildren * modeCoef.getOrDefault("hh.children",0.);

            // Household Autos
            if (hhAutos == 0) {
                utility += modeCoef.getOrDefault("hh.cars_0",0.);
            } else if (hhAutos == 1) {
                utility += modeCoef.getOrDefault("hh.cars_1",0.);
            } else if (hhAutos == 2) {
                utility += modeCoef.getOrDefault("hh.cars_2",0.);
            } else {
                utility += modeCoef.getOrDefault("hh.cars_3",0.);
            }

            // Household BBSR Region Type
            if (region.equals(AreaTypes.SGType.CORE_CITY)) {
                utility += modeCoef.getOrDefault("hh.BBSR_1",0.);
            } else if (region.equals(AreaTypes.SGType.MEDIUM_SIZED_CITY)) {
                utility += modeCoef.getOrDefault("hh.BBSR_2",0.);
            } else if (region.equals(AreaTypes.SGType.TOWN)) {
                utility += modeCoef.getOrDefault("hh.BBSR_3",0.);
            } else if (region.equals(AreaTypes.SGType.RURAL)) {
                utility += modeCoef.getOrDefault("hh.BBSR_4",0.);
            }

            double gc = generalizedCosts.get(mode);
            utility += gc * modeCoef.get("gc");

            utilities.put(mode, utility);
        }

        return utilities;
    }

    public EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone,
                                                           MitoZone destinationZone, TravelTimes travelTimes,
                                                           double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        double timeAutoD = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car");
        double timeAutoP = timeAutoD;
        double timeBus = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "bus");
        double timeTrain = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "train");
        double timeTramMetro = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "tramMetro");

        int monthlyIncome_EUR = household.getMonthlyIncome_EUR();

        double gcAutoD;
        double gcAutoP;
        double gcBus;
        double gcTrain;
        double gcTramMetro;
        double gcWalk = travelDistanceNMT;
        double gcBicycle = travelDistanceNMT;

        if (monthlyIncome_EUR <= 1500) {
            gcAutoD = timeAutoD + (travelDistanceAuto * 0.7) / coef.get(Mode.autoDriver).get("vot_under_1500_eur_min");
            gcAutoP = timeAutoP + (travelDistanceAuto * 0.7) / coef.get(Mode.autoPassenger).get("vot_under_1500_eur_min");
            gcBus = timeBus + (travelDistanceAuto * 0.12) / coef.get(Mode.bus).get("vot_under_1500_eur_min");
            gcTrain = timeTrain + (travelDistanceAuto * 0.12) / coef.get(Mode.train).get("vot_under_1500_eur_min");
            gcTramMetro = timeTramMetro + (travelDistanceAuto * 0.12) / coef.get(Mode.tramOrMetro).get("vot_under_1500_eur_min");
        } else if (monthlyIncome_EUR <= 5600) {
            gcAutoD = timeAutoD + (travelDistanceAuto * 0.7) / coef.get(Mode.autoDriver).get("vot_1500_to_5600_eur_min");
            gcAutoP = timeAutoP + (travelDistanceAuto * 0.7) / coef.get(Mode.autoPassenger).get("vot_1500_to_5600_eur_min");
            gcBus = timeBus + (travelDistanceAuto * 0.12) / coef.get(Mode.bus).get("vot_1500_to_5600_eur_min");
            gcTrain = timeTrain + (travelDistanceAuto * 0.12) / coef.get(Mode.train).get("vot_1500_to_5600_eur_min");
            gcTramMetro = timeTramMetro + (travelDistanceAuto * 0.12) / coef.get(Mode.tramOrMetro).get("vot_1500_to_5600_eur_min");
        } else {
            gcAutoD = timeAutoD + (travelDistanceAuto * 0.7) / coef.get(Mode.autoDriver).get("vot_above_5600_eur_min");
            gcAutoP = timeAutoP + (travelDistanceAuto * 0.7) / coef.get(Mode.autoPassenger).get("vot_above_5600_eur_min");
            gcBus = timeBus + (travelDistanceAuto * 0.12) / coef.get(Mode.bus).get("vot_above_5600_eur_min");
            gcTrain = timeTrain + (travelDistanceAuto * 0.12) / coef.get(Mode.train).get("vot_above_5600_eur_min");
            gcTramMetro = timeTramMetro + (travelDistanceAuto * 0.12) / coef.get(Mode.tramOrMetro).get("vot_above_5600_eur_min");
        }

        EnumMap<Mode, Double> generalizedCosts = new EnumMap<>(Mode.class);
        generalizedCosts.put(Mode.autoDriver, gcAutoD);
        generalizedCosts.put(Mode.autoPassenger, gcAutoP);
        generalizedCosts.put(Mode.bicycle, gcBicycle);
        generalizedCosts.put(Mode.bus, gcBus);
        generalizedCosts.put(Mode.train, gcTrain);
        generalizedCosts.put(Mode.tramOrMetro, gcTramMetro);
        generalizedCosts.put(Mode.walk, gcWalk);
        return generalizedCosts;

    }

}
