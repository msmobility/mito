package de.tum.bgu.msm.analysis.logsumAccessibility;

        import de.tum.bgu.msm.data.DataSet;
        import de.tum.bgu.msm.data.Purpose;
        import de.tum.bgu.msm.data.Mode;
        import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
        import de.tum.bgu.msm.resources.Resources;
        import java.util.Map;

public class ModeChoiceCoefficientSingleton {
    private static ModeChoiceCoefficientSingleton instance;
    private Map<Mode, Map<String, Double>> coefficients;

    private ModeChoiceCoefficientSingleton(DataSet dataSet, Purpose purpose) {
        coefficients = new ModeChoiceCoefficientReader(dataSet, purpose, Resources.instance.getModeChoiceCoefficients(purpose)).readCoefficientsForThisPurpose();
    }

    public static synchronized ModeChoiceCoefficientSingleton getInstance(DataSet dataSet, Purpose purpose) {
        if (instance == null) {
            instance = new ModeChoiceCoefficientSingleton(dataSet, purpose);
        }
        return instance;
    }

    public Map<Mode, Map<String, Double>> getCoefficients() {
        return coefficients;
    }
}
