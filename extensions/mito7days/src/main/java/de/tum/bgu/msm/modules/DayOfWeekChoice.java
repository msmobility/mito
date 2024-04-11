package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.DayProbabilitiesReader;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.EnumMap;
import java.util.List;


public final class DayOfWeekChoice extends Module {

    private static final Logger logger = Logger.getLogger(DayOfWeekChoice.class);

    private EnumMap<Purpose, EnumMap<Day, Double>> dayProbabilitiesByPurpose;

    public DayOfWeekChoice(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet, purposes);
        dayProbabilitiesByPurpose =  new DayProbabilitiesReader(dataSet).readCoefficients();
    }

    @Override
    public void run() {
        chooseDepartureDay();
        logger.info("day of week choice completed");
    }

    private void chooseDepartureDay() {
        //old version
        dataSet.getTrips().values().forEach(trip -> {
            Day day = MitoUtil.select(dayProbabilitiesByPurpose.get(trip.getTripPurpose()), MitoUtil.getRandomObject());
            ((MitoTrip7days)trip).setDepartureDay(day);
        });

        //new version control within individual distribution
        //TODO: Corin to add a new function for choose departure day
        /*for (Purpose purpose : purposes) {
            EnumMap<Day, Double> dayProbabilitiesThisPurpose = dayProbabilitiesByPurpose.get(purpose);
            dataSet.getPersons().values().forEach(person -> person.getTrips().stream().filter(mitoTrip ->
                    purpose.equals(mitoTrip.getTripPurpose())).collect(Collectors.toList()).forEach(trip -> {
                Day day = MitoUtil.select(dayProbabilitiesThisPurpose, MitoUtil.getRandomObject());
                ((MitoTrip7days)trip).se++++++++tDepartureDay(day);
            }));
        }*/
    }
}
