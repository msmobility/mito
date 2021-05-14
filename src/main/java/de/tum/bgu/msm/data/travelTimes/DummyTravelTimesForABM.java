package de.tum.bgu.msm.data.travelTimes;

import de.tum.bgu.msm.data.plans.Activity;
import de.tum.bgu.msm.data.plans.Mode;
import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.MicroLocation;
import de.tum.bgu.msm.data.Region;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;


public class DummyTravelTimesForABM implements TravelTimes {

    private double getTravelTimeInSeconds(Activity origin, Activity destination, Mode mode, double time){
        double distance_m = Math.abs(origin.getCoordinate().getX() - destination.getCoordinate().getX()) +
                Math.abs(origin.getCoordinate().getY() - destination.getCoordinate().getY());

        double speed_ms = 15. / 3.6;

        return distance_m / speed_ms;
    }

    @Override
    public double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) {
        if (origin instanceof MicroLocation && destination instanceof MicroLocation){

            double distance_m = Math.abs(((MicroLocation) origin).getCoordinate().getX() - ((MicroLocation) destination).getCoordinate().getX()) +
                    Math.abs(((MicroLocation) origin).getCoordinate().getY() - ((MicroLocation) destination).getCoordinate().getY());

            double speed_ms = 15. / 3.6;
            return distance_m / speed_ms / 60;
        } else {
            throw new RuntimeException("ABM model only works with microlocations");
        }
    }

    @Override
    public double getTravelTimeFromRegion(Region origin, Zone destination, double timeOfDay_s, String mode) {
        return 0;
    }

    @Override
    public double getTravelTimeToRegion(Zone origin, Region destination, double timeOfDay_s, String mode) {
        return 0;
    }

    @Override
    public IndexedDoubleMatrix2D getPeakSkim(String mode) {
        return null;
    }

    @Override
    public TravelTimes duplicate() {
        return this;
    }
}
