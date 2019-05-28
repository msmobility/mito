package de.tum.bgu.msm.data;

import java.util.EnumMap;
import java.util.Map;

import static de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator.ExplanatoryVariable;

public enum Purpose implements Id {
    HBW,
    HBE,
    HBS,
    HBO,
    NHBW,
    NHBO,
    AIRPORT;

    @Override
    public int getId(){
        return this.ordinal();
    }

    private final Map<ExplanatoryVariable, Double> tripAttractionByVariable = new EnumMap<>(ExplanatoryVariable.class);

    public void setTripAttractionForVariable(ExplanatoryVariable variable, double rate) {
        this.tripAttractionByVariable.put(variable, rate);
    }

    public Double getTripAttractionForVariable(ExplanatoryVariable variable) {
        return this.tripAttractionByVariable.get(variable);
    }
}
