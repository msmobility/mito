package de.tum.bgu.msm.data;

public class AverageBudget {

    private double budget;
    private int counts = 0;
    private double budgetSum = 0;

    public AverageBudget(double initialValue) {
        this.budget = initialValue;
    }

    public double getBudget() {
        return budget;
    }

    public void addBudgetAndUpdate(double budget) {
        this.budgetSum += budget;
        counts++;
        this.budget = budgetSum / counts;
    }

    public void reset() {
        this.budgetSum = 0;
        this.budget = 0;
        this.counts = 0;
    }
}
