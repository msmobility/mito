package de.tum.bgu.msm.data;

import com.vividsolutions.jts.geom.Coordinate;

public interface MitoOccupation extends MicroLocation, Id {

    @Override
    Coordinate getCoordinate();

    @Override
    int getZoneId();

    MitoZone getOccupationZone();

}
