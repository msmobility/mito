package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.MitoOccupationImpl;
import de.tum.bgu.msm.data.MitoZone;
import org.locationtech.jts.geom.Coordinate;

import java.util.Objects;


public abstract class MitoOccupationImplTengos extends MitoOccupationImpl {

    private MunichJobTypeTengos jobType;

    public MitoOccupationImplTengos(MitoZone occupationZone, Coordinate occupationLocation, int id) {
        super(occupationZone, occupationLocation, id);
    }


    public MunichJobTypeTengos getJobType() {
        return jobType;
    }

    public void setJobType(MunichJobTypeTengos jobType) {
        this.jobType = jobType;
    }
}
