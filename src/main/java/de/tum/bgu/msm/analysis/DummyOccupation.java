package de.tum.bgu.msm.analysis;

import de.tum.bgu.msm.data.MitoOccupation;
import de.tum.bgu.msm.data.MitoZone;
import org.locationtech.jts.geom.Coordinate;

import java.util.OptionalInt;

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
    public OptionalInt getStartTime_min() {
        return OptionalInt.empty();
    }

    @Override
    public void setStartTime_min(int startTime_min) {

    }

    @Override
    public OptionalInt getEndTime_min() {
        return OptionalInt.empty();
    }

    @Override
    public void setEndTime_min(int endTime_min) {

    }

    @Override
    public int getId() {
        return 0;
    }
}
