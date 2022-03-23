package de.tum.bgu.msm.modules.timeOfDayChoice;

import de.tum.bgu.msm.DummyZone;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.timeOfDay.AvailableTimeOfDay;
import de.tum.bgu.msm.data.timeOfDay.TimeOfDayDistribution;
import de.tum.bgu.msm.data.timeOfDay.TimeOfDayUtils;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.TimeOfDayDistributionsReader;
import de.tum.bgu.msm.modules.timeOfDay.TimeOfDayChoice;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.CDATASection;

import java.util.Collections;
import java.util.Random;

public class TestTimeOfDay {

    private DataSet dataSet;
    private MitoHousehold household1;
    private MitoTrip trip1;
    private MitoTrip trip2;
    private MitoTrip trip3;
    private MitoTrip trip4;

    @Before
    public void setupTest() {
        MitoUtil.initializeRandomNumber(new Random(42));
        Resources.initializeResources("./test/muc/test.properties");
        dataSet = new DataSet();
        dataSet.setTravelDistancesAuto((origin, destination) -> 5000);
        dataSet.setTravelDistancesNMT((origin, destination) -> 5000);
        dataSet.setTravelTimes(new TravelTimes() {
            @Override
            public double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) {
                return 10.;
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
        });
        fillDataSet();

        new TimeOfDayDistributionsReader(dataSet).read();
    }

    @Test
    public void test() {

        new TimeOfDayChoice(dataSet, Purpose.getDiscretionaryPurposes()).run();

        dataSet.getTrips().values().forEach(t -> {
                int id = t.getId();
                int from = t.getDepartureInMinutes();
                int until;
                if (t.getDepartureInMinutesReturnTrip() >0){
                    until = (int)(t.getDepartureInMinutesReturnTrip() +
                            dataSet.getTravelTimes().getTravelTime(t.getTripOrigin(), t.getTripDestination(), t.getDepartureInMinutes(), t.getTripMode().toString()));
                } else {
                    until = (int) (t.getDepartureInMinutes() +
                            dataSet.getTravelTimes().getTravelTime(t.getTripOrigin(), t.getTripDestination(), t.getDepartureInMinutes(), t.getTripMode().toString()));

                }

            System.out.println("Trip:" + id + " starts at " + from + " and ends at " + until);
        });

    }


    private void fillDataSet() {
        trip1 = new MitoTrip(1, Purpose.HBW);
        MitoPerson person1 = new MitoPerson(1, MitoOccupationStatus.WORKER, null, 30, MitoGender.MALE, true);
        trip1.setPerson(person1);

        MitoZone zone1 = DummyZone.dummy;
        zone1.setDistanceToNearestRailStop(0.5f);
        trip1.setTripOrigin(zone1);
        MitoZone zone2 = new MitoZone(2, AreaTypes.SGType.CORE_CITY);
        trip1.setTripDestination(zone2);
        trip1.setDepartureInMinutes(16 * 60 + 30);
        trip1.setDepartureInMinutesReturnTrip(17 * 60);
        trip1.setTripMode(Mode.autoDriver);

        household1 = new MitoHousehold(1, 24000, 1);
        household1.setHomeZone(zone1);
        household1.addPerson(person1);
        dataSet.addTrip(trip1);
        dataSet.addZone(zone1);
        dataSet.addZone(zone2);
        dataSet.addHousehold(household1);
        dataSet.addPerson(person1);

        trip2 = new MitoTrip(2, Purpose.HBE);
        trip2.setPerson(person1);
        MitoZone zone3 = new MitoZone(3, AreaTypes.SGType.CORE_CITY);
        zone3.setDistanceToNearestRailStop(0.5f);
        trip2.setTripOrigin(zone3);
        MitoZone zone4 = new MitoZone(4, AreaTypes.SGType.CORE_CITY);
        trip2.setTripDestination(zone4);
        trip2.setTripMode(Mode.autoDriver);
        trip2.setDepartureInMinutes(8* 60);
        trip2.setDepartureInMinutesReturnTrip(16 * 60);

        dataSet.addTrip(trip2);
        dataSet.addZone(zone3);
        dataSet.addZone(zone4);


        trip3 = new MitoTrip(3, Purpose.HBO);
        trip3.setPerson(person1);
        zone1.setDistanceToNearestRailStop(0.5f);
        trip3.setTripOrigin(zone1);
        trip3.setTripDestination(zone2);
        trip3.setTripMode(Mode.autoDriver);

        trip4 = new MitoTrip(4, Purpose.NHBW);
        trip4.setPerson(person1);
        zone1.setDistanceToNearestRailStop(0.5f);
        trip4.setTripOrigin(zone1);
        trip4.setTripDestination(zone2);
        trip4.setTripMode(Mode.autoDriver);

        dataSet.addTrip(trip3);
        dataSet.addTrip(trip4);
        person1.addTrip(trip1);
        person1.addTrip(trip2);
        person1.addTrip(trip3);
        person1.addTrip(trip4);
        household1.setTripsByPurpose(Collections.singletonList(trip1), trip1.getTripPurpose());
        household1.setTripsByPurpose(Collections.singletonList(trip2), trip2.getTripPurpose());
        household1.setTripsByPurpose(Collections.singletonList(trip3), trip3.getTripPurpose());
        household1.setTripsByPurpose(Collections.singletonList(trip4), trip4.getTripPurpose());

    }




}
