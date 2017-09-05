package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.util.uec.Calculator;

public final class TravelTimeBudgetCalculator extends Calculator<MitoHousehold> {

    public TravelTimeBudgetCalculator(int ttbSheetNumber) {
        super(Properties.TRAVEL_TIME_BUDGET_UEC_FILE, Properties.TRAVEL_TIME_BUDGET_UEC_DATA_SHEET, ttbSheetNumber, new TravelTimeBudgetDMU());
    }
}
