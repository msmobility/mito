package de.tum.bgu.msm.analysis;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculatorImpl;

import java.util.EnumMap;

public class ModeChoiceCalculatorWithPriceFactors extends ModeChoiceCalculatorImpl {

    private final static double fuelCostEurosPerKm = 0.07;
    private final static double transitFareEurosPerKm = 0.12;

    //HBW    HBE,    HBS,    HBO,    NHBW,    NHBO
    //0     1       2       3       4          5
    private final static double[] VOT1500_autoD = {4.63 / 60., 4.63 / 60, 3.26 / 60, 3.26 / 60, 3.26 / 60, 3.26 / 60};
    private final static double[] VOT5600_autoD = {8.94 / 60, 8.94 / 60, 6.30 / 60, 6.30 / 60, 6.30 / 60, 6.30 / 60};
    private final static double[] VOT7000_autoD = {12.15 / 60, 12.15 / 60, 8.56 / 60, 8.56 / 60, 8.56 / 60, 8.56 / 60};

    private final static double[] VOT1500_autoP = {7.01 / 60, 7.01 / 60, 4.30 / 60, 4.30 / 60, 4.30 / 60, 4.30 / 60};
    private final static double[] VOT5600_autoP = {13.56 / 60, 13.56 / 60, 8.31 / 60, 8.31 / 60, 8.31 / 60, 8.31 / 60};
    private final static double[] VOT7000_autoP = {18.43 / 60, 18.43 / 60, 11.30 / 60, 11.30 / 60, 11.30 / 60, 11.30 / 60};

    private final static double[] VOT1500_transit = {8.94 / 60, 8.94 / 60, 5.06 / 60, 5.06 / 60, 5.06 / 60, 5.06 / 60};
    private final static double[] VOT5600_transit = {17.30 / 60, 17.30 / 60, 9.78 / 60, 9.78 / 60, 9.78 / 60, 9.78 / 60};
    private final static double[] VOT7000_transit = {23.50 / 60, 23.50 / 60, 13.29 / 60, 13.29 / 60, 13.29 / 60, 13.29 / 60};

    private final ModeChoiceCalculatorImpl base;
    private double carPriceFactor;
    private double ptPriceFactor;

    public ModeChoiceCalculatorWithPriceFactors(ModeChoiceCalculatorImpl base, double carPriceFactor, double ptPriceFactor) {
        super();
        this.base = base;
        this.carPriceFactor = carPriceFactor;
        this.ptPriceFactor = ptPriceFactor;
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
        int purpIdx = purpose.ordinal();

        if (monthlyIncome_EUR <= 1500) {
            gcAutoD += travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) / VOT1500_autoD[purpIdx];
            gcAutoP += travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) / VOT1500_autoP[purpIdx];
            gcBus += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / VOT1500_transit[purpIdx];
            gcTrain += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / VOT1500_transit[purpIdx];
            gcTramMetro += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / VOT1500_transit[purpIdx];
        } else if (monthlyIncome_EUR <= 5600) {
            gcAutoD += travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) / VOT5600_autoD[purpIdx];
            gcAutoP += travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) / VOT5600_autoP[purpIdx];
            gcBus += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / VOT5600_transit[purpIdx];
            gcTrain += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / VOT5600_transit[purpIdx];
            gcTramMetro += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / VOT5600_transit[purpIdx];
        } else {
            gcAutoD +=  travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) / VOT7000_autoD[purpIdx];
            gcAutoP +=  travelDistanceAuto * fuelCostEurosPerKm * (carPriceFactor - 1) / VOT7000_autoP[purpIdx];
            gcBus +=  travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / VOT7000_transit[purpIdx];
            gcTrain +=  travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / VOT7000_transit[purpIdx];
            gcTramMetro += travelDistanceAuto * transitFareEurosPerKm * (ptPriceFactor - 1) / VOT7000_transit[purpIdx];
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
