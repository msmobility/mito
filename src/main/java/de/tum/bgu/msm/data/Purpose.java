package de.tum.bgu.msm.data;

import java.util.EnumMap;
import java.util.Map;

import static de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator.ExplanatoryVariable;

public enum Purpose {
    HBW,
    HBE,
    HBS,
    HBO,
    NHBW,
    NHBO;

    private final AverageBudget averageBudget = new AverageBudget(0);
    private final Map<ExplanatoryVariable, Double> tripAtractionByVariable = new EnumMap<>(ExplanatoryVariable.class);

    public void addAndUpdateBudget(double budget) {
       averageBudget.addBudgetAndUpdate(budget);
    }

    public double getAverageBudgetPerHousehold() {
        return this.averageBudget.getBudget();
    }

    public static void clearBudgets() {
        for(Purpose purpose: Purpose.values()) {
            purpose.averageBudget.reset();
        }
    }

    public void putTripAttractionForVariable(ExplanatoryVariable variable, double rate) {
        this.tripAtractionByVariable.put(variable, rate);
    }

    public double getTripAttractionForVariable(ExplanatoryVariable variable) {
        return this.tripAtractionByVariable.get(variable);
    }
}
