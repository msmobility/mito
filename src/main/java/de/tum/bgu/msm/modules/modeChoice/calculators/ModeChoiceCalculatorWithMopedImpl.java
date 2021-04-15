package de.tum.bgu.msm.modules.modeChoice.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;

import java.util.EnumMap;

public class ModeChoiceCalculatorWithMopedImpl implements ModeChoiceCalculator {

    private final static double nestingCoefficient = 0.25;

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

    private final static double[][] intercepts = {
            //Auto driver, Auto passenger, bicyle, bus, train, tram or metro, walk
            //HBW
            {0.0, 0.64, 2.98, 2.95, 2.87, 3.03, Double.NEGATIVE_INFINITY},
            //HBE
            {0.0, 1.25, 2.82, 2.15, 1.73, 1.97, Double.NEGATIVE_INFINITY},
            //HBS
            {0.0, 1.27, 2.58, 1.80, 1.36, 1.76, Double.NEGATIVE_INFINITY},
            //HBO
            {0.0, 1.14, 1.38, 1.36, 1.08, 1.46, Double.NEGATIVE_INFINITY},
            //NHBW
            {0.0, 0.68, 2.02, 0.65, 1.21, 1.0, Double.NEGATIVE_INFINITY},
            //NHBO
            {0.0, 1.23, 1.08, 0.56, 0.41, 0.59, Double.NEGATIVE_INFINITY}
    };

    private final static double[][] betaAge = {
            //HBW
            {0.0, -0.0037, 0.0, -0.016, -0.017, -0.014, 0.0},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBW
            {0.0, -0.0045, 0.0, 0.0, -0.0059, 0.0, -0.011},
            //NHBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };

    private final static double[][] betaMale = {
            //HBW
            {0.0, -0.16, 0.22, -0.28, -0.25, -0.18, 0.0},
            //HBE
            {0.0, -0.17, 0.0, -0.14, -0.15, -0.15, 0.0},
            //HBS
            {0.0, -0.47, -0.14, -0.62, -0.47, -0.53, -0.15},
            //HBO
            {0.0, -0.27, 0.17, -0.13, 0.0, -0.063, -0.13},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBO
            {0.0, -0.24, 0.0, -0.20, -0.23, -0.18, -0.073}
    };

    private final static double[][] betaDriversLicense = {
            //HBW
            {0.0, -1.03, -1.86, -2.25, -2.09, -2.14, -2.16},
            //HBE
            {0.0, -1.26, -0.43, -1.23, -0.75, -0.77, -0.55},
            //HBS
            {0.0, -1.43, -1.86, -2.43, -2.46, -2.39, -2.10},
            //HBO
            {0.0, -1.34, -1.51, -1.91, -1.66, -1.74, -1.30},
            //NHBW
            {0.0, -0.94, -1.56, -1.61, -1.67, -1.37, -1.43},
            //NHBO
            {0.0, -1.40, -1.49, -2.02, -1.74, -1.77, -1.44}
    };

    private final static double[][] betaHhSize = {
            //HBW
            {0.0, 0.063, 0.25, 0.17, 0.18, 0.15, 0.0},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBO
            {0.0, 0.0, 0.0, -0.11, -0.11, -0.15, -0.190},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };

    private final static double[][] betaHhAutos = {
            //HBW
            {0.0, -0.16, -1.11, -1.27, -1.26, -1.29, -0.73},
            //HBE
            {0.0, -0.11, -0.56, -0.52, -0.56, -0.70, -0.68},
            //HBS
            {0.0, -0.03, -0.81, -1.88, -1.73, -1.88, -0.86},
            //HBO
            {0.0, -0.029, -0.57, -1.54, -1.56, -1.72, -0.300},
            //NHBW
            {0.0, -0.11, -1.12, -1.23, -1.44, -1.52, -0.47},
            //NHBO
            {0.0, -0.029, -0.73, -0.80, -0.85, -0.86, -0.40}
    };

    private final static double[][] betaDistToRailStop = {
            //HBW
            {0.0, 0.0, 0.0, -0.36, -0.39, -0.40, 0.0},
            //HBE
            {0.0, 0.0, 0.0, -0.28, -0.26, -0.46, 0.0},
            //HBS
            {0.0, 0.0, 0.0, -0.87, -0.68, -1.02, 0.0},
            //HBO
            {0.0, 0.0, 0.0, -0.61, -0.57, -0.58, -0.0650},
            //NHBW
            {0.0, 0.0, 0.0, -0.24, 0.0, -0.16, -0.37},
            //NHBO
            {0.0, 0.0, 0.0, -0.40, -0.44, -0.48, 0.0}
    };


    private final static double[][] betaCoreCitySG = {
            //HBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };

    private final static double[][] betaMediumSizedCitySG = {
            //HBHW
            {0.0, 0.0, -0.29, -0.70, -0.75, -1.05, -0.59},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };

    private final static double[][] betaTownSG = {
            //HBW
            {0.0, 0.071, -0.39, -0.86, -0.88, -1.22, -0.89},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };

    private final static double[][] betaRuralSG = {
            //HBW
            {0.0, 0.071, -0.39, -0.86, -0.88, -1.22, -0.890},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };

    private final static double[][] betaAgglomerationUrbanR = {
            //HBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };

    private final static double[][] betaRuralR = {
            //HBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBO
            {0.0, 0.0, 0.0, -0.70, -0.91, -1.12, 0.0}
    };

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


    private final static double[][] betaHhChildren = {
            //HBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {0.0, -0.051, 0.0, 0.0, 0.0, 0.0, -0.17},
            //HBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
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

    private final static double[][] betaTripLength = {
            //HBW
            {0.0, 0.0, -0.32, 0.0, 0.0, 0.0, -2.02},
            //HBE
            {0.0, 0.0, -0.42, 0.0, 0.0, 0.0, -1.71},
            //HBS
            {0.0, 0.0, -0.42, 0.0, 0.0, 0.0, -1.46},
            //HBO
            {0.0, 0.0, -0.15, 0.0, 0.0, 0.0, -0.680},
            //NHBW
            {0.0, 0.0, -0.28, 0.0, 0.0, 0.0, -1.54},
            //NHBO
            {0.0, 0.0, -0.15, 0.0, 0.0, 0.0, -0.57}
    };

    private final static double[][] betaMunichTrip = {
            //HBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBE
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBS
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //HBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBW
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            //NHBO
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };


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

        double expsumNestAuto = Math.exp(utilityAutoD / nestingCoefficient) + Math.exp(utilityAutoP / nestingCoefficient);
        double expsumNestTransit = Math.exp(utilityBus / nestingCoefficient) + Math.exp(utilityTrain / nestingCoefficient) + Math.exp(utilityTramMetro / nestingCoefficient);
        double expsumTopLevel = Math.exp(nestingCoefficient * Math.log(expsumNestAuto)) + Math.exp(utilityBicycle) + Math.exp(utilityWalk) + Math.exp(nestingCoefficient * Math.log(expsumNestTransit));

        double probabilityAutoD;
        double probabilityAutoP;
        if (expsumNestAuto > 0) {
            probabilityAutoD = (Math.exp(utilityAutoD / nestingCoefficient) / expsumNestAuto) * (Math.exp(nestingCoefficient * Math.log(expsumNestAuto)) / expsumTopLevel);
            probabilityAutoP = (Math.exp(utilityAutoP / nestingCoefficient) / expsumNestAuto) * (Math.exp(nestingCoefficient * Math.log(expsumNestAuto)) / expsumTopLevel);
        } else {
            probabilityAutoD = 0.0;
            probabilityAutoP = 0.0;
        }

        double probabilityBus;
        double probabilityTrain;
        double probabilityTramMetro;
        if (expsumNestTransit > 0) {
            probabilityBus = (Math.exp(utilityBus / nestingCoefficient) / expsumNestTransit) * (Math.exp(nestingCoefficient * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTrain = (Math.exp(utilityTrain / nestingCoefficient) / expsumNestTransit) * (Math.exp(nestingCoefficient * Math.log(expsumNestTransit)) / expsumTopLevel);
            probabilityTramMetro = (Math.exp(utilityTramMetro / nestingCoefficient) / expsumNestTransit) * (Math.exp(nestingCoefficient * Math.log(expsumNestTransit)) / expsumTopLevel);
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
        int purpIdx = purpose.ordinal();

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

        double gcAutoD = generalizedCosts.get(Mode.autoDriver);
        double gcAutoP = generalizedCosts.get(Mode.autoPassenger);
        double gcBus = generalizedCosts.get(Mode.bus);
        double gcTrain = generalizedCosts.get(Mode.train);
        double gcTramMetro = generalizedCosts.get(Mode.tramOrMetro);


        double utilityAutoD = intercepts[purpIdx][0]
                + betaAge[purpIdx][0] * age
                + betaMale[purpIdx][0] * isMale
                + betaDriversLicense[purpIdx][0] * hasLicense
                + betaHhSize[purpIdx][0] * hhSize
                + betaHhAutos[purpIdx][0] * hhAutos
                + betaDistToRailStop[purpIdx][0] * distanceToNearestRailStop
                + betaHhChildren[purpIdx][0] * hhChildren
                + betaCoreCitySG[purpIdx][0] * isCoreCity
                + betaMediumSizedCitySG[purpIdx][0] * isMediumCity
                + betaTownSG[purpIdx][0] * isTown
                + betaRuralSG[purpIdx][0] * isRural
                + betaAgglomerationUrbanR[purpIdx][0] * (isAgglomerationR + isUrbanR)
                + betaRuralR[purpIdx][0] * isRuralR
                + betaGeneralizedCost[purpIdx][0] * gcAutoD
                + betaGeneralizedCost_Squared[purpIdx][0] * (gcAutoD * gcAutoD)
                + betaMunichTrip[purpIdx][0] * isMunichTrip;


        double utilityAutoP = intercepts[purpIdx][1]
                + betaAge[purpIdx][1] * age
                + betaMale[purpIdx][1] * isMale
                + betaDriversLicense[purpIdx][1] * hasLicense
                + betaHhSize[purpIdx][1] * hhSize
                + betaHhAutos[purpIdx][1] * hhAutos
                + betaDistToRailStop[purpIdx][1] * distanceToNearestRailStop
                + betaHhChildren[purpIdx][1] * hhChildren
                + betaCoreCitySG[purpIdx][1] * isCoreCity
                + betaMediumSizedCitySG[purpIdx][1] * isMediumCity
                + betaTownSG[purpIdx][1] * isTown
                + betaRuralSG[purpIdx][1] * isRural
                + betaAgglomerationUrbanR[purpIdx][1] * (isAgglomerationR + isUrbanR)
                + betaRuralR[purpIdx][1] * isRuralR
                + betaGeneralizedCost[purpIdx][1] * gcAutoP
                + betaGeneralizedCost_Squared[purpIdx][1] * (gcAutoP * gcAutoP)
                + betaMunichTrip[purpIdx][1] * isMunichTrip;

        double utilityBicycle = intercepts[purpIdx][2]
                + betaAge[purpIdx][2] * age
                + betaMale[purpIdx][2] * isMale
                + betaDriversLicense[purpIdx][2] * hasLicense
                + betaHhSize[purpIdx][2] * hhSize
                + betaHhAutos[purpIdx][2] * hhAutos
                + betaDistToRailStop[purpIdx][2] * distanceToNearestRailStop
                + betaHhChildren[purpIdx][2] * hhChildren
                + betaCoreCitySG[purpIdx][2] * isCoreCity
                + betaMediumSizedCitySG[purpIdx][2] * isMediumCity
                + betaTownSG[purpIdx][2] * isTown
                + betaRuralSG[purpIdx][2] * isRural
                + betaAgglomerationUrbanR[purpIdx][2] * (isAgglomerationR + isUrbanR)
                + betaRuralR[purpIdx][2] * isRuralR
                + betaTripLength[purpIdx][2] * travelDistanceNMT
                + betaMunichTrip[purpIdx][2] * isMunichTrip;

        double utilityBus = intercepts[purpIdx][3]
                + betaAge[purpIdx][3] * age
                + betaMale[purpIdx][3] * isMale
                + betaDriversLicense[purpIdx][3] * hasLicense
                + betaHhSize[purpIdx][3] * hhSize
                + betaHhAutos[purpIdx][3] * hhAutos
                + betaDistToRailStop[purpIdx][3] * distanceToNearestRailStop
                + betaHhChildren[purpIdx][3] * hhChildren
                + betaCoreCitySG[purpIdx][3] * isCoreCity
                + betaMediumSizedCitySG[purpIdx][3] * isMediumCity
                + betaTownSG[purpIdx][3] * isTown
                + betaRuralSG[purpIdx][3] * isRural
                + betaAgglomerationUrbanR[purpIdx][3] * (isAgglomerationR + isUrbanR)
                + betaRuralR[purpIdx][3] * isRuralR
                + betaGeneralizedCost[purpIdx][3] * gcBus
                + betaGeneralizedCost_Squared[purpIdx][3] * (gcBus * gcBus)
                + betaMunichTrip[purpIdx][3] * isMunichTrip;

        double utilityTrain = intercepts[purpIdx][4]
                + betaAge[purpIdx][4] * age
                + betaMale[purpIdx][4] * isMale
                + betaDriversLicense[purpIdx][4] * hasLicense
                + betaHhSize[purpIdx][4] * hhSize
                + betaHhAutos[purpIdx][4] * hhAutos
                + betaDistToRailStop[purpIdx][4] * distanceToNearestRailStop
                + betaHhChildren[purpIdx][4] * hhChildren
                + betaCoreCitySG[purpIdx][4] * isCoreCity
                + betaMediumSizedCitySG[purpIdx][4] * isMediumCity
                + betaTownSG[purpIdx][4] * isTown
                + betaRuralSG[purpIdx][4] * isRural
                + betaAgglomerationUrbanR[purpIdx][4] * (isAgglomerationR + isUrbanR)
                + betaRuralR[purpIdx][4] * isRuralR
                + betaGeneralizedCost[purpIdx][4] * gcTrain
                + betaGeneralizedCost_Squared[purpIdx][4] * (gcTrain * gcTrain)
                + betaMunichTrip[purpIdx][4] * isMunichTrip;

        double utilityTramMetro = intercepts[purpIdx][5]
                + betaAge[purpIdx][5] * age
                + betaMale[purpIdx][5] * isMale
                + betaDriversLicense[purpIdx][5] * hasLicense
                + betaHhSize[purpIdx][5] * hhSize
                + betaHhAutos[purpIdx][5] * hhAutos
                + betaDistToRailStop[purpIdx][5] * distanceToNearestRailStop
                + betaHhChildren[purpIdx][5] * hhChildren
                + betaCoreCitySG[purpIdx][5] * isCoreCity
                + betaMediumSizedCitySG[purpIdx][5] * isMediumCity
                + betaTownSG[purpIdx][5] * isTown
                + betaRuralSG[purpIdx][5] * isRural
                + betaAgglomerationUrbanR[purpIdx][5] * (isAgglomerationR + isUrbanR)
                + betaRuralR[purpIdx][5] * isRuralR
                + betaGeneralizedCost[purpIdx][5] * gcTramMetro
                + betaGeneralizedCost_Squared[purpIdx][5] * (gcTramMetro * gcTramMetro)
                + betaMunichTrip[purpIdx][5] * isMunichTrip;

        double utilityWalk = intercepts[purpIdx][6]
                + betaAge[purpIdx][6] * age
                + betaMale[purpIdx][6] * isMale
                + betaDriversLicense[purpIdx][6] * hasLicense
                + betaHhSize[purpIdx][6] * hhSize
                + betaHhAutos[purpIdx][6] * hhAutos
                + betaDistToRailStop[purpIdx][6] * distanceToNearestRailStop
                + betaHhChildren[purpIdx][6] * hhChildren
                + betaCoreCitySG[purpIdx][6] * isCoreCity
                + betaMediumSizedCitySG[purpIdx][6] * isMediumCity
                + betaTownSG[purpIdx][6] * isTown
                + betaRuralSG[purpIdx][6] * isRural
                + betaAgglomerationUrbanR[purpIdx][6] * (isAgglomerationR + isUrbanR)
                + betaRuralR[purpIdx][6] * isRuralR
                + betaTripLength[purpIdx][6] * travelDistanceNMT
                + betaMunichTrip[purpIdx][6] * isMunichTrip;

        EnumMap<Mode, Double> utilities = new EnumMap<>(Mode.class);
        utilities.put(Mode.autoDriver, utilityAutoD);
        utilities.put(Mode.autoPassenger, utilityAutoP);
        utilities.put(Mode.bicycle, utilityBicycle);
        utilities.put(Mode.bus, utilityBus);
        utilities.put(Mode.train, utilityTrain);
        utilities.put(Mode.tramOrMetro, utilityTramMetro);
        utilities.put(Mode.walk, utilityWalk);
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
        int purpIdx = purpose.ordinal();

        double gcAutoD;
        double gcAutoP;
        double gcBus;
        double gcTrain;
        double gcTramMetro;

        if (monthlyIncome_EUR <= 1500) {
            gcAutoD = timeAutoD + (travelDistanceAuto * fuelCostEurosPerKm) / VOT1500_autoD[purpIdx];
            gcAutoP = timeAutoP + (travelDistanceAuto * fuelCostEurosPerKm) / VOT1500_autoP[purpIdx];
            gcBus = timeBus + (travelDistanceAuto * transitFareEurosPerKm) / VOT1500_transit[purpIdx];
            gcTrain = timeTrain + (travelDistanceAuto * transitFareEurosPerKm) / VOT1500_transit[purpIdx];
            gcTramMetro = timeTramMetro + (travelDistanceAuto * transitFareEurosPerKm) / VOT1500_transit[purpIdx];
        } else if (monthlyIncome_EUR <= 5600) {
            gcAutoD = timeAutoD + (travelDistanceAuto * fuelCostEurosPerKm) / VOT5600_autoD[purpIdx];
            gcAutoP = timeAutoP + (travelDistanceAuto * fuelCostEurosPerKm) / VOT5600_autoP[purpIdx];
            gcBus = timeBus + (travelDistanceAuto * transitFareEurosPerKm) / VOT5600_transit[purpIdx];
            gcTrain = timeTrain + (travelDistanceAuto * transitFareEurosPerKm) / VOT5600_transit[purpIdx];
            gcTramMetro = timeTramMetro + (travelDistanceAuto * transitFareEurosPerKm) / VOT5600_transit[purpIdx];
        } else {
            gcAutoD = timeAutoD + (travelDistanceAuto * fuelCostEurosPerKm) / VOT7000_autoD[purpIdx];
            gcAutoP = timeAutoP + (travelDistanceAuto * fuelCostEurosPerKm) / VOT7000_autoP[purpIdx];
            gcBus = timeBus + (travelDistanceAuto * transitFareEurosPerKm) / VOT7000_transit[purpIdx];
            gcTrain = timeTrain + (travelDistanceAuto * transitFareEurosPerKm) / VOT7000_transit[purpIdx];
            gcTramMetro = timeTramMetro + (travelDistanceAuto * transitFareEurosPerKm) / VOT7000_transit[purpIdx];
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
}
