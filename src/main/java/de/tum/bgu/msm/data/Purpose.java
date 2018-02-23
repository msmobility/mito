package de.tum.bgu.msm.data;

import java.util.EnumMap;
import java.util.Map;

import static de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator.ExplanatoryVariable;

public enum Purpose implements Id{
    HBW,
    HBE,
    HBS,
    HBO,
    NHBW,
    NHBO;

    @Override
    public int getId(){
        return this.ordinal();
    }

    private final Map<ExplanatoryVariable, Double> tripAtractionByVariable = new EnumMap<>(ExplanatoryVariable.class);

    public void putTripAttractionForVariable(ExplanatoryVariable variable, double rate) {
        this.tripAtractionByVariable.put(variable, rate);
    }

    public Double getTripAttractionForVariable(ExplanatoryVariable variable) {
        return this.tripAtractionByVariable.get(variable);
    }
}
