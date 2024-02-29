package de.tum.bgu.msm.scenarios.av;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.ModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;

import java.util.EnumMap;

public class AVModeChoiceCalculatorImpl implements ModeChoiceCalculator {

    private ModeChoiceCalculator baseCalculator;

    private final static double nestingCoefficient = 0.25;

    private final static double sharedAVCostEurosPerKm = 1.20;

    private final static double fuelCostEurosPerKm = 0.07;

    private final static double[][] betaGeneralizedCost = {
            //HBW
            {-0.0088, -0.0088, 0.0, -0.0088, -0.0088, -0.0088, 0.00},
            //HBE
            {-0.0025, -0.0025, 0.0, -0.0025, -0.0025, -0.0025, 0.0},
            //HBS
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBO
            {-0.0012, -0.0012, 0.0, -0.0012, -0.0012, -0.0012, 0.00},
            //NHBW
            {-0.0034, -0.0034, 0.0, -0.0034, -0.0034, -0.0034, 0.0},
            //NHBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };

    private final static double[] VOT1500_privateAV = {3.47, 3.47, 2.45, 2.45, 2.45, 2.45};
    private final static double[] VOT5600_privateAV = {6.71, 6.71, 4.73, 4.73, 4.73, 4.73};
    private final static double[] VOT7000_privateAV = {9.11, 9.11, 6.42, 6.42, 6.42, 6.42};

    private final static double[] VOT1500_sharedAV = {7.98, 7.98, 4.68, 4.68, 4.68, 4.68};
    private final static double[] VOT5600_sharedAV = {15.43, 15.43, 9.05, 9.05, 9.05, 9.05};
    private final static double[] VOT7000_sharedAV = {20.97, 20.97, 12.30, 12.30, 12.30, 12.30};

    public AVModeChoiceCalculatorImpl(ModeChoiceCalculator baseCalculator) {
        this.baseCalculator = baseCalculator;
    }

    public AVModeChoiceCalculatorImpl() {
        this.baseCalculator = new ModeChoiceCalculatorImpl();
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


        EnumMap<Mode, Double> utilities = baseCalculator.calculateUtilities(
                purpose,
                household,
                person,
                originZone,
                destinationZone,
                travelTimes,
                travelDistanceAuto,
                travelDistanceNMT,
                peakHour_s);

        EnumMap<Mode, Double> baseProbs = baseCalculator.calculateProbabilities(
                purpose,
                household,
                person,
                originZone,
                destinationZone,
                travelTimes,
                travelDistanceAuto,
                travelDistanceNMT,
                peakHour_s
        );

        EnumMap<Mode, Double> generalizedCosts = calculateGeneralizedCosts(
                purpose,
                household,
                person,
                originZone,
                destinationZone,
                travelTimes,
                travelDistanceAuto,
                travelDistanceNMT,
                peakHour_s
        );

        final double utilityAutoD = utilities.get(Mode.autoDriver);
        final double utilityAutoP = utilities.get(Mode.autoPassenger);
        final double utilityBicycle = utilities.get(Mode.bicycle);
        final double utilityBus = utilities.get(Mode.bus);
        final double utilityTrain = utilities.get(Mode.train);
        final double utilityTramMetro = utilities.get(Mode.tramOrMetro);
        final double utilityWalk = utilities.get(Mode.walk);

        final double gcAutoD = generalizedCosts.get(Mode.autoDriver);
        final double gcAutoP = generalizedCosts.get(Mode.autoPassenger);
        final double gcBus = generalizedCosts.get(Mode.bus);
        final double gcTrain = generalizedCosts.get(Mode.train);
        final double gcTramMetro = generalizedCosts.get(Mode.tramOrMetro);
        final double gcSharedAV = generalizedCosts.get(Mode.sharedAV);
        final double gcPrivateAV = generalizedCosts.get(Mode.privateAV);

        double logsumAuto = Math.log(Math.exp(utilityAutoD / nestingCoefficient) + Math.exp(utilityAutoP / nestingCoefficient));
        double logsumTransit = Math.log(Math.exp(utilityBus / nestingCoefficient) + Math.exp(utilityTrain / nestingCoefficient)
                + Math.exp(utilityTramMetro / nestingCoefficient));

        double baseProbabilityAutoNest = Math.exp(nestingCoefficient * logsumAuto) / (Math.exp(nestingCoefficient * logsumAuto) + Math.exp(utilityBicycle) + Math.exp(utilityWalk) + Math.exp(nestingCoefficient * logsumTransit));
        double baseProbabilityTransitNest = Math.exp(nestingCoefficient * logsumTransit) / (Math.exp(nestingCoefficient * logsumAuto) + Math.exp(utilityBicycle) + Math.exp(utilityWalk) + Math.exp(nestingCoefficient * logsumTransit));

        int purpIdx;
        if (purpose.equals(Purpose.HBR)){
            purpIdx = Purpose.HBO.ordinal();
            //there is no mode choice for HBR trips yet
        } else {
            purpIdx = purpose.ordinal();
        }


        // Numerator of Auto and Transit probabilities (exp of utilities)
        double relativeExpOfAutoNestUtility = baseProbabilityAutoNest * Math.pow((1 + Math.exp(betaGeneralizedCost[purpIdx][0] * (gcPrivateAV - gcAutoD) / nestingCoefficient) + Math.exp((utilityAutoP - utilityAutoD) / nestingCoefficient)) / (1 + Math.exp((utilityAutoP - utilityAutoD) / nestingCoefficient)), nestingCoefficient);
        double relativeExpOfTransitNestUtility = baseProbabilityTransitNest * Math.pow((1 + Math.exp(betaGeneralizedCost[purpIdx][3] * (gcSharedAV - gcBus) / nestingCoefficient) + Math.exp((utilityTrain - utilityBus) / nestingCoefficient) + Math.exp((utilityTramMetro - utilityBus) / nestingCoefficient)) / (1 + Math.exp((utilityTrain - utilityBus) / nestingCoefficient) + Math.exp((utilityTramMetro - utilityBus) / nestingCoefficient)), nestingCoefficient);

        // Denominator (sum of exp of utilities)
        double sumOfRelativeExpOfUtilities = relativeExpOfAutoNestUtility + relativeExpOfTransitNestUtility + 1 - baseProbabilityAutoNest - baseProbabilityTransitNest;

        // Individual probabilities
        double probabilityAutoNest = relativeExpOfAutoNestUtility / sumOfRelativeExpOfUtilities;
        double probabilityPrivateAV = probabilityAutoNest * Math.exp(betaGeneralizedCost[purpIdx][0] * (gcPrivateAV - gcAutoD) / nestingCoefficient) / (1 + Math.exp((betaGeneralizedCost[purpIdx][0] * (gcPrivateAV - gcAutoD)) / nestingCoefficient) + Math.exp((utilityAutoP - utilityAutoD) / nestingCoefficient));
        double probabilityAutoD = probabilityAutoNest / (1 + Math.exp(betaGeneralizedCost[purpIdx][0] * (gcPrivateAV - gcAutoD) / nestingCoefficient) + Math.exp((utilityAutoP - utilityAutoD) / nestingCoefficient));
        double probabilityAutoP = probabilityAutoNest / (1 + Math.exp(betaGeneralizedCost[purpIdx][1] * (gcPrivateAV - gcAutoP) / nestingCoefficient) + Math.exp((utilityAutoD - utilityAutoP) / nestingCoefficient));
        double probabilityTransitNest = relativeExpOfTransitNestUtility / sumOfRelativeExpOfUtilities;
        double probabilitySharedAV = probabilityTransitNest * Math.exp(betaGeneralizedCost[purpIdx][3] * (gcSharedAV - gcBus) / nestingCoefficient) / (1 + Math.exp(betaGeneralizedCost[purpIdx][3] * (gcSharedAV - gcBus) / nestingCoefficient) + Math.exp((utilityTrain - utilityBus) / nestingCoefficient) + Math.exp((utilityTramMetro - utilityBus) / nestingCoefficient));
        double probabilityBus = probabilityTransitNest / (1 + Math.exp(betaGeneralizedCost[purpIdx][3] * (gcSharedAV - gcBus) / nestingCoefficient) + Math.exp((utilityTrain - utilityBus) / nestingCoefficient) + Math.exp((utilityTramMetro - utilityBus) / nestingCoefficient));
        double probabilityTrain = probabilityTransitNest / (1 + Math.exp(betaGeneralizedCost[purpIdx][4] * (gcSharedAV - gcTrain) / nestingCoefficient) + Math.exp((utilityBus - utilityTrain) / nestingCoefficient) + Math.exp((utilityTramMetro - utilityTrain) / nestingCoefficient));
        double probabilityTramMetro = probabilityTransitNest / (1 + Math.exp(betaGeneralizedCost[purpIdx][5] * (gcSharedAV - gcTramMetro) / nestingCoefficient) + Math.exp((utilityBus - utilityTramMetro) / nestingCoefficient) + Math.exp((utilityTrain - utilityTramMetro) / nestingCoefficient));
        double probabilityBicycle = baseProbs.get(Mode.bicycle) / sumOfRelativeExpOfUtilities;
        double probabilityWalk = baseProbs.get(Mode.walk) / sumOfRelativeExpOfUtilities;

        baseProbs.put(Mode.autoDriver, probabilityAutoD);
        baseProbs.put(Mode.autoPassenger, probabilityAutoP);
        baseProbs.put(Mode.bicycle, probabilityBicycle);
        baseProbs.put(Mode.bus, probabilityBus);
        baseProbs.put(Mode.train, probabilityTrain);
        baseProbs.put(Mode.tramOrMetro, probabilityTramMetro);
        baseProbs.put(Mode.walk, probabilityWalk);
        baseProbs.put(Mode.privateAV, probabilityPrivateAV);
        baseProbs.put(Mode.sharedAV, probabilitySharedAV);
        return baseProbs;
    }

    @Override
    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        int monthlyIncome_EUR = household.getMonthlyIncome_EUR();
        int purpIdx;
        if (purpose.equals(Purpose.HBR)){
            purpIdx = Purpose.HBO.ordinal();
            //there is no mode choice for HBR trips yet
        } else {
            purpIdx = purpose.ordinal();
        }
        double timeAutoD = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car");

        EnumMap<Mode, Double> baseGeneralizedCosts = baseCalculator.calculateGeneralizedCosts(
                purpose,
                household,
                person,
                originZone,
                destinationZone,
                travelTimes,
                travelDistanceAuto,
                travelDistanceNMT,
                peakHour_s
        );

        double gcSharedAV;
        double gcPrivateAV;
        if (monthlyIncome_EUR <= 1500) {
            // change only in VOT
            gcPrivateAV = timeAutoD + (travelDistanceAuto * fuelCostEurosPerKm) / VOT1500_privateAV[purpIdx];
            // change in VOT and cost
            gcSharedAV = timeAutoD + (travelDistanceAuto * sharedAVCostEurosPerKm) / VOT1500_sharedAV[purpIdx];
        } else if (monthlyIncome_EUR <= 5600) {
            // change only in VOT
            gcPrivateAV = timeAutoD + (travelDistanceAuto * fuelCostEurosPerKm) / VOT5600_privateAV[purpIdx];
            // change in VOT and cost
            gcSharedAV = timeAutoD + (travelDistanceAuto * sharedAVCostEurosPerKm) / VOT5600_sharedAV[purpIdx];
        } else {
            // change only in VOT
            gcPrivateAV = timeAutoD + (travelDistanceAuto * fuelCostEurosPerKm) / VOT7000_privateAV[purpIdx];
            // change in VOT and cost
            gcSharedAV = timeAutoD + (travelDistanceAuto * sharedAVCostEurosPerKm) / VOT7000_sharedAV[purpIdx];
        }

        baseGeneralizedCosts.put(Mode.privateAV, gcPrivateAV);
        baseGeneralizedCosts.put(Mode.sharedAV, gcSharedAV);
        return baseGeneralizedCosts;
    }
}
