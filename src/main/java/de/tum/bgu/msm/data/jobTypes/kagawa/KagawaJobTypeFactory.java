package de.tum.bgu.msm.data.jobTypes.kagawa;

import de.tum.bgu.msm.data.jobTypes.JobType;
import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;

public class KagawaJobTypeFactory implements JobTypeFactory {
    @Override
    public JobType getType(String id) {
        return KagawaJobType.valueOf(id.toUpperCase());
    }
}
