package de.tum.bgu.msm.util.uec;

import com.pb.common.calculator2.UtilityExpressionCalculator;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

public abstract class Calculator<T> {

    private final UtilityExpressionCalculator calculator;
    private final DMU dmu;
    private final boolean log;

    public Calculator(String uecPropertiesPath, String dataSheetPropertiesPath, DMU dmu, DataSet dataSet, boolean log, int sheetNumber) {
        this.log = log;
        String uecFileName = Resources.INSTANCE.getString(uecPropertiesPath);
        int dataSheetNumber = Resources.INSTANCE.getInt(dataSheetPropertiesPath);
        this.calculator = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, sheetNumber, dataSheetNumber, dmu);
        this.dmu = dmu;
    }

    protected abstract void log(T object);

    public double calculate(T object, int[] totalAvail) {
        dmu.setup(object);
        double util[] = calculator.solve(dmu.getDmuIndexValues(), dmu, totalAvail);
        if (log) {
            log(object);
        }
        return util[0];
    }

    public int getNumberOfAlternatives() {
        return calculator.getNumberOfAlternatives();
    }

    public void logAnswersArray(Logger logger, String prefix) {
        this.calculator.logAnswersArray(logger, prefix);
    };
}

