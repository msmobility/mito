package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.DummyOccupation;
import de.tum.bgu.msm.DummyZone;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;

public class ModeChoiceCalculatorTest {

    private ModeChoiceJSCalculator calculator;

    private final double[] reference = new double[]{0.4104862335019519,
            0.04737661812592914,
            0.0745563964450754,
            0.012142093739147075,
            0.0048338138977189445,
            0.01353338321691506,
            0.014462452935472239,
            0.41047388033750554,
            0.012135127800284829};

    @Before
    public void setup() {
        Resources.initializeResources("./testInput/test.properties");
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoiceAV"));
        calculator = new ModeChoiceJSCalculator(reader, Purpose.HBS);
    }

    @Test
    public void test() {
        MitoZone zone = DummyZone.dummy;
        zone.setDistanceToNearestRailStop(0.5f);
        zone.setShapeFeature(new MyFeature(false));
        //origin.setAreaTypeHBWModeChoice(AreaType.HBW_mediumSizedCity);
        MitoHousehold hh = new MitoHousehold(1, 2000, 1, null);
        MitoPerson pp = new MitoPerson(1, MitoOccupationStatus.STUDENT, DummyOccupation.dummy, 20, MitoGender.FEMALE, true);
        hh.addPerson(pp);
        MitoTrip trip = new MitoTrip(1, Purpose.HBS);
        trip.setTripOrigin(zone);
        trip.setTripDestination(zone);

        double[] result = calculator.calculateProbabilities(hh, pp, zone, zone, new TravelTimes() {
            @Override
            public double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) {
                switch (mode) {
                    case "car":
                        return 15.;
                    case "bus":
                        return 30.;
                    case "tramMetro":
                        return 25;
                    case "train":
                        return 40.;
                    default:
                        return 0;
                }
            }

            @Override
            public double getTravelTimeToRegion(Location origin, Region destination, double timeOfDay_s, String mode) {
                return 0;
            }

            @Override
            public IndexedDoubleMatrix2D getPeakSkim(String mode) {
                return null;
            }

            @Override
            public TravelTimes duplicate() {
                return null;
            }
        }, 5., 5., 0);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals("Result " + i + " is totally wrong.",reference[i], result[i], 0.000001);
        }

    }

}
