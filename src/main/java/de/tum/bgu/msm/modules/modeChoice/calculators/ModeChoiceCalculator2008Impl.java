package de.tum.bgu.msm.modules.modeChoice.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.EnumMap;
import java.util.Map;

public class ModeChoiceCalculator2008Impl implements ModeChoiceCalculator {

    private final Purpose purpose;
    private final static Logger logger = Logger.getLogger(ModeChoiceCalculator2008Impl.class);
    private final Map<Mode, Map<String, Double>> coef;

    public ModeChoiceCalculator2008Impl(Purpose purpose, DataSet dataSet) {
        this.purpose = purpose;
        coef = new ModeChoiceCoefficientReader(dataSet, purpose, Resources.instance.getModeChoiceCoefficients(purpose)).readCoefficientsForThisPurpose();
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

        final double utilityAutoD = utilities.get(Mode.autoDriver);
        final double utilityAutoP = utilities.get(Mode.autoPassenger);
        final double utilityBicycle = utilities.get(Mode.bicycle);
        final double utilityBus = utilities.get(Mode.bus);
        final double utilityTrain = utilities.get(Mode.train);
        final double utilityTramMetro = utilities.get(Mode.tramOrMetro);
        final double utilityWalk = utilities.get(Mode.walk);

        double expsumNestAuto = Math.exp(utilityAutoD / coef.get(Mode.autoDriver).get("nestingCoefficient")) + Math.exp(utilityAutoP / coef.get(Mode.autoDriver).get("nestingCoefficient"));
        double expsumNestTransit = Math.exp(utilityBus / coef.get(Mode.train).get("nestingCoefficient")) + Math.exp(utilityTrain / coef.get(Mode.train).get("nestingCoefficient")) + Math.exp(utilityTramMetro / coef.get(Mode.train).get("nestingCoefficient"));
        double expsumTopLevel = Math.exp(coef.get(Mode.autoDriver).get("nestingCoefficient") * Math.log(expsumNestAuto)) + Math.exp(utilityBicycle) + Math.exp(utilityWalk) + Math.exp(coef.get(Mode.train).get("nestingCoefficient") * Math.log(expsumNestTransit));

        double probabilityAutoD;
        double probabilityAutoP;
        if (expsumNestAuto > 0) {
            probabilityAutoD = (Math.exp(utilityAutoD / coef.get(Mode.autoPassenger).get("nestingCoefficient")) / expsumNestAuto) * (Math.exp(coef.get(Mode.autoPassenger).get("nestingCoefficient") * Math.log(expsumNestAuto)) / expsumTopLevel);
            probabilityAutoP = (Math.exp(utilityAutoP / coef.get(Mode.autoPassenger).get("nestingCoefficient")) / expsumNestAuto) * (Math.exp(coef.get(Mode.autoPassenger).get("nestingCoefficient") * Math.log(expsumNestAuto)) / expsumTopLevel);
        } else {
            probabilityAutoD = 0.0;
            probabilityAutoP = 0.0;
        }

        double probabilityBus;
        double probabilityTrain;
        double probabilityTramMetro;
        if (expsumNestTransit > 0) {
            probabilityBus = (Math.exp(utilityBus / coef.get(Mode.train).get("nestingCoefficient")) / expsumNestTransit) * (Math.exp(coef.get(Mode.train).get("nestingCoefficient") * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTrain = (Math.exp(utilityTrain / coef.get(Mode.train).get("nestingCoefficient")) / expsumNestTransit) * (Math.exp(coef.get(Mode.train).get("nestingCoefficient") * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTramMetro = (Math.exp(utilityTramMetro / coef.get(Mode.train).get("nestingCoefficient")) / expsumNestTransit) * (Math.exp(coef.get(Mode.train).get("nestingCoefficient") * Math.log(expsumNestTransit)) / expsumTopLevel);
        } else {
            probabilityBus = 0.0;
            probabilityTrain = 0.0;
            probabilityTramMetro = 0.0;
        }
        double probabilityBicycle = Math.exp(utilityBicycle) / expsumTopLevel;
        double probabilityWalk = Math.exp(utilityWalk) / expsumTopLevel;


        EnumMap<Mode, Double> probabilities = new EnumMap<>(Mode.class);
        probabilities.put(Mode.autoDriver, probabilityAutoD);
        probabilities.put(Mode.autoPassenger, probabilityAutoP);
        probabilities.put(Mode.bicycle, probabilityBicycle);
        probabilities.put(Mode.bus, probabilityBus);
        probabilities.put(Mode.train, probabilityTrain);
        probabilities.put(Mode.tramOrMetro, probabilityTramMetro);
        probabilities.put(Mode.walk, probabilityWalk);
        return probabilities;
    }

    @Override
    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        int age = person.getAge();
        int isMale = person.getMitoGender() == MitoGender.MALE ? 1 : 0;
        int hasLicense = person.hasDriversLicense() ? 1 : 0;

        int hhSize = household.getHhSize();
        int hhAutos = household.getAutos();
        int hhChildren = DataSet.getChildrenForHousehold(household);

        final float distanceToNearestRailStop = originZone.getDistanceToNearestRailStop();

        int isCoreCity = originZone.getAreaTypeSG() == AreaTypes.SGType.CORE_CITY ? 1 : 0;
        int isMediumCity = originZone.getAreaTypeSG() == AreaTypes.SGType.MEDIUM_SIZED_CITY ? 1 : 0;
        int isTown = originZone.getAreaTypeSG() == AreaTypes.SGType.TOWN ? 1 : 0;
        int isRural = originZone.getAreaTypeSG() == AreaTypes.SGType.RURAL ? 1 : 0;

        int isAgglomerationR = originZone.getAreaTypeR() == AreaTypes.RType.AGGLOMERATION ? 1 : 0;
        int isRuralR = originZone.getAreaTypeR() == AreaTypes.RType.RURAL ? 1 : 0;
        int isUrbanR = originZone.getAreaTypeR() == AreaTypes.RType.URBAN ? 1 : 0;

        int isMunichTrip = originZone.isMunichZone() ? 1 : 0;

        EnumMap<Mode, Double> generalizedCosts = calculateGeneralizedCosts(purpose, household, person,
                originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);


        EnumMap<Mode, Double> utilities = new EnumMap<>(Mode.class);
        for (Mode mode :coef.keySet()){
            double distance = isMotorized(mode)? travelDistanceAuto : travelDistanceNMT;
            final Map<String, Double> modeCoef = coef.get(mode);
            double gc = generalizedCosts.get(mode);
            double utility = modeCoef.get("intercept")
                    + modeCoef.get("age") * age
                    + modeCoef.get("isMale") * isMale
                    + modeCoef.get("hasLicense") * hasLicense
                    + modeCoef.get("hhSize") * hhSize
                    + modeCoef.get("hhAutos") * hhAutos
                    + modeCoef.get("distanceToNearestRailStop") * distanceToNearestRailStop
                    + modeCoef.get("hhChildren") * hhChildren
                    + modeCoef.get("isCoreCity") * isCoreCity
                    + modeCoef.get("isMediumCity") * isMediumCity
                    + modeCoef.get("isTown") * isTown
                    + modeCoef.get("isRural") * isRural
                    + modeCoef.get("isAgglomerationOrUrbanR") * (isAgglomerationR + isUrbanR)
                    + modeCoef.get("isRuralR") * isRuralR
                    + modeCoef.get("gc") * gc
                    + modeCoef.get("gc_squared") * (gc * gc)
                    + modeCoef.get("distance") * distance
                    + modeCoef.get("isMunich") * isMunichTrip;

            utilities.put(mode, utility);
        }

        return utilities;
    }

    @Override
    public EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        double timeAutoD = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car");
        double timeAutoP = timeAutoD;
        double timeBus = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "bus");
        double timeTrain = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "train");
        double timeTramMetro = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "tramMetro");

        int monthlyIncome_EUR = household.getMonthlyIncome_EUR();
        int purpIdx;
        if (purpose.equals(Purpose.HBR)) {
            purpIdx = Purpose.HBO.ordinal();
            //there is no mode choice for HBR trips yet
        } else {
            purpIdx = purpose.ordinal();
        }

        double gcAutoD;
        double gcAutoP;
        double gcBus;
        double gcTrain;
        double gcTramMetro;

        if (monthlyIncome_EUR <= 1500) {
            gcAutoD = timeAutoD + (travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm")) / coef.get(Mode.autoDriver).get("vot_under_1500");
            gcAutoP = timeAutoP + (travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm")) / coef.get(Mode.autoPassenger).get("vot_under_1500");
            gcBus = timeBus + (travelDistanceAuto * coef.get(Mode.bus).get("costPerKm")) / coef.get(Mode.bus).get("vot_under_1500");
            gcTrain = timeTrain + (travelDistanceAuto * coef.get(Mode.train).get("costPerKm")) / coef.get(Mode.train).get("vot_under_1500");
            gcTramMetro = timeTramMetro + (travelDistanceAuto * coef.get(Mode.tramOrMetro).get("costPerKm")) / coef.get(Mode.tramOrMetro).get("vot_under_1500");
        } else if (monthlyIncome_EUR <= 5600) {
            gcAutoD = timeAutoD + (travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm")) / coef.get(Mode.autoDriver).get("vot_1500_to_5600");
            gcAutoP = timeAutoP + (travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm")) / coef.get(Mode.autoPassenger).get("vot_1500_to_5600");
            gcBus = timeBus + (travelDistanceAuto * coef.get(Mode.bus).get("costPerKm")) / coef.get(Mode.bus).get("vot_1500_to_5600");
            gcTrain = timeTrain + (travelDistanceAuto * coef.get(Mode.train).get("costPerKm")) / coef.get(Mode.train).get("vot_1500_to_5600");
            gcTramMetro = timeTramMetro + (travelDistanceAuto * coef.get(Mode.tramOrMetro).get("costPerKm")) / coef.get(Mode.tramOrMetro).get("vot_1500_to_5600");
        } else {
            gcAutoD = timeAutoD + (travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm")) / coef.get(Mode.autoDriver).get("vot_above_5600");
            gcAutoP = timeAutoP + (travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm")) / coef.get(Mode.autoPassenger).get("vot_above_5600");
            gcBus = timeBus + (travelDistanceAuto * coef.get(Mode.bus).get("costPerKm")) / coef.get(Mode.bus).get("vot_above_5600");
            gcTrain = timeTrain + (travelDistanceAuto * coef.get(Mode.train).get("costPerKm")) / coef.get(Mode.train).get("vot_above_5600");
            gcTramMetro = timeTramMetro + (travelDistanceAuto * coef.get(Mode.tramOrMetro).get("costPerKm")) / coef.get(Mode.tramOrMetro).get("vot_above_5600");
        }

        EnumMap<Mode, Double> generalizedCosts = new EnumMap<>(Mode.class);
        generalizedCosts.put(Mode.autoDriver, gcAutoD);
        generalizedCosts.put(Mode.autoPassenger, gcAutoP);
        generalizedCosts.put(Mode.bicycle, 0.);
        generalizedCosts.put(Mode.bus, gcBus);
        generalizedCosts.put(Mode.train, gcTrain);
        generalizedCosts.put(Mode.tramOrMetro, gcTramMetro);
        generalizedCosts.put(Mode.walk, 0.);
        return generalizedCosts;

    }

    private static boolean isMotorized(Mode mode) {
        if (mode.equals(Mode.walk) || mode.equals(Mode.bicycle)){
            return false;
        } else {
            return true;
        }
    }
}
