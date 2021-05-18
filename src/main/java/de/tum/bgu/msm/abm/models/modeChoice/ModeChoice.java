package de.tum.bgu.msm.abm.models.modeChoice;

import de.tum.bgu.msm.data.plans.Leg;
import de.tum.bgu.msm.data.plans.LegMode;
import de.tum.bgu.msm.data.plans.Tour;

public class ModeChoice {


    LegMode chooseTourMainMode(Tour tour){
        return LegMode.UNKNOWN;
    }

    LegMode chooseMode(Leg leg, Tour tour){
        return LegMode.UNKNOWN;
    }

}
