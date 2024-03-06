package de.tum.bgu.msm.modules.tripDistribution.tripDistributors;

import de.tum.bgu.msm.data.Purpose;

public enum TripDistributorType {
    HomeBasedMandatory,
    HomeBasedDiscretionary,
    HomeBasedDiscretionaryWithTTB,
    Airport,
    NonHomeBasedDiscretionary,
    NonHomeBasedDiscretionaryWithTTB,
    RecreationalRoundTrip;


    public static TripDistributorType getDefault(Purpose purpose) {
        switch(purpose) {
            case HBW:
            case HBE:
                return HomeBasedMandatory;
            case HBS:
            case HBO:
            case HBR:
                return HomeBasedDiscretionary;
            case NHBW:
            case NHBO:
                return NonHomeBasedDiscretionary;
            case AIRPORT:
                return Airport;
            case RRT:
                return RecreationalRoundTrip;
            default:
                throw new RuntimeException("No default for purpose " + purpose);
        }
    }
}
