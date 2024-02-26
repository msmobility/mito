package de.tum.bgu.msm.scenarios.drtNoise;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import java.util.EnumMap;

public class DrtAutoNestModeChoiceCalculatorImpl implements ModeChoiceCalculator {

    private final ModeChoiceCalculator baseCalculator;

    private final PreparedGeometry serviceArea;

    private final static double NESTING_COEFFICIENT = 0.25;

    private final static double SERVICE_COST_PER_KM = 0.27;
    private final static double BASE_FARE = 2;
    private final static double DETOUR_FACTOR = 1.284;
    private final static double WAITING_TIME = 5;

        private final static double[] VOT1500_privateAV = {3.47 / 60, 3.47 / 60, 2.45 / 60, 2.45 / 60, 2.45 / 60, 2.45 / 60};
    private final static double[] VOT5600_privateAV = {6.71 / 60, 6.71 / 60, 4.73 / 60, 4.73 / 60, 4.73 / 60, 4.73 / 60};
    private final static double[] VOT7000_privateAV = {9.11 / 60, 9.11 / 60, 6.42 / 60, 6.42 / 60, 6.42 / 60, 6.42 / 60};

//    private final static double[] VOT1500_privateAV = {7.01 / 60, 7.01 / 60, 4.30 / 60, 4.30 / 60, 4.30 / 60, 4.30 / 60};
//    private final static double[] VOT5600_privateAV = {13.56 / 60, 13.56 / 60, 8.31 / 60, 8.31 / 60, 8.31 / 60, 8.31 / 60};
//    private final static double[] VOT7000_privateAV = {18.43 / 60, 18.43 / 60, 11.30 / 60, 11.30 / 60, 11.30 / 60, 11.30 / 60};

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

    private final static double[][] betaGeneralizedCost_Squared = {
            //HBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {-0.0000068, -0.0000068, 0.0, -0.0000068, -0.0000068, -0.0000068, 0.0},
            //HBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBO
            {-0.000017, -0.000017, 0.0, -0.000017, -0.000017, -0.000017, 0.0}
    };

    public DrtAutoNestModeChoiceCalculatorImpl(ModeChoiceCalculator baseCalculator, Geometry serviceArea) {
        this.baseCalculator = baseCalculator;
        this.serviceArea = PreparedGeometryFactory.prepare(serviceArea);
    }


    @Override
    public EnumMap<Mode, Double> calculateProbabilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

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
        final double utilityPooledTaxi = utilities.get(Mode.pooledTaxi);

        double expsumNestAuto = Math.exp(utilityAutoD / NESTING_COEFFICIENT) + Math.exp(utilityAutoP / NESTING_COEFFICIENT) + Math.exp(utilityPooledTaxi / NESTING_COEFFICIENT);
        double expsumNestTransit = Math.exp(utilityBus / NESTING_COEFFICIENT) + Math.exp(utilityTrain / NESTING_COEFFICIENT) + Math.exp(utilityTramMetro / NESTING_COEFFICIENT);
        double expsumTopLevel = Math.exp(NESTING_COEFFICIENT * Math.log(expsumNestAuto)) + Math.exp(utilityBicycle) + Math.exp(utilityWalk) + Math.exp(NESTING_COEFFICIENT * Math.log(expsumNestTransit));

        double probabilityAutoD;
        double probabilityAutoP;
        double probabilityPooledTaxi;
        if (expsumNestAuto > 0) {
            probabilityAutoD = (Math.exp(utilityAutoD / NESTING_COEFFICIENT) / expsumNestAuto) * (Math.exp(NESTING_COEFFICIENT * Math.log(expsumNestAuto)) / expsumTopLevel);
            probabilityAutoP = (Math.exp(utilityAutoP / NESTING_COEFFICIENT) / expsumNestAuto) * (Math.exp(NESTING_COEFFICIENT * Math.log(expsumNestAuto)) / expsumTopLevel);
            probabilityPooledTaxi = (Math.exp(utilityPooledTaxi / NESTING_COEFFICIENT) / expsumNestAuto) * (Math.exp(NESTING_COEFFICIENT * Math.log(expsumNestAuto)) / expsumTopLevel);
        } else {
            probabilityAutoD = 0.0;
            probabilityAutoP = 0.0;
            probabilityPooledTaxi = 0.;
        }

        double probabilityBus;
        double probabilityTrain;
        double probabilityTramMetro;

        if (expsumNestTransit > 0) {
            probabilityBus = (Math.exp(utilityBus / NESTING_COEFFICIENT) / expsumNestTransit) * (Math.exp(NESTING_COEFFICIENT * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTrain = (Math.exp(utilityTrain / NESTING_COEFFICIENT) / expsumNestTransit) * (Math.exp(NESTING_COEFFICIENT * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTramMetro = (Math.exp(utilityTramMetro / NESTING_COEFFICIENT) / expsumNestTransit) * (Math.exp(NESTING_COEFFICIENT * Math.log(expsumNestTransit)) / expsumTopLevel);
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
        probabilities.put(Mode.pooledTaxi, probabilityPooledTaxi);
        return probabilities;
    }

    @Override
    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        final EnumMap<Mode, Double> baseUtilities = baseCalculator.calculateUtilities(purpose, household, person, originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);

        double utilityPooledTaxi;

        if (serviceArea.contains(originZone.getGeometry())
                && serviceArea.contains(destinationZone.getGeometry())) {


            EnumMap<Mode, Double> gc = calculateGeneralizedCosts(purpose, household, person, originZone,
                    destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);

            //base utility for auto driver
            double utilityAutoD = baseUtilities.get(Mode.autoPassenger);

            double gcPooledTaxi = gc.get(Mode.pooledTaxi);
            double gcAutoD = gc.get(Mode.autoPassenger);

            //additional (or less) utility for the additive generalized cost term
            double additionalUtility = betaGeneralizedCost[purpose.ordinal()][1] * (gcPooledTaxi - gcAutoD)
                    + betaGeneralizedCost_Squared[purpose.ordinal()][1] * (gcPooledTaxi - gcAutoD);

            //add difference in utilities caused by difference in generalized costs to the base utility
            utilityPooledTaxi = utilityAutoD + additionalUtility;

        } else {
            utilityPooledTaxi = Double.NEGATIVE_INFINITY;
        }
        baseUtilities.put(Mode.pooledTaxi, utilityPooledTaxi);
        return baseUtilities;
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

        double monetaryCost = BASE_FARE + (travelDistanceAuto * DETOUR_FACTOR) * SERVICE_COST_PER_KM;
        double monetaryCostAsTime;
        if (monthlyIncome_EUR <= 1500) {
            monetaryCostAsTime = monetaryCost / VOT1500_privateAV[purpIdx];
        } else if (monthlyIncome_EUR <= 5600) {
            monetaryCostAsTime = monetaryCost / VOT5600_privateAV[purpIdx];
        } else {
            monetaryCostAsTime = monetaryCost / VOT7000_privateAV[purpIdx];
        }

        double generalizedCost = monetaryCostAsTime + timeAutoD * DETOUR_FACTOR + WAITING_TIME;
        baseGeneralizedCosts.put(Mode.pooledTaxi, generalizedCost);
        return baseGeneralizedCosts;
    }
}
