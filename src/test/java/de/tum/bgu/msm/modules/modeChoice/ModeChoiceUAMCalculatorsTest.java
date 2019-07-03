package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.DummyOccupation;
import de.tum.bgu.msm.DummyZone;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.accessTimes.AccessAndEgressVariables;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeChoiceUAMCalculatorsTest {


    private Map<String, ModeChoiceJSCalculator> calculators = new HashMap<>();

    @Before
    public void setup() {
        Resources.initializeResources("./testInput/test.properties");
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoiceUAM"));
        calculators.put("ModeChoiceUAM", new ModeChoiceJSCalculator(reader, Purpose.HBS));
        reader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoiceUAMIncremental"));
        calculators.put("ModeChoiceUAMIncremental", new ModeChoiceJSCalculator(reader, Purpose.HBS));
        reader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoiceUAMIncrementalNoAVs.txt"));
        calculators.put("ModeChoiceUAMIncrementalNoAVs.txt" ,new ModeChoiceJSCalculator(reader, Purpose.HBS));
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


        for (String calculatorName : calculators.keySet()) {

            double [] result = calculators.get(calculatorName).calculateProbabilitiesUAM(hh, pp, zone, zone, new TravelTimes() {
                @Override
                public double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) {
                    switch (mode) {
                        case "car":
                            return 35;
                        case "bus":
                            return 40.;
                        case "tramMetro":
                            return 1000;
                        case "train":
                            return 35.;
                        case "uam":
                            return 30;
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
            }, new AccessAndEgressVariables() {
                @Override
                public double getAccessTime(Location origin, Location destination, String mode){
                    return 15;
                }
            }, 50., 50, 100., 8., 20., 4);

            System.out.println("#####Calculator" + calculatorName + "#####");
            for (int i = 0; i < result.length; i++) {
                System.out.println("Mode " + Mode.valueOf(i) + " with probability " + result[i]);
            }
        }

    }
}
