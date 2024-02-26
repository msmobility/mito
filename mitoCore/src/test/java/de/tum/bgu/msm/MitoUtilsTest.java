package de.tum.bgu.msm;

import de.tum.bgu.msm.util.MitoUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class MitoUtilsTest {


    @Test
    public final void testFloatScaleMap() {
        Map<Integer, Float> source = new HashMap<>();
        source.put(1, 3.4f);
        source.put(2, 2.2f);
        source.put(3, 6.7f);
        source.put(4, 10.f);
        source.put(5, 4.7f);

        MitoUtil.scaleMapTo(source, 100);

        assertEquals( 100.f, source.get(4), 0.f);

    }

    @Test
    public final void testDoubleScaleMap() {
        Map<Integer, Double> source = new HashMap<>();
        source.put(1, 3.4);
        source.put(2, 2.2);
        source.put(3, 6.7);
        source.put(4, 10.);
        source.put(5, 4.7);

        MitoUtil.scaleMapTo(source, 100);

        assertEquals( 100., source.get(4), 0.);

    }

    @Test
    public final void testSelect() {
        MitoUtil.initializeRandomNumber(new Random(42));
        Map<Integer, Double> mappedProbabilities = new HashMap<>();
        mappedProbabilities.put(1, 0.);
        mappedProbabilities.put(2, 0.);
        mappedProbabilities.put(3, 0.);
        mappedProbabilities.put(4, 1.);
        mappedProbabilities.put(5, 0.);
        mappedProbabilities.put(6, 0.);
        mappedProbabilities.put(7, 0.);
        mappedProbabilities.put(8, 0.);
        mappedProbabilities.put(9, 0.);
        mappedProbabilities.put(10, 0.);

        assertEquals(4, (int) MitoUtil.select(mappedProbabilities));
    }
}
