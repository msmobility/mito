package de.tum.bgu.msm.modules.Scaling;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.modules.Module;

public class TripScaling extends Module {

    public TripScaling(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        scaleTrips();
    }

    private void scaleTrips() {



    }
}
