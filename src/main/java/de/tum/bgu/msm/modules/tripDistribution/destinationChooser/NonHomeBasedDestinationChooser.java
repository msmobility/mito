package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import com.pb.common.matrix.Matrix;
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
            Matrix> baseProbabilities, DataSet dataSet) {
        super(purpose, baseProbabilities, dataSet);
        this.priorPurposes = priorPurposes;
        this.relatedOccupation = relatedOccupation;
    }

    @Override
    protected Zone findOrigin(MitoHousehold household, MitoTrip trip) {
        List<Zone> possibleBaseZones = new ArrayList<>();
        for (Purpose purpose : priorPurposes) {
            for (MitoTrip priorTrip : household.getTripsForPurpose(purpose)) {
                if (priorTrip.getPerson().equals(trip.getPerson())) {
                    possibleBaseZones.add(priorTrip.getTripDestination());
                }
            }
        }
        if (!possibleBaseZones.isEmpty()) {
            Zone zone = MitoUtil.select(random, possibleBaseZones);
            return zone;
        }
        if (trip.getPerson().getOccupation() == relatedOccupation && trip.getPerson().getOccupationZone() != null) {
            Zone zone = trip.getPerson().getOccupationZone();
            return zone;
        }

        final Purpose selectedPurpose = MitoUtil.select(random, priorPurposes);
        return findRandomOrigin(household, selectedPurpose);
    }

    @Override
    protected Zone findDestination(MitoTrip trip) {
        destinationProbabilities = baseProbabilities.get(purpose).getRow(trip.getTripOrigin().getZoneId()).getValues()[0];
        adjustDestinationProbabilities(trip.getTripOrigin().getZoneId());
        return super.findDestination(trip);
    }

    @Override
    protected void updateAdjustedDestinationProbabilities(MitoHousehold household) {
        return;
    }

    private Zone findRandomOrigin(MitoHousehold household, Purpose priorPurpose) {
        TripDistribution.COMPLETELY_RANDOM_NHB_TRIPS.incrementAndGet();
        final float[] originProbabilities = baseProbabilities.get(priorPurpose).getRow(household.getHomeZone().getZoneId()).copyValues1D();
        final int destination = baseProbabilities.get(purpose).getExternalNumber(MitoUtil.select(originProbabilities, random));
        Zone zone = dataSet.getZones().get(destination);
        return zone;
    }
}
