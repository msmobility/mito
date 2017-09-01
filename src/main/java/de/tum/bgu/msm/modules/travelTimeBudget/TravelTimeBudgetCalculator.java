package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.util.uec.Calculator;
import org.apache.log4j.Logger;

public final class TravelTimeBudgetCalculator extends Calculator<MitoHousehold>{

    private static final Logger logger = Logger.getLogger(TravelTimeBudgetCalculator.class);
    private final String purpose;

    public TravelTimeBudgetCalculator(boolean log, String purpose, DataSet dataSet, int ttbSheetNumber) {
        super(Properties.TRAVEL_TIME_BUDGET_UEC_FILE, Properties.TRAVEL_TIME_BUDGET_UEC_DATA_SHEET, new TravelTimeBudgetDMU(), dataSet, log, ttbSheetNumber);
        this.purpose = purpose;
    }

    protected void log(MitoHousehold hh) {
        logger.info("Household " + hh.getHhId() + " with " + hh.getHhSize() + " persons living in area type " +
                hh.getHomeZone().getRegion());
        super.logAnswersArray(logger, purpose + " Travel Time Budget");
    }
}
