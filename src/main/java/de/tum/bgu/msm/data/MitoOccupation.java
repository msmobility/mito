package de.tum.bgu.msm.data;


import org.locationtech.jts.geom.Coordinate;

import java.util.Optional;

public interface MitoOccupation extends MicroLocation, Id {

    @Override
    Coordinate getCoordinate();

    @Override
    int getZoneId();

    MitoZone getOccupationZone();

    Optional<Integer> getStartTime();

    void setStartTime(int startTime_s);

    Optional<Integer> getEndTime();

    void setEndTime(int endTime_s);
}
