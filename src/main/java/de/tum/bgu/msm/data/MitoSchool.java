package de.tum.bgu.msm.data;


import org.locationtech.jts.geom.Coordinate;

public class MitoSchool extends MitoOccupationImpl {
    public MitoSchool(MitoZone occupationZone, Coordinate occupationLocation, int id) {
        super(occupationZone, occupationLocation, id);
    }
}
