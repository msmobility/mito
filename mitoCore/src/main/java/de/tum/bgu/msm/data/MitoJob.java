package de.tum.bgu.msm.data;


import org.locationtech.jts.geom.Coordinate;

public class MitoJob extends MitoOccupationImpl {
    public MitoJob(MitoZone occupationZone, Coordinate occupationLocation, int id) {
        super(occupationZone, occupationLocation, id);
    }
}
