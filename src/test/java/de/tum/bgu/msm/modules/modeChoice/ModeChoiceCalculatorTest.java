package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Resources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;

public class ModeChoiceCalculatorTest {

    private ModeChoiceJSCalculator calculator;

    private final double[] reference = new double[]{0.35258741,0.21025060,0.07781168,0.01532404,0.00755559,0.01247863,0.00364841,0.35258208,0.01560092};

    @Before
    public void setup() {
        Resources.initializeResources("./testInput/test.properties");
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoiceAV"));
        calculator = new ModeChoiceJSCalculator(reader);
    }

    @Test
    public void test() {
        MitoZone zone = new MitoZone(1, 100, null);
        zone.setDistanceToNearestRailStop(0.5f);
        //origin.setAreaTypeHBWModeChoice(AreaTypeForModeChoice.HBW_mediumSizedCity);
        MitoHousehold hh = new MitoHousehold(1, 20000, 1, null);
        MitoPerson pp = new MitoPerson(1, Occupation.STUDENT, 1, 20, Gender.FEMALE, true);
        hh.addPerson(pp);
        MitoTrip trip = new MitoTrip(1, Purpose.HBS);
        trip.setTripOrigin(zone);
        trip.setTripDestination(zone);

        //for(int i= 0; i< 1000000; i ++) {
            double[] result = calculator.calculateProbabilities(hh, pp, trip, (origin1, destination, timeOfDay_s, mode) -> {
                switch(mode) {
                    case "car": return 15.;
                    case "bus": return 30.;
                    case "tramMetro": return 25;
                    case "train": return 40.;
                    default: return 0;
                }
            }, 5., 5., 0);
        //}
        for(int i = 0; i < result.length; i++) {
            Assert.assertEquals("Result " + i + " is totally wrong.",reference[i], result[i], 0.000001);
        }

    }

}
