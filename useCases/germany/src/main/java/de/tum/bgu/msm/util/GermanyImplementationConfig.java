package de.tum.bgu.msm.util;

import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;
import de.tum.bgu.msm.jobTypes.GermanyJobTypeFactory;;

public class GermanyImplementationConfig implements ImplementationConfig {

    private final static GermanyImplementationConfig instance = new GermanyImplementationConfig();

    private GermanyImplementationConfig() {}

    public static GermanyImplementationConfig get() {
        return instance;
    }

    @Override
    public JobTypeFactory getJobTypeFactory() {
        return new GermanyJobTypeFactory();
    }
}
