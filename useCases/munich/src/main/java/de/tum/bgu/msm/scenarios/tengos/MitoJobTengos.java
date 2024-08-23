package de.tum.bgu.msm.scenarios.tengos;


import de.tum.bgu.msm.data.MitoJob;
import de.tum.bgu.msm.data.MitoOccupationImpl;
import de.tum.bgu.msm.data.MitoZone;
import org.locationtech.jts.geom.Coordinate;

public class MitoJobTengos extends MitoJob {

    private MunichJobTypeTengos jobType;

    public MitoJobTengos(MitoZone occupationZone, Coordinate occupationLocation, int id) {
        super(occupationZone, occupationLocation, id);
    }

    public MunichJobTypeTengos getJobType() {
        return jobType;
    }

    public void setJobType(MunichJobTypeTengos jobType) {
        this.jobType = jobType;
    }
}
