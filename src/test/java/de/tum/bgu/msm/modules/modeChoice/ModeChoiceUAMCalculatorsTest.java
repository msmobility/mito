package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.DummyOccupation;
import de.tum.bgu.msm.DummyZone;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.accessTimes.AccessAndEgressVariables;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
//import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeChoiceUAMCalculatorsTest {


    private Map<String, ModeChoiceJSCalculator> calculators = new HashMap<>();
    private Map<String, Double> uamShares = new HashMap<>();
    private List<String> calculatorNames = new ArrayList<>();

    @Test
    public void test(){
        calculatorNames.add("ModeChoiceUAM");
        calculatorNames.add("ModeChoiceUAMIncremental");
        calculatorNames.add("ModeChoiceUAMIncrementalNoAVs.txt");

        for (Purpose purpose : Purpose.values()){
            if (!purpose.equals(Purpose.AIRPORT)){
                setupThisPurpose(purpose);
                testThisPurpose(purpose);
            }

        }
    }

    public void setupThisPurpose(Purpose purpose) {
        Resources.initializeResources("./testInput/test.properties");
        for (String str : calculatorNames){
            Reader reader = new InputStreamReader(this.getClass().getResourceAsStream(str));
            calculators.put(str, new ModeChoiceJSCalculator(reader, purpose));
        }
    }

    public void testThisPurpose(Purpose purpose) {
        MitoZone zone = DummyZone.dummy;
        zone.setDistanceToNearestRailStop(0.5f);
        zone.setShapeFeature(new MyFeature(false));
        zone.setAreaTypeR(AreaTypes.RType.AGGLOMERATION);
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
            }, new AccessAndEgressVariables() {
                @Override
                public double getAccessTime(Location origin, Location destination, String mode){
                    return 15;
                }
            }, 50., 50, 100., 8., 20., 4);

            System.out.println("#####Calculator" + calculatorName + "##### Purpose " + purpose.toString() + "######");



            NumberFormat formatter = new DecimalFormat("#0.00");
            for (int i = 0; i < result.length; i++) {
                System.out.println("Mode " + Mode.valueOf(i).toString() + "\t" + formatter.format(result[i]*100) + "%");
            }
        }

    }
}
