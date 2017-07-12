package de.tum.bgu.msm;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Nico on 12.07.2017.
 */
public class MitoUtilsTest {


    @Test
    public final void testScaleMap() {
        Map<Integer, Float> source = new HashMap<>();
        source.put(1, 3.4f);
        source.put(2, 2.2f);
        source.put(3, 6.7f);
        source.put(4, 10.f);
        source.put(5, 4.7f);

        MitoUtil.scaleMap(source, 100);

        assertEquals((double) 100.f, (double) source.get(4), 0.);

    }
}
