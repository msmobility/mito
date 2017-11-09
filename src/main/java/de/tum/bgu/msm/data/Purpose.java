package de.tum.bgu.msm.data;

import java.util.EnumMap;
import java.util.Map;

import static de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator.explanatoryVariable;

public enum Purpose {
    HBW,
    HBE,
    HBS,
    HBO,
    NHBW,
    NHBO;

    private final AverageBudget averageBudget = new AverageBudget(0);
    private final Map<explanatoryVariable, Double> tripAtractionByVariable = new EnumMap<>(explanatoryVariable.class);

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

    public void putTripAttractionForVariable(explanatoryVariable variable, double rate) {
        this.tripAtractionByVariable.put(variable, rate);
    }

    public double getTripAttractionForVariable(explanatoryVariable variable) {
        return this.tripAtractionByVariable.get(variable);
    }
}
