package de.tum.bgu.msm;

import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.junit.Test;

public class MitoIT {

    @Test
    public void test() {
        //TODO: Use more realistic test input for skims (i.e. reduce skims to only selected 70 zones and differ by mode)
        MitoModel model = MitoModel.standAloneModel("./test/muc/test.properties", MunichImplementationConfig.get());
        model.runModel();
    }
}
