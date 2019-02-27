package de.tum.bgu.msm.data;

import com.vividsolutions.jts.geom.Coordinate;

public class MitoJob extends MitoOccupationImpl {
    public MitoJob(MitoZone occupationZone, Coordinate occupationLocation, int id) {
        super(occupationZone, occupationLocation, id);
    }
}
