package de.tum.bgu.msm.modules.scenarios;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Telework extends Module {

    private final Logger logger = Logger.getLogger(Telework.class);
    private final double percentage;

    public Telework(DataSet dataSet, List<Purpose> purposes, double percentage) {
        super(dataSet, purposes);
        this.percentage = percentage;
    }



    private void removePercentageOfHomeToWorkTrips(){
        Set<Integer> tripIdsToRemove = new HashSet<>();
        for (MitoPerson person : dataSet.getPersons().values()){
            person.getTrips().stream().forEach(mitoTrip -> {
                if (mitoTrip.getTripPurpose().equals(Purpose.HBW)){
                    if (MitoUtil.getRandomObject().nextDouble() < percentage){
                        tripIdsToRemove.add(mitoTrip.getTripId());
                    }
                }
            });
        }

        for (int id : tripIdsToRemove){
            dataSet.removeTrip(id);
        }
        logger.info("Removed HBW trips because of telework: " + tripIdsToRemove.size());
    }


    @Override
    public void run() {

    }
}
