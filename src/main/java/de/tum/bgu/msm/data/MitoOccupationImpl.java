package de.tum.bgu.msm.data;


import org.locationtech.jts.geom.Coordinate;

import java.util.Objects;
import java.util.Optional;

public abstract class MitoOccupationImpl implements MitoOccupation {

    private final MitoZone occupationZone;
    private final Coordinate occupationLocation;
    private final int id;

    private Integer startTime = null;
    private Integer endTime = null;

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
    public Optional<Integer> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    @Override
    public void setStartTime(int startTime_s) {
        this.startTime = startTime_s;
    }

    @Override
    public Optional<Integer> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    @Override
    public void setEndTime(int endTime_s) {
        this.startTime = endTime_s;
    }
}
