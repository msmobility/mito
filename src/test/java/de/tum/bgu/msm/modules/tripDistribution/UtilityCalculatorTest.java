package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.AreaType;
import de.tum.bgu.msm.data.MitoZone;
import org.junit.Test;

import java.io.InputStreamReader;

public class UtilityCalculatorTest {

    @Test
    public void test() {
        DestinationUtilityJSCalculator calculator = new DestinationUtilityJSCalculator(new InputStreamReader(this.getClass().getResourceAsStream("TripDistribution")));

        System.out.println(calculator.calculateNhbwUtility(new MitoZone(1,1, AreaType.RURAL), 10));
        System.out.println(calculator.calculateNhboUtility(new MitoZone(1,1, AreaType.RURAL), 10));
    }
}
