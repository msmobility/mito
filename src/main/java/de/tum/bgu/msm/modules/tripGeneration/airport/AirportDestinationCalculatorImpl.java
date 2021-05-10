package de.tum.bgu.msm.modules.tripGeneration.airport;

import de.tum.bgu.msm.data.AreaTypes;

public class AirportDestinationCalculatorImpl implements AirportDestinationCalculator {

    private static final double B_LOG_POP_EMP = 0.198613;
    private static final double B_LOGSUM = 2.829126;
    private static final double B_IS_CORE = 1.464005;
    private static final double B_IS_MEDIUM = 0;
    private static final double B_IS_TOWN = 0;

    @Override
    public double calculateUtilityOfThisZone(double popEmp, double logsum, AreaTypes.SGType areaType){


        int is_core = 0;
        int is_medium = 0;
        int is_town = 0;

        switch (areaType) {
            case CORE_CITY:
                is_core = 1;
                break;
            case MEDIUM_SIZED_CITY:
                is_medium = 1;
                break;
            case TOWN:
                is_town = 1;
                break;
            default:
        }

        return B_LOG_POP_EMP * Math.log(popEmp) + B_LOGSUM * logsum +
                B_IS_CORE * is_core + B_IS_MEDIUM * is_medium + B_IS_TOWN * is_town;
    }
}
