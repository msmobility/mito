package de.tum.bgu.msm.data.jobTypes;

import de.tum.bgu.msm.data.jobTypes.JobType;
import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;

public class MunichJobTypeFactory implements JobTypeFactory {
    @Override
    public JobType getType(String id) {
        return MunichJobType.valueOf(id.toUpperCase());
    }
}
