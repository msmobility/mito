package de.tum.bgu.msm.data;

public enum MitoGender {
    MALE,
    FEMALE;

    public static MitoGender valueOf(int code) {
        if(code == 2) {
            return FEMALE;
        } else if(code == 1) {
            return MALE;
        } else {
            throw new RuntimeException("Undefined gender code given!");
        }
    }

}
