package de.tum.bgu.msm.modules.tripGeneration.airport;

import de.tum.bgu.msm.data.AreaTypes;

public interface AirportDestinationCalculator {
    double calculateUtilityOfThisZone(double popEmp, double logsum, AreaTypes.SGType areaType);
}
