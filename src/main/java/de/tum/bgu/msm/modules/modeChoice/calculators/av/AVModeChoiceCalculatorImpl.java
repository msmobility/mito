package de.tum.bgu.msm.modules.modeChoice.calculators.av;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculatorImpl;

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
    public double[] calculateProbabilities(
            Purpose purpose,
            MitoHousehold household,
            MitoPerson person,
            MitoZone originZone,
            MitoZone destinationZone,
            TravelTimes travelTimes,
            double travelDistanceAuto,
            double travelDistanceNMT,
            double peakHour_s) {


        double[] utilities = baseCalculator.calculateUtilities(
                purpose,
                household,
                person,
                originZone,
                destinationZone,
                travelTimes,
                travelDistanceAuto,
                travelDistanceNMT,
                peakHour_s);

        double[] baseProbs = baseCalculator.calculateProbabilities(
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

        double[] generalizedCosts = calculateGeneralizedCosts(
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

        final double utilityAutoD = utilities[0];
        final double utilityAutoP = utilities[1];
        final double utilityBicycle = utilities[2];
        final double utilityBus = utilities[3];
        final double utilityTrain = utilities[4];
        final double utilityTramMetro = utilities[5];
        final double utilityWalk = utilities[6];

        final double gcAutoD = generalizedCosts[0];
        final double gcAutoP = generalizedCosts[1];
        final double gcBus = generalizedCosts[3];
        final double gcTrain = generalizedCosts[4];
        final double gcTramMetro = generalizedCosts[5];
        final double gcSharedAV = generalizedCosts[6];
        final double gcPrivateAV = generalizedCosts[7];

        double logsumAuto = Math.log(Math.exp(utilityAutoD / nestingCoefficient) + Math.exp(utilityAutoP / nestingCoefficient));
        double logsumTransit = Math.log(Math.exp(utilities[3] / nestingCoefficient) + Math.exp(utilities[4] / nestingCoefficient)
                + Math.exp(utilities[5] / nestingCoefficient));

        double baseProbabilityAutoNest = Math.exp(nestingCoefficient * logsumAuto) / (Math.exp(nestingCoefficient * logsumAuto) + Math.exp(utilityBicycle) + Math.exp(utilityWalk) + Math.exp(nestingCoefficient * logsumTransit));
        double baseProbabilityTransitNest = Math.exp(nestingCoefficient * logsumTransit) / (Math.exp(nestingCoefficient * logsumAuto) + Math.exp(utilityBicycle) + Math.exp(utilityWalk) + Math.exp(nestingCoefficient * logsumTransit));

        int purpIdx = purpose.ordinal();


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
        double probabilityBicycle = baseProbs[2] / sumOfRelativeExpOfUtilities;
        double probabilityWalk = baseProbs[6] / sumOfRelativeExpOfUtilities;

        return new double[]{
                probabilityAutoD,
                probabilityAutoP,
                probabilityBicycle,
                probabilityBus,
                probabilityTrain,
                probabilityTramMetro,
                probabilityWalk,
                probabilityPrivateAV,
                probabilitySharedAV
        };
    }

    @Override
    public double[] calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public double[] calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        int monthlyIncome_EUR = household.getMonthlyIncome_EUR();
        int purpIdx = purpose.ordinal();
        double timeAutoD = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car");

        double[] baseGeneralizedCosts = baseCalculator.calculateGeneralizedCosts(
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

        double[] generalizedCosts = new double[baseGeneralizedCosts.length + 2];
        System.arraycopy(baseGeneralizedCosts, 0, generalizedCosts, 0, baseGeneralizedCosts.length);
        generalizedCosts[generalizedCosts.length-2] = gcPrivateAV;
        generalizedCosts[generalizedCosts.length-1] = gcSharedAV;
        return generalizedCosts;
    }
}
