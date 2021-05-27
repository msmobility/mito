package de.tum.bgu.msm.analysis;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculator2008Impl;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculatorImpl;
import de.tum.bgu.msm.resources.Resources;

import java.util.EnumMap;
import java.util.Map;

public class ModeChoiceCalculatorWithPriceFactors extends ModeChoiceCalculator2008Impl {

    private final static double fuelCostEurosPerKm = 0.07;
    private final static double transitFareEurosPerKm = 0.12;

    //HBW    HBE,    HBS,    HBO,    NHBW,    NHBO
    //0     1       2       3       4          5

    private final ModeChoiceCalculator base;
    private final Map<Mode, Map<String, Double>> coef;
    private double carPriceFactor;
    private double ptPriceFactor;

    public ModeChoiceCalculatorWithPriceFactors(ModeChoiceCalculator base, double carPriceFactor, double ptPriceFactor, Purpose purpose, DataSet dataSet) {
        super(purpose, dataSet);
        this.base = base;
        this.carPriceFactor = carPriceFactor;
        this.ptPriceFactor = ptPriceFactor;
        this.coef = new ModeChoiceCoefficientReader(dataSet, purpose, Resources.instance.getModeChoiceCoefficients(purpose)).readCoefficientsForThisPurpose();
    }

    @Override
    public EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        EnumMap<Mode, Double> generalizedCosts = base.calculateGeneralizedCosts(purpose, household, person, originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);


        double gcAutoD = generalizedCosts.get(Mode.autoDriver);
        double gcAutoP = generalizedCosts.get(Mode.autoPassenger);
        double gcBus = generalizedCosts.get(Mode.bus);
        double gcTrain = generalizedCosts.get(Mode.train);
        double gcTramMetro = generalizedCosts.get(Mode.tramOrMetro);

        int monthlyIncome_EUR = household.getMonthlyIncome_EUR();

        if (monthlyIncome_EUR <= 1500) {
            gcAutoD += travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) / coef.get(Mode.autoDriver).get("vot_under_1500");
            gcAutoP += travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) /coef.get(Mode.autoPassenger).get("vot_under_1500");
            gcBus += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / coef.get(Mode.bus).get("vot_under_1500");
            gcTrain += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / coef.get(Mode.train).get("vot_under_1500");
            gcTramMetro += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / coef.get(Mode.tramOrMetro).get("vot_under_1500");
        } else if (monthlyIncome_EUR <= 5600) {
            gcAutoD += travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) /coef.get(Mode.autoDriver).get("vot_1500_to_5600");
            gcAutoP += travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) / coef.get(Mode.autoPassenger).get("vot_1500_to_5600");
            gcBus += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / coef.get(Mode.bus).get("vot_1500_to_5600");
            gcTrain += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / coef.get(Mode.train).get("vot_1500_to_5600");
            gcTramMetro += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / coef.get(Mode.tramOrMetro).get("vot_1500_to_5600");
        } else {
            gcAutoD +=  travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) / coef.get(Mode.autoDriver).get("vot_above_5600");
            gcAutoP +=  travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) / coef.get(Mode.autoPassenger).get("vot_above_5600");
            gcBus +=  travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / coef.get(Mode.bus).get("vot_above_5600");
            gcTrain +=  travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / coef.get(Mode.train).get("vot_above_5600");
            gcTramMetro += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / coef.get(Mode.tramOrMetro).get("vot_above_5600");
        }

        generalizedCosts.put(Mode.autoDriver, gcAutoD);
        generalizedCosts.put(Mode.autoPassenger, gcAutoP);
        generalizedCosts.put(Mode.bicycle, 0.);
        generalizedCosts.put(Mode.bus, gcBus);
        generalizedCosts.put(Mode.train, gcTrain);
        generalizedCosts.put(Mode.tramOrMetro, gcTramMetro);
        generalizedCosts.put(Mode.walk, 0.);
        return generalizedCosts;

    }
}
