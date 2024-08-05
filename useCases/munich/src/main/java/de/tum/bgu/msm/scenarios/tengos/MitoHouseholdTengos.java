package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.MitoHousehold;
import org.apache.log4j.Logger;

public class MitoHouseholdTengos extends MitoHousehold {
    private static final Logger logger = Logger.getLogger(MitoHouseholdTengos.class);

    private boolean nursingHome = false;

    public MitoHouseholdTengos(int id, int monthlyIncome_EUR, int autos, boolean modelled) {
        super(id, monthlyIncome_EUR, autos, modelled);
    }

    public boolean isNursingHome() {
        return nursingHome;
    }

    public void setNursingHome(boolean nursingHome) {
        this.nursingHome = nursingHome;
    }
}
