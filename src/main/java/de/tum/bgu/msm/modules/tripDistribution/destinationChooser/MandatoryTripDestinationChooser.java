package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;

import java.util.EnumMap;

public final class MandatoryTripDestinationChooser extends BasicDestinationChooser {

    private final Occupation occupation;

    public MandatoryTripDestinationChooser(Purpose purpose, Occupation occupation, EnumMap<Purpose, DoubleMatrix2D> baseProbabilities, DataSet dataSet) {
        super(purpose, baseProbabilities, dataSet);
        this.occupation = occupation;
    }

    @Override
    protected MitoZone findDestination(MitoTrip trip) {
        if (isFixedByOccupation(trip)) {
            return trip.getPerson().getOccupationZone();
        }
        TripDistribution.RANDOM_OCCUPATION_DESTINATION_TRIPS.incrementAndGet();
        return super.findDestination(trip);
    }

    @Override
    protected void updateBudgets(MitoHousehold household) {
        return;
    }

    @Override
    protected void updateAdjustedDestinationProbabilities(MitoHousehold household){
        return;
    }

    @Override
    void postProcessTrip(MitoTrip trip) {
        return;
    }

    @Override
    protected boolean isValid(MitoHousehold household) {
        return !household.getTripsForPurpose(purpose).isEmpty();
    }

    private boolean isFixedByOccupation(MitoTrip trip) {
        if (trip.getPerson().getOccupation() == occupation) {
            if (trip.getPerson().getOccupationZone() != null) {
                return true;
            }
        }
        return false;
    }
}
