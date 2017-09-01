package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.TravelTimes;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.util.uec.DMU;
import javafx.util.Pair;

public class TripDistributionDMU extends DMU<Pair<Zone,Zone>> {

    private final TravelTimes travelTimes;

    //uec variables
    int totalEmployees;
    int retailEmployees;
    int otherEmployees;
    int schoolEnrollment;

    double travelTime;

    public TripDistributionDMU(TravelTimes travelTimes) {
        this.travelTimes = travelTimes;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }

    public double getTravelTime() {
        return travelTime;
    }

    @Override
    protected void setup(Pair<Zone, Zone> zonePair) {
        Zone fixed = zonePair.getKey();
        Zone inQuestion = zonePair.getValue();
        this.totalEmployees = inQuestion.getTotalEmpl();
        this.travelTime = travelTimes.getTravelTimeFromTo(fixed, inQuestion);
    }
}
