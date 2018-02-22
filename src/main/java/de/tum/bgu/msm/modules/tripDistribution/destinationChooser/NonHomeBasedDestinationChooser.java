package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public final class NonHomeBasedDestinationChooser extends BasicDestinationChooser {

    private final List<Purpose> priorPurposes;
    private final Occupation relatedOccupation;

    public NonHomeBasedDestinationChooser(Purpose purpose, List<Purpose> priorPurposes,
                                          Occupation relatedOccupation, EnumMap<Purpose,
            DoubleMatrix2D> baseProbabilities, DataSet dataSet) {
        super(purpose, baseProbabilities, dataSet);
        this.priorPurposes = priorPurposes;
        this.relatedOccupation = relatedOccupation;
    }

    @Override
    protected MitoZone findOrigin(MitoHousehold household, MitoTrip trip) {
        List<MitoZone> possibleBaseZones = new ArrayList<>();
        for (Purpose purpose : priorPurposes) {
            for (MitoTrip priorTrip : household.getTripsForPurpose(purpose)) {
                if (priorTrip.getPerson().equals(trip.getPerson())) {
                    possibleBaseZones.add(priorTrip.getTripDestination());
                }
            }
        }
        if (!possibleBaseZones.isEmpty()) {
            MitoZone zone = MitoUtil.select(random, possibleBaseZones);
            return zone;
        }
        if (trip.getPerson().getOccupation() == relatedOccupation && trip.getPerson().getOccupationZone() != null) {
            MitoZone zone = trip.getPerson().getOccupationZone();
            return zone;
        }

        final Purpose selectedPurpose = MitoUtil.select(random, priorPurposes);
        return findRandomOrigin(household, selectedPurpose);
    }

    @Override
    protected MitoZone findDestination(MitoTrip trip) {
        destinationProbabilities = baseProbabilities.get(purpose).viewRow(trip.getTripOrigin().getId()).copy();
//        adjustDestinationProbabilities(trip.getTripOrigin().getId());
        return super.findDestination(trip);
    }

    @Override
    protected void updateAdjustedDestinationProbabilities(MitoHousehold household) {
        return;
    }

    private MitoZone findRandomOrigin(MitoHousehold household, Purpose priorPurpose) {
        TripDistribution.COMPLETELY_RANDOM_NHB_TRIPS.incrementAndGet();
        final DoubleMatrix1D originProbabilities = baseProbabilities.get(priorPurpose).viewRow(household.getHomeZone().getId());
        final int destination = MitoUtil.select(originProbabilities.toArray(), random);
        MitoZone zone = dataSet.getZones().get(destination);
        return zone;
    }
}
