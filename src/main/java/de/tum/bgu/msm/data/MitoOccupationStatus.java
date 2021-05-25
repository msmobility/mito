package de.tum.bgu.msm.data;

public enum MitoOccupationStatus {
    WORKER,
    UNEMPLOYED,
    STUDENT,
    RETIRED;

    public static MitoOccupationStatus valueOf(int occupationCode) {
        if(occupationCode == 1) {
            return WORKER;
        } else if(occupationCode == 2 || occupationCode == 0) {
            return UNEMPLOYED;
        } else if(occupationCode == 3) {
            return STUDENT;
        } else if (occupationCode == 4){
            return RETIRED;
        }
        else {
            throw new RuntimeException("Undefined occupation code given!");
        }
    }
}
