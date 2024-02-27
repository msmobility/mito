package de.tum.bgu.msm.jobTypes;

import de.tum.bgu.msm.data.jobTypes.JobType;
import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;

public class GermanyJobTypeFactory implements JobTypeFactory {
    @Override
    public JobType getType(String id) {
        return GermanyJobType.valueOf(id.toUpperCase());
    }
}
