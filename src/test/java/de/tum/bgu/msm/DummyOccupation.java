package de.tum.bgu.msm;

import de.tum.bgu.msm.data.MitoOccupation;
import de.tum.bgu.msm.data.MitoZone;
import com.vividsolutions.jts.geom.Coordinate;

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
    public int getId() {
        return 0;
    }
}
