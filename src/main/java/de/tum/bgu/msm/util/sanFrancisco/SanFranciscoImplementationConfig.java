package de.tum.bgu.msm.util.sanFrancisco;

import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;
import de.tum.bgu.msm.data.jobTypes.munich.MunichJobTypeFactory;
import de.tum.bgu.msm.data.jobTypes.sanFrancisco.SanFrancsicoJobTypeFactory;
import de.tum.bgu.msm.util.ImplementationConfig;

public class SanFranciscoImplementationConfig implements ImplementationConfig {

    private final static SanFranciscoImplementationConfig instance = new SanFranciscoImplementationConfig();

    private SanFranciscoImplementationConfig() {}

    public static SanFranciscoImplementationConfig get() {
        return instance;
    }

    @Override
    public JobTypeFactory getJobTypeFactory() {
        return new SanFrancsicoJobTypeFactory();
    }
}
