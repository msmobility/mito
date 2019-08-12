package de.tum.bgu.msm.data;


import com.vividsolutions.jts.geom.Coordinate;

public interface MicroLocation extends Location {

    Coordinate getCoordinate();
}
