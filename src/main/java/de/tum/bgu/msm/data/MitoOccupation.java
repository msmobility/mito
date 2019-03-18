package de.tum.bgu.msm.data;


import org.locationtech.jts.geom.Coordinate;

public interface MitoOccupation extends MicroLocation, Id {

    @Override
    Coordinate getCoordinate();

    @Override
    int getZoneId();

    MitoZone getOccupationZone();

}
