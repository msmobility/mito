package de.tum.bgu.msm.data;

import org.locationtech.jts.geom.Coordinate;

public class UnknownLocation implements MicroLocation{

    public static MicroLocation get(){
        return new UnknownLocation();
    }


    @Override
    public int getZoneId() {
        return 0;
    }

    @Override
    public Coordinate getCoordinate() {
        return null;
    }
}
