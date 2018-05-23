package de.tum.bgu.msm.modules.tripGeneration;

/**
 * Created by Nico on 20.07.2017.
 */
public class HouseholdType {

    private final int sizeL;
    private final int sizeH;
    private final int workersL;
    private final int workersH;
    private final int economicStatusL;
    private final int economicStatusH;
    private final int autosL;
    private final int autosH;
    private final int regionL;
    private final int regionH;
    private final int id;

    private int numberOfRecords = 0;

    public HouseholdType(int id, int sizeL, int sizeH, int workersL, int workersH, int economicStatusL, int economicStatusH, int autosL, int autosH, int regionL, int regionH) {
        this.id = id;
        this.sizeL = sizeL;
        this.sizeH = sizeH;
        this.workersL = workersL;
        this.workersH = workersH;
        this.economicStatusL = economicStatusL;
        this.economicStatusH = economicStatusH;
        this.autosL = autosL;
        this.autosH = autosH;
        this.regionL = regionL;
        this.regionH = regionH;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public int getId() {
        return this.id;
    }

    public boolean applies(int size, int workers, int economicStatus, int autos, int region) {
        if (appliesInSize(size) && appliesInWorkers(workers) && appliesInEconomicStatus(economicStatus) && appliesInAutos(autos) && appliesInRegion(region)) {
            numberOfRecords++;
            return true;
        } else {
            return false;
        }
    }

    private boolean appliesInRegion(int region) {
        return region >= regionL && region <= regionH;
    }

    private boolean appliesInAutos(int autos) {
        return autos >= autosL
                && autos <= autosH;
    }

    private boolean appliesInEconomicStatus(int economicStatus) {
        return economicStatus >= economicStatusL && economicStatus <= economicStatusH;
    }

    private boolean appliesInWorkers(int workers) {
        return workers >= workersL && workers <= workersH;
    }

    private boolean appliesInSize(int size) {
        return size >= sizeL && size <= sizeH;
    }

    public boolean hasTheseAttributes(int hhSizeL, int hhSizeH, int wrkL, int wrkH, int ecoStatL, int ecoStatH, int autoL, int autoH, int regL, int regH) {
        return(hhSizeL==sizeL && hhSizeH==sizeH && wrkL==workersL && wrkH == workersH && ecoStatL == economicStatusL &&
        ecoStatH == economicStatusH && autoL==autosL && autoH==autosH && regL==regionL && regH==regionH);
    }
}
