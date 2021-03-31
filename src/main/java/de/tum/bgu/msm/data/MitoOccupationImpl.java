package de.tum.bgu.msm.data;


import org.locationtech.jts.geom.Coordinate;

import java.util.Objects;
import java.util.OptionalInt;

public abstract class MitoOccupationImpl implements MitoOccupation {

    private final MitoZone occupationZone;
    private final Coordinate occupationLocation;
    private final int id;

    private int startTime = Integer.MIN_VALUE;
    private int endTime = Integer.MIN_VALUE;

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

    @Override
    public OptionalInt getStartTime_min() {
        if(startTime >= 0) {
            return OptionalInt.of(endTime);
        } else {
            return OptionalInt.empty();
        }
    }

    @Override
    public void setStartTime_min(int startTime_min) {
        this.startTime = startTime_min;
    }

    @Override
    public OptionalInt getEndTime_min() {
        if(endTime >= 0) {
            return OptionalInt.of(endTime);
        } else {
            return OptionalInt.empty();
        }
    }

    @Override
    public void setEndTime_min(int endTime_min) {
        this.endTime = endTime_min;
    }
}
