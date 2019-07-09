package de.tum.bgu.msm.data;


import com.vividsolutions.jts.geom.Coordinate;

public class MitoSchool extends MitoOccupationImpl {
    public MitoSchool(MitoZone occupationZone, Coordinate occupationLocation, int id) {
        super(occupationZone, occupationLocation, id);
    }
}
