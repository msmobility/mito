package de.tum.bgu.msm.data.jobTypes.sanFrancisco;

import de.tum.bgu.msm.data.jobTypes.JobType;
import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;
import de.tum.bgu.msm.data.jobTypes.munich.MunichJobType;

public class SanFrancsicoJobTypeFactory implements JobTypeFactory {
    @Override
    public JobType getType(String id) {
        return SanFranciscoJobType.valueOf(id.toUpperCase());
    }
}
