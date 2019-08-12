package de.tum.bgu.msm.data.jobTypes.kagawa;

import de.tum.bgu.msm.data.jobTypes.JobType;
import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;

public class JobTypeFactoryTak implements JobTypeFactory {
    @Override
    public JobType getType(String id) {
        return JobTypeTak.valueOf(id.toUpperCase());
    }
}
