package de.tum.bgu.msm.data;

public enum Occupation {
    WORKER,
    UNEMPLOYED,
    STUDENT;

    public static Occupation valueOf(int occupationCode) {
        if(occupationCode == 1) {
            return WORKER;
        } else if(occupationCode == 2) {
            return UNEMPLOYED;
        } else if(occupationCode == 3) {
            return STUDENT;
        } else {
            throw new RuntimeException("Undefined occupation code given!");
        }
    }
}
