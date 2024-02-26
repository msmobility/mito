package de.tum.bgu.msm.data;


import org.locationtech.jts.geom.Coordinate;

public interface MicroLocation extends Location {

    Coordinate getCoordinate();
}
