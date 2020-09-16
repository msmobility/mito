package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.DummyOccupation;
import de.tum.bgu.msm.DummyZone;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculatorImpl;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumMap;

public class ModeChoiceCalculatorTest {

    private ModeChoiceCalculator calculator;
    private ModeChoiceCalculator drtCalculator;

    private final double[] reference = new double[]{
            0.5851514772392093,
            0.27380766911828386,
            0.08751674107474669,
            0.02303730219100069,
            0.009176089627710512,
            0.017207262466889552,
            0.004103458282159372
    };

    @Before
    public void setup() {
        Resources.initializeResources("./test/muc/test.properties");
        calculator = new ModeChoiceCalculatorImpl();
    }

    @Test
    public void test() {
        MitoZone zone = DummyZone.dummy;
        zone.setDistanceToNearestRailStop(0.5f);
        //origin.setAreaTypeHBWModeChoice(AreaType.HBW_mediumSizedCity);
        MitoHousehold hh = new MitoHousehold(1, 20000, 1);
        MitoPerson pp = new MitoPerson(1, MitoOccupationStatus.STUDENT, DummyOccupation.dummy, 20, MitoGender.FEMALE, true);
        hh.addPerson(pp);
        MitoTrip trip = new MitoTrip(1, Purpose.HBS);
        trip.setTripOrigin(zone);
        trip.setTripDestination(zone);

        final TravelTimes travelTimes = new TravelTimes() {
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
            public double getTravelTimeFromRegion(Region origin, Zone destination, double timeOfDay_s, String mode) {
                return 0;
            }

            @Override
            public double getTravelTimeToRegion(Zone origin, Region destination, double timeOfDay_s, String mode) {
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
        };

        EnumMap<Mode, Double> result = calculator.calculateProbabilities(Purpose.HBS, hh, pp, zone, zone, travelTimes, 5., 5., 0);

        for(int i = 0; i < reference.length; i++) {
            Assert.assertEquals("Result " + i + " is totally wrong.",reference[i], result.get(Mode.valueOf(i)), 0.000001);
        }

    }

}
