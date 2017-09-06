package de.tum.bgu.msm.util.uec;

import com.pb.common.calculator2.UtilityExpressionCalculator;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.Calculator;
import org.apache.log4j.Logger;

public abstract class UECCalculator<T> implements Calculator<T>{

    private static final Logger logger = Logger.getLogger(UECCalculator.class);

    private final UtilityExpressionCalculator calculator;
    protected final DMU<T> dmu;
    private final int[] altAvailable;

    public UECCalculator(String uecPropertiesPath, String dataSheetPropertiesPath,
                         int sheetNumber, DMU dmu) {
        String uecFileName = Resources.INSTANCE.getString(uecPropertiesPath);
        int dataSheetNumber = Resources.INSTANCE.getInt(dataSheetPropertiesPath);
        this.calculator = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, sheetNumber, dataSheetNumber, dmu);
        this.dmu = dmu;
        int numAlts= calculator.getNumberOfAlternatives();
        this.altAvailable = new int[numAlts + 1];
        for (int i = 1; i < altAvailable.length; i++) {
            altAvailable[i] = 1;
        }
    }

    @Override
    public double calculate(boolean log, T object) {
        updateDMU(object);
        double util[] = calculator.solve(dmu.getDmuIndexValues(), dmu, altAvailable);
        if (log) {
            calculator.logAnswersArray(logger, " Results using dmu: " + dmu.toString());
        }
        return util[0];
    }

    protected void updateDMU(T object) {
            dmu.updateDMU(object);
    }
}

