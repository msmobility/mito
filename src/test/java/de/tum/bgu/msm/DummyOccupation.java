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
    public Optional<Integer> getStartTime_min() {
        return Optional.empty();
    }

    @Override
    public void setStartTime_min(int startTime_min) {

    }

    @Override
    public Optional<Integer> getEndTime_min() {
        return Optional.empty();
    }

    @Override
    public void setEndTime_min(int endTime_min) {

    }

    @Override
    public int getId() {
        return 0;
    }
}
