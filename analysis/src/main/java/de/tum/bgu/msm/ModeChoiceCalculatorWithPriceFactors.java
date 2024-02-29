package de.tum.bgu.msm;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
import de.tum.bgu.msm.modules.ModeChoiceCalculator2017Impl;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.resources.Resources;

import java.util.EnumMap;
import java.util.Map;

public class ModeChoiceCalculatorWithPriceFactors extends ModeChoiceCalculator2017Impl {

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
        double gcTaxi = generalizedCosts.get(Mode.taxi);

        int monthlyIncome_EUR = household.getMonthlyIncome_EUR();

        if (monthlyIncome_EUR <= 1500) {
            gcAutoD += travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm") * (carPriceFactor - 1) / coef.get(Mode.autoDriver).get("vot_under_1500_eur_min");
            gcAutoP += travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm") * (carPriceFactor - 1) /coef.get(Mode.autoPassenger).get("vot_under_1500_eur_min");
            gcBus += travelDistanceAuto * coef.get(Mode.bus).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.bus).get("vot_under_1500_eur_min");
            gcTrain += travelDistanceAuto * coef.get(Mode.train).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.train).get("vot_under_1500_eur_min");
            gcTramMetro += travelDistanceAuto * coef.get(Mode.tramOrMetro).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.tramOrMetro).get("vot_under_1500_eur_min");
            gcTaxi += travelDistanceAuto * coef.get(Mode.taxi).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.taxi).get("vot_under_1500_eur_min");
        } else if (monthlyIncome_EUR <= 5600) {
            gcAutoD += travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm") * (carPriceFactor - 1) /coef.get(Mode.autoDriver).get("vot_1500_to_5600_eur_min");
            gcAutoP += travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm") * (carPriceFactor - 1) / coef.get(Mode.autoPassenger).get("vot_1500_to_5600_eur_min");
            gcBus += travelDistanceAuto * coef.get(Mode.bus).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.bus).get("vot_1500_to_5600_eur_min");
            gcTrain += travelDistanceAuto * coef.get(Mode.train).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.train).get("vot_1500_to_5600_eur_min");
            gcTramMetro += travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.tramOrMetro).get("vot_1500_to_5600_eur_min");
            gcTaxi += travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.taxi).get("vot_under_1500_eur_min");
        } else {
            gcAutoD +=  travelDistanceAuto * coef.get(Mode.autoDriver).get("costPerKm") * (carPriceFactor - 1) / coef.get(Mode.autoDriver).get("vot_above_5600_eur_min");
            gcAutoP +=  travelDistanceAuto * coef.get(Mode.autoPassenger).get("costPerKm") * (carPriceFactor - 1) / coef.get(Mode.autoPassenger).get("vot_above_5600_eur_min");
            gcBus +=  travelDistanceAuto * coef.get(Mode.bus).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.bus).get("vot_above_5600_eur_min");
            gcTrain +=  travelDistanceAuto * coef.get(Mode.train).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.train).get("vot_above_5600_eur_min");
            gcTramMetro += travelDistanceAuto * coef.get(Mode.tramOrMetro).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.tramOrMetro).get("vot_above_5600_eur_min");
            gcTaxi += travelDistanceAuto * coef.get(Mode.taxi).get("costPerKm") * (ptPriceFactor - 1) / coef.get(Mode.taxi).get("vot_above_5600_eur_min");
        }

        generalizedCosts.put(Mode.autoDriver, gcAutoD);
        generalizedCosts.put(Mode.autoPassenger, gcAutoP);
        generalizedCosts.put(Mode.bus, gcBus);
        generalizedCosts.put(Mode.train, gcTrain);
        generalizedCosts.put(Mode.tramOrMetro, gcTramMetro);
        generalizedCosts.put(Mode.taxi, gcTaxi);
        return generalizedCosts;

    }
}
