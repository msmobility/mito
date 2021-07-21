package de.tum.bgu.msm.data.impedances;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.Region;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;

public class TomTomImpedance implements Impedance{

    private final IndexedDoubleMatrix2D matrix;

    public TomTomImpedance(IndexedDoubleMatrix2D matrix) {
        this.matrix = matrix;
    }


    @Override
    public double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) {
        return matrix.getIndexed(origin.getZoneId(), destination.getZoneId());
    }

    @Override
    public double getTravelTimeFromRegion(Region origin, Zone destination, double timeOfDay_s, String mode) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public double getTravelTimeToRegion(Zone origin, Region destination, double timeOfDay_s, String mode) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public IndexedDoubleMatrix2D getPeakSkim(String mode) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public TravelTimes duplicate() {
        throw new RuntimeException("Not implemented");
    }
}
