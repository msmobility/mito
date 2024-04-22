package de.tum.bgu.msm.analysis.logsumAccessibility;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;

import java.util.EnumMap;
import java.util.Map;

public class LogsumCalculator2 {

    //private final ModeChoiceCalculator base;
    private final Map<Mode, Map<String, Double>> coef;
    private static final double SPEED_WALK_KMH = 4;
    private static final double SPEED_BICYCLE_KMH = 10;

    public LogsumCalculator2(Map<Mode, Map<String, Double>> coef) {
        this.coef = coef;
  }

    public double calculateLogsumByZone(
            Boolean hasEV,
            MitoZone originZone,
            MitoZone destinationZone,
            TravelTimes travelTimes,
            double travelDistanceAuto,
            double travelDistanceNMT,
            double peakHour_s) {

        EnumMap<Mode, Double> utilities = calculateUtilities(hasEV,
                originZone, destinationZone, travelTimes
                , travelDistanceAuto, travelDistanceNMT, peakHour_s);

        final double utilityAutoD = utilities.get(Mode.autoDriver);
        final double utilityAutoP = utilities.get(Mode.autoPassenger);
        final double utilityBicycle = utilities.get(Mode.bicycle);
        final double utilityBus = utilities.get(Mode.bus);
        final double utilityTrain = utilities.get(Mode.train);
        final double utilityTramMetro = utilities.get(Mode.tramOrMetro);
        final double utilityTaxi = utilities.get(Mode.taxi);
        final double utilityWalk = utilities.get(Mode.walk);

        final Double nestingCoefficientAutoModes = coef.get(Mode.autoDriver).get("nestingCoefficient");
        final Double nestingCoefficientPtModes = coef.get(Mode.train).get("nestingCoefficient");

        double expsumNestAuto =
                Math.exp(utilityAutoD / nestingCoefficientAutoModes) +
                        Math.exp(utilityAutoP / nestingCoefficientAutoModes);
        double expsumNestTransit =
                Math.exp(utilityBus / nestingCoefficientPtModes) +
                        Math.exp(utilityTrain / nestingCoefficientPtModes) +
                        Math.exp(utilityTramMetro / nestingCoefficientPtModes) +
                        Math.exp(utilityTaxi / nestingCoefficientPtModes);
        double expsumTopLevel =
                Math.exp(nestingCoefficientAutoModes * Math.log(expsumNestAuto)) +
                        Math.exp(utilityBicycle) +
                        Math.exp(utilityWalk) +
                        Math.exp(nestingCoefficientPtModes * Math.log(expsumNestTransit));

        return Math.log(expsumTopLevel);
    }

    public EnumMap<Mode, Double> calculateUtilities(Boolean hasEV, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        float hhSize1, hhSize2, hhSize3, hhSize4,isMale, isFemale,worker, unemployed, student, retired,age_0_to_17,
                age_18_to_29,age_30_to_39,age_40_to_49,age_50_to_59, age_above_60, econStatus1, econStatus2, econStatus3,
                econStatus4, econStatus5, hhAutos0, hhAutos1,hhAutos2, monthlyInc;
        if (hasEV == true){
            hhSize1 = 0.1426f;
            hhSize2 = 0.2526f;
            hhSize3 = 0.2184f;
            hhSize4 = 0.3864f;
            isMale = 0.5f;
            isFemale = 0.5f;
            worker = 0.4189f;
            unemployed = 0.4256f;
            student = 0.1554f;
            retired = 0.0f;
            age_0_to_17 = 0.1803f;
            age_18_to_29 = 0.137f;
            age_30_to_39 = 0.1278f;
            age_40_to_49 = 0.1872f;
            age_50_to_59 = 0.1441f;
            age_above_60 = 0.2236f;
            econStatus1 = 0.111f;
            econStatus2 = 0.115f;
            econStatus3 = 0.27f;
            econStatus4 = 0.32f;
            econStatus5 = 0.184f;
            hhAutos0 = 0.0f;
            hhAutos1 = 0.4705f;
            hhAutos2 = 0.5295f;
            monthlyInc = 3345f;
        }else{
            hhSize1 = 0.1908f;
            hhSize2 = 0.3058f;
            hhSize3 = 0.1921f;
            hhSize4 = 0.3113f;
            isMale = 0.489f;
            isFemale = 0.511f;
            worker = 0.3839f;
            unemployed = 0.4698f;
            student = 0.1463f;
            retired = 0.0f;
            age_0_to_17 = 0.1659f;
            age_18_to_29 = 0.1387f;
            age_30_to_39 = 0.1367f;
            age_40_to_49 = 0.1717f;
            age_50_to_59 = 0.1448f;
            age_above_60 = 0.2523f;
            econStatus1 = 0.148f;
            econStatus2 = 0.134f;
            econStatus3 = 0.291f;
            econStatus4 = 0.28f;
            econStatus5 = 0.146f;
            hhAutos0 = 0.122f;
            hhAutos1 = 0.5249f;
            hhAutos2 = 0.3531f;
            monthlyInc = 2891f;
        }
        boolean hhHasBicycles = true;

        EnumMap<Mode, Double> generalizedCosts = calculateGeneralizedCosts(monthlyInc,
                originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);

        EnumMap<Mode, Double> utilities = new EnumMap<>(Mode.class);

        for (Mode mode :coef.keySet()){
            //double distance = isMotorized(mode)? travelDistanceAuto : travelDistanceNMT;
            final Map<String, Double> modeCoef = coef.get(mode);
            double utility = modeCoef.get("intercept");
            utility += isMale * modeCoef.get("gender_male");
            utility += isFemale * modeCoef.get("gender_female");
            utility += worker * modeCoef.get("is_employed");
            utility += unemployed * modeCoef.get("is_homemaker_or_other");
            utility += student * modeCoef.get("is_student");
            utility += retired * modeCoef.get("is_retired_or_pensioner");
            utility += age_0_to_17*modeCoef.get("age_0_to_17");
            utility += age_18_to_29*modeCoef.get("age_18_to_29");
            utility += age_30_to_39*modeCoef.get("age_30_to_39");
            utility += age_40_to_49*modeCoef.get("age_40_to_49");
            utility += age_50_to_59*modeCoef.get("age_50_to_59");
            utility += age_above_60*modeCoef.get("age_above_60");
            utility += econStatus1*modeCoef.get("is_economic_status_very_low");
            utility += econStatus2*modeCoef.get("is_economic_status_low");
            utility += econStatus3*modeCoef.get("is_economic_status_medium");
            utility += econStatus4*modeCoef.get("is_economic_status_high");
            utility += econStatus5*modeCoef.get("is_economic_status_very_high");
            utility += hhSize1*modeCoef.get("is_hh_one_person");
            utility += hhSize2*modeCoef.get("is_hh_two_persons");
            utility += hhSize3*modeCoef.get("is_hh_three_persons");
            utility += hhSize4*modeCoef.get("is_hh_four_or_more_persons");
            utility += hhAutos0*modeCoef.get("hh_no_car");
            utility += hhAutos1*modeCoef.get("hh_one_car");
            utility += hhAutos2*modeCoef.get("hh_two_or_more_cars");
            if (hhHasBicycles){
                utility += modeCoef.get("hh_has_bike");
            } else {
                utility += modeCoef.get("hh_no_bike");
            }
            double gc = generalizedCosts.get(mode);
            utility += modeCoef.get("exp_generalized_time_min") * Math.exp(gc * modeCoef.get("alpha"));

            utilities.put(mode, utility);
        }

        return utilities;
    }

    public EnumMap<Mode, Double> calculateGeneralizedCosts(float monthlyInc, MitoZone originZone,
                                                           MitoZone destinationZone, TravelTimes travelTimes,
                                                           double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        double timeAutoD = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car");
        double timeAutoP = timeAutoD;
        double timeBus = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "bus");
        double timeTrain = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "train");
        double timeTramMetro = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "tramMetro");
        double timeTaxi = timeAutoD;

        double gcAutoD;
        double gcAutoP;
        double gcBus;
        double gcTrain;
        double gcTramMetro;
        double gcTaxi;
        double gcWalk = travelDistanceNMT / SPEED_WALK_KMH * 60;
        double gcBicycle = travelDistanceNMT / SPEED_BICYCLE_KMH * 60;

        if (monthlyInc <= 1500) {
            gcAutoD = timeAutoD + (travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm")) / coef.get(Mode.autoDriver).get("vot_under_1500_eur_min");
            gcAutoP = timeAutoP + (travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm")) / coef.get(Mode.autoPassenger).get("vot_under_1500_eur_min");
            gcBus = timeBus + (travelDistanceAuto * coef.get(Mode.bus).get("costPerKm")) / coef.get(Mode.bus).get("vot_under_1500_eur_min");
            gcTrain = timeTrain + (travelDistanceAuto * coef.get(Mode.train).get("costPerKm")) / coef.get(Mode.train).get("vot_under_1500_eur_min");
            gcTramMetro = timeTramMetro + (travelDistanceAuto * coef.get(Mode.tramOrMetro).get("costPerKm")) / coef.get(Mode.tramOrMetro).get("vot_under_1500_eur_min");
            gcTaxi = timeTaxi + (travelDistanceAuto * coef.get(Mode.taxi).get("costPerKm")) / coef.get(Mode.taxi).get("vot_under_1500_eur_min");
        } else if (monthlyInc <= 5600) {
            gcAutoD = timeAutoD + (travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm")) / coef.get(Mode.autoDriver).get("vot_1500_to_5600_eur_min");
            gcAutoP = timeAutoP + (travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm")) / coef.get(Mode.autoPassenger).get("vot_1500_to_5600_eur_min");
            gcBus = timeBus + (travelDistanceAuto * coef.get(Mode.bus).get("costPerKm")) / coef.get(Mode.bus).get("vot_1500_to_5600_eur_min");
            gcTrain = timeTrain + (travelDistanceAuto * coef.get(Mode.train).get("costPerKm")) / coef.get(Mode.train).get("vot_1500_to_5600_eur_min");
            gcTramMetro = timeTramMetro + (travelDistanceAuto * coef.get(Mode.tramOrMetro).get("costPerKm")) / coef.get(Mode.tramOrMetro).get("vot_1500_to_5600_eur_min");
            gcTaxi = timeTaxi + (travelDistanceAuto * coef.get(Mode.taxi).get("costPerKm")) / coef.get(Mode.taxi).get("vot_1500_to_5600_eur_min");
        } else {
            gcAutoD = timeAutoD + (travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm")) / coef.get(Mode.autoDriver).get("vot_above_5600_eur_min");
            gcAutoP = timeAutoP + (travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm")) / coef.get(Mode.autoPassenger).get("vot_above_5600_eur_min");
            gcBus = timeBus + (travelDistanceAuto * coef.get(Mode.bus).get("costPerKm")) / coef.get(Mode.bus).get("vot_above_5600_eur_min");
            gcTrain = timeTrain + (travelDistanceAuto * coef.get(Mode.train).get("costPerKm")) / coef.get(Mode.train).get("vot_above_5600_eur_min");
            gcTramMetro = timeTramMetro + (travelDistanceAuto * coef.get(Mode.tramOrMetro).get("costPerKm")) / coef.get(Mode.tramOrMetro).get("vot_above_5600_eur_min");
            gcTaxi = timeTaxi + (travelDistanceAuto * coef.get(Mode.taxi).get("costPerKm")) / coef.get(Mode.taxi).get("vot_above_5600_eur_min");
        }

        EnumMap<Mode, Double> generalizedCosts = new EnumMap<>(Mode.class);
        generalizedCosts.put(Mode.autoDriver, gcAutoD);
        generalizedCosts.put(Mode.autoPassenger, gcAutoP);
        generalizedCosts.put(Mode.bicycle, gcBicycle);
        generalizedCosts.put(Mode.bus, gcBus);
        generalizedCosts.put(Mode.train, gcTrain);
        generalizedCosts.put(Mode.tramOrMetro, gcTramMetro);
        generalizedCosts.put(Mode.taxi, gcTaxi);
        generalizedCosts.put(Mode.walk, gcWalk);
        return generalizedCosts;

    }

}
