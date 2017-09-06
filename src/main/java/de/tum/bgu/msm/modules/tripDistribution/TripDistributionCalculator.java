package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.TravelTimes;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.util.uec.UECCalculator;

public final class TripDistributionCalculator extends UECCalculator<Zone> {

    public TripDistributionCalculator(int sheetNumber, TravelTimes travelTimes) {
        super(Properties.TRIP_DISTRIBUTION_UEC_FILE, Properties.TRIP_DISTRIBUTION_UEC_DATA_SHEET, sheetNumber,  new TripDistributionDMU(travelTimes));
    }

    public void setOriginZone(Zone zone) {
        ((TripDistributionDMU)dmu).setOriginZone(zone);
    }

    public void setBudget(double budget) {
        ((TripDistributionDMU) dmu).setBudget(budget);
    }


}
