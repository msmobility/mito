package de.tum.bgu.msm.data;


import org.locationtech.jts.geom.Coordinate;

import java.util.Objects;

public abstract class MitoOccupationImpl implements MitoOccupation {

    private final MitoZone occupationZone;
    private final Coordinate occupationLocation;
    private final int id;

    public MitoOccupationImpl(MitoZone occupationZone, Coordinate occupationLocation, int id) {
        this.occupationZone = Objects.requireNonNull(occupationZone);
        this.occupationLocation = Objects.requireNonNull(occupationLocation);
        this.id = id;
    }

    @Override
    public Coordinate getCoordinate() {
        return occupationLocation;
    }

    @Override
    public int getZoneId() {
        return occupationZone.getId();
    }

    @Override
    public MitoZone getOccupationZone() {
        return occupationZone;
    }

    @Override
    public int getId() {
        return id;
    }
}
