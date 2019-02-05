package de.tum.bgu.msm.util.kagawa;

import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;
import de.tum.bgu.msm.data.jobTypes.kagawa.KagawaJobTypeFactory;
import de.tum.bgu.msm.util.ImplementationConfig;

public class KagawaImplementationConfig implements ImplementationConfig {

    private final static KagawaImplementationConfig instance = new KagawaImplementationConfig();

    private KagawaImplementationConfig() {}

    public static KagawaImplementationConfig get() {
        return instance;
    }

    @Override
    public JobTypeFactory getJobTypeFactory() {
        return new KagawaJobTypeFactory();
    }
}
