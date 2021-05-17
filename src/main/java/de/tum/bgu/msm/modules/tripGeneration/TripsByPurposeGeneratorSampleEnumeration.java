package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.*;

import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.DROPPED_TRIPS_AT_BORDER_COUNTER;
import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.TRIP_ID_COUNTER;

class TripsByPurposeGeneratorSampleEnumeration extends RandomizableConcurrentFunction<Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>>> implements TripsByPurposeGenerator {

    private static final Logger logger = Logger.getLogger(TripsByPurposeGeneratorSampleEnumeration.class);
    private final boolean dropAtBorder = Resources.instance.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER);

    private Map<MitoHousehold, List<MitoTrip>> tripsByHH = new HashMap<>();

    private final DataSet dataSet;
    private final Purpose activityPurpose;

    private final HouseholdTypeManager householdTypeManager;
    private double scaleFactorForGeneration;


    TripsByPurposeGeneratorSampleEnumeration(DataSet dataSet, Purpose activityPurpose, double scaleFactorForGeneration) {
        super(MitoUtil.getRandomObject().nextLong());
        this.dataSet = dataSet;
        this.activityPurpose = activityPurpose;
        householdTypeManager = new HouseholdTypeManager(activityPurpose);
        this.scaleFactorForGeneration = scaleFactorForGeneration;
    }

    @Override
    public Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>> call() {
        logger.info("  Generating trips with activityPurpose " + activityPurpose + " (multi-threaded)");
        logger.info("Created trip frequency distributions for " + activityPurpose);
        logger.info("Started assignment of trips for hh, activityPurpose: " + activityPurpose);
        final Iterator<MitoHousehold> iterator = dataSet.getHouseholds().values().iterator();
        for (; iterator.hasNext(); ) {
            MitoHousehold next = iterator.next();
            if (MitoUtil.getRandomObject().nextDouble() < scaleFactorForGeneration) {
                generateTripsForHousehold(next, scaleFactorForGeneration);
            }
        }
        return new Tuple<>(activityPurpose, tripsByHH);
    }


    private void generateTripsForHousehold(MitoHousehold hh, double scaleFactorForGeneration) {
        HouseholdType hhType = householdTypeManager.determineHouseholdType(hh);
        if (hhType == null) {
            logger.error("Could not create trips for Household " + hh.getId() + " for Purpose " + activityPurpose + ": No Household Type applicable");
            return;
        }
        Integer[] tripFrequencies = householdTypeManager.getTripFrequenciesForHouseholdType(hhType);
        if (tripFrequencies == null) {
            logger.error("Could not find trip frequencies for this hhType/Purpose: " + hhType.getId() + "/" + activityPurpose);
            return;
        }
        if (MitoUtil.getSum(tripFrequencies) == 0) {
            //logger.info("No trips for this hhType/Purpose: " + hhType.getId() + "/" + activityPurpose);
            return;
        }

        List<MitoTrip> trips = new ArrayList<>();
        int numberOfTrips = selectNumberOfTrips(tripFrequencies);
        for (int i = 0; i < numberOfTrips; i++) {
            MitoTrip trip = createTrip(hh);
            if (trip != null) {
                trips.add(trip);
            }
        }
        tripsByHH.put(hh, trips);
    }

    private int selectNumberOfTrips(Integer[] tripFrequencies) {
        double[] probabilities = new double[tripFrequencies.length];
        for (int i = 0; i < tripFrequencies.length; i++) {
            probabilities[i] = (double) tripFrequencies[i];
        }
        return MitoUtil.select(probabilities, random);
    }

    private MitoTrip createTrip(MitoHousehold hh) {
        boolean dropThisTrip = reduceTripGenAtStudyAreaBorder(hh.getHomeZone());
        if (dropThisTrip) {
            DROPPED_TRIPS_AT_BORDER_COUNTER.incrementAndGet();
            return null;
        }
        return new MitoTrip(TRIP_ID_COUNTER.incrementAndGet(), activityPurpose);
    }

    private boolean reduceTripGenAtStudyAreaBorder(MitoZone tripOrigin) {
        if (dropAtBorder) {
            float damper = dataSet.getZones().get(tripOrigin.getId()).getReductionAtBorderDamper();
            return random.nextFloat() < damper;
        }
        return false;
    }
}
