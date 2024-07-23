package uk.cam.mrc.phm.util;

import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;
import de.tum.bgu.msm.util.ImplementationConfig;
import uk.cam.mrc.phm.jobTypes.ManchesterJobTypeFactory;

public class ManchesterImplementationConfig implements ImplementationConfig {

    private final static ManchesterImplementationConfig instance = new ManchesterImplementationConfig();

    private ManchesterImplementationConfig() {}

    public static ManchesterImplementationConfig get() {
        return instance;
    }

    @Override
    public JobTypeFactory getJobTypeFactory() {
        return new ManchesterJobTypeFactory();
    }
}
