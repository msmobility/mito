package de.tum.bgu.msm.data;

public enum MitoOccupationStatus {
    WORKER,
    UNEMPLOYED,
    STUDENT;

    public static MitoOccupationStatus valueOf(int occupationCode) {
        if(occupationCode == 1) {
            return WORKER;
        } else if(occupationCode == 2 || occupationCode == 0 || occupationCode == 4) {
            return UNEMPLOYED;
        } else if(occupationCode == 3) {
            return STUDENT;
        } else {
            throw new RuntimeException("Undefined occupation code given!");
        }
    }
}
