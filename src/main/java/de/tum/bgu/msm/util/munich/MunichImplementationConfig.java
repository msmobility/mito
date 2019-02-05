package de.tum.bgu.msm.util.munich;

import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;
import de.tum.bgu.msm.data.jobTypes.munich.MunichJobTypeFactory;
import de.tum.bgu.msm.util.ImplementationConfig;

public class MunichImplementationConfig implements ImplementationConfig {

    private final static MunichImplementationConfig instance = new MunichImplementationConfig();

    private MunichImplementationConfig() {}

    public static MunichImplementationConfig get() {
        return instance;
    }

    @Override
    public JobTypeFactory getJobTypeFactory() {
        return new MunichJobTypeFactory();
    }
}
