package de.tum.bgu.msm.util;

import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;
import de.tum.bgu.msm.data.jobTypes.kagawa.JobTypeFactoryTak;

public class KagawaImplementationConfig implements ImplementationConfig {

    private final static KagawaImplementationConfig instance = new KagawaImplementationConfig();

    private KagawaImplementationConfig() {}

    public static KagawaImplementationConfig get() {
        return instance;
    }

    @Override
    public JobTypeFactory getJobTypeFactory() {
        return new JobTypeFactoryTak();
    }
}
