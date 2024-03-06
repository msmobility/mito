package de.tum.bgu.msm.modules.tripDistribution.tripDistributors;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.locationtech.jts.geom.Coordinate;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public class AirportDistributor extends AbstractDistributor {

    private final MitoZone airportZone;

    private MicroLocation airport;

    private boolean toAirport;

    public AirportDistributor(Purpose purpose, Collection<MitoHousehold> householdCollection, DataSet dataSet,
                              EnumMap<Purpose, List<TripDistribution.tripDistributionData>> distributionData) {
        super(purpose, householdCollection, dataSet, distributionData, null);
        if(!purpose.equals(Purpose.AIRPORT)) {
            throw new RuntimeException("Airport distribution only works with airport purpose!");
        }
        this.airportZone = dataSet.getZones().get(Resources.instance.getInt(Properties.AIRPORT_ZONE));
        this.airport = getAirport();
        postProcessTrip(null);
    }

    private MicroLocation getAirport() {
        return new MicroLocation() {
            @Override
            public Coordinate getCoordinate() {
                return new Coordinate(Resources.instance.getInt(Properties.AIRPORT_X),
                        Resources.instance.getInt(Properties.AIRPORT_Y));
            }

            @Override
            public int getZoneId() {
                return airportZone.getZoneId();
            }
        };
    }

    @Override
    protected Location findOrigin(MitoHousehold household, MitoTrip trip) {
        return toAirport ? household : airport;
    }

    @Override
    protected Location findDestination(MitoTrip trip, int categoryIndex) {
        return toAirport ? airport : trip.getPerson().getHousehold();
    }

    // use this method to select the direction of the next trip
    @Override
    protected void postProcessTrip(MitoTrip trip) {
        toAirport = super.random.nextDouble() < 0.5;
    }
}
