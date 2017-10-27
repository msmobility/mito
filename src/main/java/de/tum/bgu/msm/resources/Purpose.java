package de.tum.bgu.msm.resources;

import de.tum.bgu.msm.data.AverageBudget;

public enum Purpose {
    HBW,
    HBE,
    HBS,
    HBO,
    NHBW,
    NHBO;

    private final AverageBudget averageBudget = new AverageBudget(0);

    public void addAndUpdateBudget(double budget) {
       averageBudget.addBudgetAndUpdate(budget);
    }

    public double getAverageBudgetPerHousehold() {
        return this.averageBudget.getBudget();
    }
}
