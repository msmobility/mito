package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.jobTypes.JobType;
import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;

public class MunichJobTypeTengosFactory implements JobTypeFactory {
    @Override
    public JobType getType(String id) {
        return MunichJobTypeTengos.valueOf(id.toUpperCase());
    }
}
