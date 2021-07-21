package de.tum.bgu.msm.modules.tripDistribution;

import java.util.Map;

public class FlexibleDestinationUtilityCalculator implements DestinationUtilityCalculator{

    private final Map<String, Double> coefficients;


    FlexibleDestinationUtilityCalculator(Map<String, Double> coefficients) {
        this.coefficients = coefficients;
    }

    public double calculateExpUtility(Map<String, Double> variables) {

        double utility = 0;
        utility += coefficients.get(ExplanatoryVariable.expDistance_km) *
                Math.exp(coefficients.get(ExplanatoryVariable.alphaDistance_km) *
                        coefficients.get(ExplanatoryVariable.calibrationFactorAlphaDistance) *
                        variables.get(ExplanatoryVariable.distance_km));

        if (variables.get(ExplanatoryVariable.logAttraction) == 0){
            return 0.;
        } else {
            utility += coefficients.get(ExplanatoryVariable.logAttraction) * Math.log(variables.get(ExplanatoryVariable.logAttraction));
        }

        utility += coefficients.get(ExplanatoryVariable.tomTomOdIntensity) * variables.get(ExplanatoryVariable.tomTomOdIntensity);

        return Math.exp(utility);
    }
}
