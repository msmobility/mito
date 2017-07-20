package de.tum.bgu.msm.modules.tripGeneration;

/**
 * Created by Nico on 20.07.2017.
 */
public class HouseholdType {

    private final int size_l;
    private final int size_h;
    private final int workers_l;
    private final int workers_h;
    private final int income_l;
    private final int income_h;
    private final int autos_l;
    private final int autos_h;
    private final int region_l;
    private final int region_h;
    private final int id;

    private int numberOfRecords = 0;

    public HouseholdType(int id, int size_l, int size_h, int workers_l, int workers_h, int income_l, int income_h, int autos_l, int autos_h, int region_l, int region_h) {
        this.id = id;
        this.size_l = size_l;
        this.size_h = size_h;
        this.workers_l = workers_l;
        this.workers_h = workers_h;
        this.income_l = income_l;
        this.income_h = income_h;
        this.autos_l = autos_l;
        this.autos_h = autos_h;
        this.region_l = region_l;
        this.region_h = region_h;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public int getId() {
        return this.id;
    }

    public boolean applies(int size, int workers, int income, int autos, int region) {
        if (appliesInSize(size) && appliesInWorkers(workers) && appliesInIncome(income) && appliesInAutos(autos) && appliesInRegion(region)) {
            numberOfRecords++;
            return true;
        } else {
            return false;
        }
    }

    private boolean appliesInRegion(int region) {
        return region >= region_l && region <= region_h;
    }

    private boolean appliesInAutos(int autos) {
        return autos >= autos_l
                && autos <= autos_h;
    }

    private boolean appliesInIncome(int income) {
        return income >= income_l && income <= income_h;
    }

    private boolean appliesInWorkers(int workers) {
        return workers >= workers_l && workers <= workers_h;
    }

    private boolean appliesInSize(int size) {
        return size >= size_l && size <= size_h;
    }

}
