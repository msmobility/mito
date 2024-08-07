package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;
import de.tum.bgu.msm.data.jobTypes.MunichJobTypeFactory;
import de.tum.bgu.msm.util.ImplementationConfig;

public class MunichImplementationConfigTengos implements ImplementationConfig {

    private final static MunichImplementationConfigTengos instance = new MunichImplementationConfigTengos();

    private MunichImplementationConfigTengos() {}

    public static MunichImplementationConfigTengos get() {
        return instance;
    }

    @Override
    public JobTypeFactory getJobTypeFactory() {
        return new MunichJobTypeTengosFactory();
    }
}
