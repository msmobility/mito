package de.tum.bgu.msm;

import de.tum.bgu.msm.data.MitoOccupation;
import de.tum.bgu.msm.data.MitoZone;
import org.locationtech.jts.geom.Coordinate;

import java.util.Optional;

public class DummyOccupation implements MitoOccupation {

    public final static DummyOccupation dummy = new DummyOccupation();

    private DummyOccupation(){};

    @Override
    public Coordinate getCoordinate() {
        return null;
    }

    @Override
    public int getZoneId() {
        return 0;
    }

    @Override
    public MitoZone getOccupationZone() {
        return DummyZone.dummy;
    }

    @Override
    public Optional<Integer> getStartTime() {
        return Optional.empty();
    }

    @Override
    public void setStartTime(int startTime_s) {

    }

    @Override
    public int getId() {
        return 0;
    }
}
