package de.tum.bgu.msm.analysis.logsumAccessibility;

        import de.tum.bgu.msm.data.DataSet;
        import de.tum.bgu.msm.data.Purpose;
        import de.tum.bgu.msm.data.Mode;
        import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
        import de.tum.bgu.msm.resources.Resources;

        import java.util.HashMap;
        import java.util.Map;

public class ModeChoiceCoefficientSingleton {

    private static Map<Purpose, ModeChoiceCoefficientSingleton> instances = new HashMap<>();
    private Map<Mode, Map<String, Double>> coefficients;

    private ModeChoiceCoefficientSingleton(DataSet dataSet, Purpose purpose) {
        coefficients = new ModeChoiceCoefficientReader(dataSet, purpose, Resources.instance.getModeChoiceCoefficients(purpose)).readCoefficientsForThisPurpose();
    }

    public static synchronized ModeChoiceCoefficientSingleton getInstance(DataSet dataSet, Purpose purpose) {
        if (!instances.containsKey(purpose)) {
            instances.put(purpose, new ModeChoiceCoefficientSingleton(dataSet, purpose));
        }
        return instances.get(purpose);
    }

    public Map<Mode, Map<String, Double>> getCoefficients() {
        return coefficients;
    }
}
