package de.tum.bgu.msm.data;


import org.locationtech.jts.geom.Coordinate;

import java.util.OptionalInt;

public interface MitoOccupation extends MicroLocation, Id {

    @Override
    Coordinate getCoordinate();

    @Override
    int getZoneId();

    MitoZone getOccupationZone();

    OptionalInt getStartTime_min();

    void setStartTime_min(int startTime_min);

    OptionalInt getEndTime_min();

    void setEndTime_min(int endTime_min);
}
