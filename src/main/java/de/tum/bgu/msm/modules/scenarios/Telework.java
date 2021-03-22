package de.tum.bgu.msm.modules.scenarios;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Telework extends Module {

    private final double percentage;
    private final Logger logger = Logger.getLogger(Telework.class);

    public Telework(DataSet dataSet, List<Purpose> purposes, double percentage) {
        super(dataSet, purposes);
        this.percentage = percentage;
    }

    @Override
    public void run() {
        removeTripsDueToTelework();
    }

    private void removeTripsDueToTelework() {
        int count = 0;
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {
            Set<MitoTrip> tripCandiatesToRemove = new HashSet<>();
            for (MitoTrip mitoTrip : hh.getTripsForPurpose(Purpose.HBW)) {
                if (MitoUtil.getRandomObject().nextDouble() < percentage) {
                    tripCandiatesToRemove.add(mitoTrip);
                }
            }
            for (MitoTrip mitoTrip :  tripCandiatesToRemove) {
                dataSet.removeTrip(mitoTrip.getTripId());
                hh.getTripsForPurpose(Purpose.HBW).remove(mitoTrip);
                MitoPerson person = mitoTrip.getPerson();
                person.removeTripFromPerson(mitoTrip);
                count++;

            }


        }
        logger.info("Numebr of trips removed due to telework: " + count);
    }
}
