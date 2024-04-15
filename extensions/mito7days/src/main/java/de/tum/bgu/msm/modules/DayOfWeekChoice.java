package de.tum.bgu.msm.modules;

import com.google.common.collect.Lists;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.DaysOfWeekReader;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.*;


public final class DayOfWeekChoice extends Module {

    private static final Logger logger = Logger.getLogger(DayOfWeekChoice.class);

    private final EnumMap<Purpose, ArrayList<LinkedHashMap<String,Integer>>> probabilitiesByPurpose;

    public DayOfWeekChoice(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet, purposes);
        probabilitiesByPurpose =  new DaysOfWeekReader(dataSet).readCoefficients();
    }

    @Override
    public void run() {
        chooseDepartureDay();
        logger.info("day of week choice completed");
    }

    private void chooseDepartureDay() {
        for (Purpose purpose : Purpose.values()) {
            for (MitoPerson person : dataSet.getPersons().values()) {
                List<MitoTrip> trips = person.getTripsForPurpose(purpose);
                if (trips.isEmpty()) continue;
                assignDays(purpose, trips);
            }
        }
    }

    private void assignDays(Purpose purpose, List<MitoTrip> trips) {

        LinkedHashMap<String, Integer> candidates;

        // Select candidates. If number of trips is too large, split into smaller partitions and try again
        try {
            candidates = probabilitiesByPurpose.get(purpose).get(trips.size());
        } catch (IndexOutOfBoundsException e) {
            for (List<MitoTrip> partition : Lists.partition(trips, 7)) {
                assignDays(purpose, partition);
            }
            return;
        }

        // Select day sequence
        double[] frequencies = candidates.values().stream().mapToDouble(Integer::doubleValue).toArray();
        int index = MitoUtil.select(frequencies, MitoUtil.getRandomObject());
        Iterator<String> it = candidates.keySet().iterator();
        for (int i = 0; i < index; i++) it.next();
        String sequence = it.next();

        // Assign sequence to trips
        int i = 0;
        for (MitoTrip trip : trips) {
            ((MitoTrip7days) trip).setDepartureDay(Day.getDay(sequence.charAt(i)));
            i++;
        }
    }
}
