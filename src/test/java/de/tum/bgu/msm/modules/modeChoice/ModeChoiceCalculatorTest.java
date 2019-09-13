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

    private final double[] reference = new double[]{0.35258741,0.21025060,0.07781168,0.01532404,0.00755559,0.01247863,0.00364841,0.35258208,0.015602413198211748};

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
        //origin.setAreaTypeHBWModeChoice(AreaType.HBW_mediumSizedCity);
        MitoHousehold hh = new MitoHousehold(1, 20000, 1);
        MitoPerson pp = new MitoPerson(1, MitoOccupationStatus.STUDENT, DummyOccupation.dummy, 20, MitoGender.FEMALE, true);
        hh.addPerson(pp);
        MitoTrip trip = new MitoTrip(1, Purpose.HBS);
        trip.setTripOrigin(zone);
        trip.setTripDestination(zone);

        double[] result = calculator.calculateProbabilities(hh, pp, zone, zone, new TravelTimes() {
        	@Override
        	public double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) {
        		switch(mode) {
        		case "car": return 15.;
        		case "bus": return 30.;
        		case "tramMetro": return 25;
        		case "train": return 40.;
        		default: return 0;
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
