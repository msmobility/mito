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

public class ModeChoiceCalculatorTest {

    private ModeChoiceCalculator calculator;

    private final double[] reference = new double[]{
            0.5852608988491658,
            0.27370909307613045,
            0.08747502074524541,
            0.023052212714255723,
            0.009195343377500283,
            0.017205929125637027,
            0.004101502112065239
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

        double[] result = calculator.calculateProbabilities(Purpose.HBS, hh, pp, zone, zone, new TravelTimes() {
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
        }, 5., 5., 0);
        for(int i = 0; i < result.length; i++) {
            Assert.assertEquals("Result " + i + " is totally wrong.",reference[i], result[i], 0.000001);
        }

    }

}
