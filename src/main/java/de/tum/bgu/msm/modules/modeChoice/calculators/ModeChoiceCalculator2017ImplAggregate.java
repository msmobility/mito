package de.tum.bgu.msm.modules.modeChoice.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculatorAggregate;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.EnumMap;
import java.util.Map;

public class ModeChoiceCalculator2017ImplAggregate implements ModeChoiceCalculatorAggregate {

    private static final double SPEED_WALK_KMH = 4;
    private static final double SPEED_BICYCLE_KMH = 10;
    private final Purpose purpose;
    private final static Logger logger = Logger.getLogger(ModeChoiceCalculator2017ImplAggregate.class);
    private final Map<Mode, Map<String, Double>> coef;

    public ModeChoiceCalculator2017ImplAggregate(Purpose purpose, DataSet dataSet) {
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

        double probabilityAutoD;
        double probabilityAutoP;
        if (expsumNestAuto > 0) {
            probabilityAutoD =
                    (Math.exp(utilityAutoD / nestingCoefficientAutoModes) / expsumNestAuto) * (Math.exp(nestingCoefficientAutoModes * Math.log(expsumNestAuto)) / expsumTopLevel);
            probabilityAutoP =
                    (Math.exp(utilityAutoP / nestingCoefficientAutoModes) / expsumNestAuto) * (Math.exp(nestingCoefficientAutoModes * Math.log(expsumNestAuto)) / expsumTopLevel);
        } else {
            probabilityAutoD = 0.0;
            probabilityAutoP = 0.0;
        }

        double probabilityBus;
        double probabilityTrain;
        double probabilityTramMetro;
        double probabilityTaxi;
        if (expsumNestTransit > 0) {
            probabilityBus =
                    (Math.exp(utilityBus / nestingCoefficientPtModes) / expsumNestTransit) * (Math.exp(nestingCoefficientPtModes * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTrain =
                    (Math.exp(utilityTrain / nestingCoefficientPtModes) / expsumNestTransit) * (Math.exp(nestingCoefficientPtModes * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTramMetro =
                    (Math.exp(utilityTramMetro / nestingCoefficientPtModes) / expsumNestTransit) * (Math.exp(nestingCoefficientPtModes * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTaxi =
                    (Math.exp(utilityTaxi / nestingCoefficientPtModes) / expsumNestTransit) * (Math.exp(nestingCoefficientPtModes * Math.log(expsumNestTransit)) / expsumTopLevel);
        } else {
            probabilityBus = 0.0;
            probabilityTrain = 0.0;
            probabilityTramMetro = 0.0;
            probabilityTaxi = 0.0;
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
        probabilities.put(Mode.taxi, probabilityTaxi);
        probabilities.put(Mode.walk, probabilityWalk);
        return probabilities;
    }

    @Override
    public EnumMap<Mode, Double> calculateProbabilities(Purpose purpose, MitoAggregatePersona persona, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        EnumMap<Mode, Double> utilities = calculateUtilities(
                purpose, persona, originZone, destinationZone, travelTimes
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

        double probabilityAutoD;
        double probabilityAutoP;
        if (expsumNestAuto > 0) {
            probabilityAutoD =
                    (Math.exp(utilityAutoD / nestingCoefficientAutoModes) / expsumNestAuto) * (Math.exp(nestingCoefficientAutoModes * Math.log(expsumNestAuto)) / expsumTopLevel);
            probabilityAutoP =
                    (Math.exp(utilityAutoP / nestingCoefficientAutoModes) / expsumNestAuto) * (Math.exp(nestingCoefficientAutoModes * Math.log(expsumNestAuto)) / expsumTopLevel);
        } else {
            probabilityAutoD = 0.0;
            probabilityAutoP = 0.0;
        }

        double probabilityBus;
        double probabilityTrain;
        double probabilityTramMetro;
        double probabilityTaxi;
        if (expsumNestTransit > 0) {
            probabilityBus =
                    (Math.exp(utilityBus / nestingCoefficientPtModes) / expsumNestTransit) * (Math.exp(nestingCoefficientPtModes * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTrain =
                    (Math.exp(utilityTrain / nestingCoefficientPtModes) / expsumNestTransit) * (Math.exp(nestingCoefficientPtModes * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTramMetro =
                    (Math.exp(utilityTramMetro / nestingCoefficientPtModes) / expsumNestTransit) * (Math.exp(nestingCoefficientPtModes * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTaxi =
                    (Math.exp(utilityTaxi / nestingCoefficientPtModes) / expsumNestTransit) * (Math.exp(nestingCoefficientPtModes * Math.log(expsumNestTransit)) / expsumTopLevel);
        } else {
            probabilityBus = 0.0;
            probabilityTrain = 0.0;
            probabilityTramMetro = 0.0;
            probabilityTaxi = 0.0;
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
        probabilities.put(Mode.taxi, probabilityTaxi);
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
        MitoOccupationStatus occupationStatus = person.getMitoOccupationStatus();

        boolean hhHasBicycles = false;
        for (MitoPerson p : household.getPersons().values()) {
            if (p.getHasBicycle().get()) {
                hhHasBicycles = true;
            }
        }
        int economicStatus = household.getEconomicStatus();


        EnumMap<Mode, Double> generalizedCosts = calculateGeneralizedCosts(purpose, household, person,
                originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);


        EnumMap<Mode, Double> utilities = new EnumMap<>(Mode.class);
        for (Mode mode :coef.keySet()){
            //double distance = isMotorized(mode)? travelDistanceAuto : travelDistanceNMT;
            final Map<String, Double> modeCoef = coef.get(mode);
            double utility = modeCoef.get("intercept");
            if (isMale ==  1){
                utility += modeCoef.get("gender_male");
            } else {
                utility += modeCoef.get("gender_female");
            }
            switch (occupationStatus){
                case WORKER:
                    utility += modeCoef.get("is_employed");
                    break;
                case UNEMPLOYED:
                    utility += modeCoef.get("is_homemaker_or_other");
                    break;
                case STUDENT:
                    utility += modeCoef.get("is_student");
                    break;
                case RETIRED:
                    utility += modeCoef.get("is_retired_or_pensioner");
                    break;
            }
            if (age < 18){
                utility += modeCoef.get("age_0_to_17");
            } else if (age < 30){
                utility += modeCoef.get("age_18_to_29");
            } else if (age < 40){
                utility += modeCoef.get("age_30_to_39");
            } else if (age < 50){
                utility += modeCoef.get("age_40_to_49");
            } else if (age < 60) {
                utility += modeCoef.get("age_50_to_59");
            } else {
                utility += modeCoef.get("age_above_60");
            }
            switch(economicStatus){
                case 0:
                    utility += modeCoef.get("is_economic_status_very_low");
                    break;
                case 1:
                    utility += modeCoef.get("is_economic_status_low");
                    break;
                case 2:
                    utility += modeCoef.get("is_economic_status_medium");
                    break;
                case 3:
                    utility += modeCoef.get("is_economic_status_high");
                    break;
                case 4:
                    utility += modeCoef.get("is_economic_status_very_high");
                    break;
            }
            switch (hhSize){
                case 1:
                    utility += modeCoef.get("is_hh_one_person");
                    break;
                case 2:
                    utility += modeCoef.get("is_hh_two_persons");
                    break;
                case 3:
                    utility += modeCoef.get("is_hh_three_persons");
                    break;
                default:
                    utility += modeCoef.get("is_hh_four_or_more_persons");
                    break;
            }
            switch (hhAutos){
                case 0:
                    utility += modeCoef.get("hh_no_car");
                    break;
                case 1:
                    utility += modeCoef.get("hh_one_car");
                    break;
                default:
                    utility += modeCoef.get("hh_two_or_more_cars");
                    break;
            }
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

    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoAggregatePersona persona, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        EnumMap<Mode, Double> generalizedCosts = calculateGeneralizedCosts(purpose, persona,
                originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);

        EnumMap<Mode, Double> utilities = new EnumMap<>(Mode.class);
        for (Mode mode :coef.keySet()){
            //double distance = isMotorized(mode)? travelDistanceAuto : travelDistanceNMT;
            final Map<String, Double> modeCoef = coef.get(mode);
            double utility = modeCoef.get("intercept");

            utility += modeCoef.get("gender_male") * persona.getAggregateAttributes().get("p.MALE");
            utility += modeCoef.get("gender_female") * persona.getAggregateAttributes().get("p.FEMALE");

            utility += modeCoef.get("is_employed")*persona.getAggregateAttributes().get("p.occupation_WORKER");
            utility += modeCoef.get("is_student")*persona.getAggregateAttributes().get("p.occupation_STUDENT");
            utility += modeCoef.get("is_homemaker_or_other")*persona.getAggregateAttributes().get("p.occupation_UNEMPLOYED");

            utility += modeCoef.get("age_0_to_17")*persona.getAggregateAttributes().get("modeChoice.p.age_0_to_17");
            utility += modeCoef.get("age_18_to_29")*persona.getAggregateAttributes().get("modeChoice.p.age_18_to_29");
            utility += modeCoef.get("age_30_to_39")*persona.getAggregateAttributes().get("modeChoice.p.age_30_to_39");
            utility += modeCoef.get("age_40_to_49")*persona.getAggregateAttributes().get("modeChoice.p.age_40_to_49");
            utility += modeCoef.get("age_50_to_59")*persona.getAggregateAttributes().get("modeChoice.p.age_50_to_59");
            utility += modeCoef.get("age_above_60")*persona.getAggregateAttributes().get("modeChoice.p.age_above_60");

            // TODO: 5/15/2024 seems to be a bug on economic status. The values are from 1 to 5, instead of 0 to 4. It is reading one less than supposed to be
            //keep the original implementation. Value of zero is never taken, others are moved all by one! Commented is the proper version.
/*            utility += modeCoef.get("is_economic_status_very_low")*persona.getAggregateAttributes().get("hh.econStatus_1");
            utility += modeCoef.get("is_economic_status_low")*persona.getAggregateAttributes().get("hh.econStatus_2");
            utility += modeCoef.get("is_economic_status_medium")*persona.getAggregateAttributes().get("hh.econStatus_3");
            utility += modeCoef.get("is_economic_status_high")*persona.getAggregateAttributes().get("hh.econStatus_4");
            utility += modeCoef.get("is_economic_status_very_high")*persona.getAggregateAttributes().get("hh.econStatus_5");*/

            utility += modeCoef.get("is_economic_status_low")*persona.getAggregateAttributes().get("hh.econStatus_1");
            utility += modeCoef.get("is_economic_status_medium")*persona.getAggregateAttributes().get("hh.econStatus_2");
            utility += modeCoef.get("is_economic_status_high")*persona.getAggregateAttributes().get("hh.econStatus_3");
            utility += modeCoef.get("is_economic_status_very_high")*persona.getAggregateAttributes().get("hh.econStatus_4");

            utility += modeCoef.get("is_hh_one_person")*persona.getAggregateAttributes().get("hh.size_1");
            utility += modeCoef.get("is_hh_two_persons")*persona.getAggregateAttributes().get("hh.size_2");
            utility += modeCoef.get("is_hh_three_persons")*persona.getAggregateAttributes().get("hh.size_3");
            utility += modeCoef.get("is_hh_four_or_more_persons")*persona.getAggregateAttributes().get("hh.size_4");
            utility += modeCoef.get("is_hh_four_or_more_persons")*persona.getAggregateAttributes().get("hh.size_5");

            double propZeroCars = 1-persona.getAggregateAttributes().get("hh.cars_1") - persona.getAggregateAttributes().get("hh.cars_2") -
                    persona.getAggregateAttributes().get("hh.cars_3");

            utility += modeCoef.get("hh_no_car")*propZeroCars;
            utility += modeCoef.get("hh_one_car")*persona.getAggregateAttributes().get("hh.cars_1");
            utility += modeCoef.get("hh_two_or_more_cars")*persona.getAggregateAttributes().get("hh.cars_2");
            utility += modeCoef.get("hh_two_or_more_cars")*persona.getAggregateAttributes().get("hh.cars_3");

            utility += modeCoef.get("hh_has_bike")*persona.getAggregateAttributes().get("hh.bikes_0");
            utility += modeCoef.get("hh_no_bike")*persona.getAggregateAttributes().get("hh.bikes_1");

            double gc = generalizedCosts.get(mode);
            utility += modeCoef.get("exp_generalized_time_min") * Math.exp(gc * modeCoef.get("alpha"));

            utilities.put(mode, utility);
        }

        return utilities;
    }

    @Override
    public EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone,
                                                           MitoZone destinationZone, TravelTimes travelTimes,
                                                           double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        double timeAutoD = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car");
        double timeAutoP = timeAutoD;
        double timeBus = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "bus");
        double timeTrain = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "train");
        double timeTramMetro = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "tramMetro");
        double timeTaxi = timeAutoD;

        int monthlyIncome_EUR = household.getMonthlyIncome_EUR();

        double gcAutoD;
        double gcAutoP;
        double gcBus;
        double gcTrain;
        double gcTramMetro;
        double gcTaxi;
        double gcWalk = travelDistanceNMT / SPEED_WALK_KMH * 60;
        double gcBicycle = travelDistanceNMT / SPEED_BICYCLE_KMH * 60;

        if (monthlyIncome_EUR <= 1500) {
            gcAutoD = timeAutoD + (travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm")) / coef.get(Mode.autoDriver).get("vot_under_1500_eur_min");
            gcAutoP = timeAutoP + (travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm")) / coef.get(Mode.autoPassenger).get("vot_under_1500_eur_min");
            gcBus = timeBus + (travelDistanceAuto * coef.get(Mode.bus).get("costPerKm")) / coef.get(Mode.bus).get("vot_under_1500_eur_min");
            gcTrain = timeTrain + (travelDistanceAuto * coef.get(Mode.train).get("costPerKm")) / coef.get(Mode.train).get("vot_under_1500_eur_min");
            gcTramMetro = timeTramMetro + (travelDistanceAuto * coef.get(Mode.tramOrMetro).get("costPerKm")) / coef.get(Mode.tramOrMetro).get("vot_under_1500_eur_min");
            gcTaxi = timeTaxi + (travelDistanceAuto * coef.get(Mode.taxi).get("costPerKm")) / coef.get(Mode.taxi).get("vot_under_1500_eur_min");
        } else if (monthlyIncome_EUR <= 5600) {
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

    public EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose, MitoAggregatePersona persona, MitoZone originZone,
                                                           MitoZone destinationZone, TravelTimes travelTimes,
                                                           double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        double timeAutoD = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car");
        double timeAutoP = timeAutoD;
        double timeBus = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "bus");
        double timeTrain = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "train");
        double timeTramMetro = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "tramMetro");
        double timeTaxi = timeAutoD;

        double share_income_1500 = persona.getAggregateAttributes().get("hh.income_1500");
        double share_income_1500_5600 = persona.getAggregateAttributes().get("hh.income_1500_5600");
        double share_income_EUR_5600 = persona.getAggregateAttributes().get("hh.income_5600");

        double gcAutoD;
        double gcAutoP;
        double gcBus;
        double gcTrain;
        double gcTramMetro;
        double gcTaxi;
        double gcWalk = travelDistanceNMT / SPEED_WALK_KMH * 60;
        double gcBicycle = travelDistanceNMT / SPEED_BICYCLE_KMH * 60;

        double vot_autoDriver = coef.get(Mode.autoDriver).get("vot_under_1500_eur_min") * share_income_1500 +
                coef.get(Mode.autoDriver).get("vot_1500_to_5600_eur_min") * share_income_1500_5600+
                coef.get(Mode.autoDriver).get("vot_above_5600_eur_min") * share_income_EUR_5600;

        double vot_autoPassenger = coef.get(Mode.autoPassenger).get("vot_under_1500_eur_min") * share_income_1500 +
                coef.get(Mode.autoPassenger).get("vot_1500_to_5600_eur_min") * share_income_1500_5600+
                coef.get(Mode.autoPassenger).get("vot_above_5600_eur_min") * share_income_EUR_5600;

        double vot_bus = coef.get(Mode.bus).get("vot_under_1500_eur_min") * share_income_1500 +
                coef.get(Mode.bus).get("vot_1500_to_5600_eur_min") * share_income_1500_5600+
                coef.get(Mode.bus).get("vot_above_5600_eur_min") * share_income_EUR_5600;

        double vot_train = coef.get(Mode.train).get("vot_under_1500_eur_min") * share_income_1500 +
                coef.get(Mode.train).get("vot_1500_to_5600_eur_min") * share_income_1500_5600+
                coef.get(Mode.train).get("vot_above_5600_eur_min") * share_income_EUR_5600;

        double vot_tramOrMetro = coef.get(Mode.tramOrMetro).get("vot_under_1500_eur_min") * share_income_1500 +
                coef.get(Mode.tramOrMetro).get("vot_1500_to_5600_eur_min") * share_income_1500_5600+
                coef.get(Mode.tramOrMetro).get("vot_above_5600_eur_min") * share_income_EUR_5600;

        double vot_taxi = coef.get(Mode.taxi).get("vot_under_1500_eur_min") * share_income_1500 +
                coef.get(Mode.taxi).get("vot_1500_to_5600_eur_min") * share_income_1500_5600+
                coef.get(Mode.taxi).get("vot_above_5600_eur_min") * share_income_EUR_5600;

        gcAutoD = timeAutoD + (travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm")) / vot_autoDriver;
        gcAutoP = timeAutoP + (travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm")) / vot_autoPassenger;
        gcBus = timeBus + (travelDistanceAuto * coef.get(Mode.bus).get("costPerKm")) / vot_bus;
        gcTrain = timeTrain + (travelDistanceAuto * coef.get(Mode.train).get("costPerKm")) / vot_train;
        gcTramMetro = timeTramMetro + (travelDistanceAuto * coef.get(Mode.tramOrMetro).get("costPerKm")) / vot_tramOrMetro;
        gcTaxi = timeTaxi + (travelDistanceAuto * coef.get(Mode.taxi).get("costPerKm")) / vot_taxi;


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

    private static boolean isMotorized(Mode mode) {
        if (mode.equals(Mode.walk) || mode.equals(Mode.bicycle)){
            return false;
        } else {
            return true;
        }
    }
}
