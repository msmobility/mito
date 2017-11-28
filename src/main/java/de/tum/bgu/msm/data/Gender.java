package de.tum.bgu.msm.data;

public enum Gender {
    MALE,
    FEMALE;

    public static Gender valueOf(int code) {
        if(code == 2) {
            return FEMALE;
        } else if(code == 1) {
            return MALE;
        } else {
            throw new RuntimeException("Undefined gender code given!");
        }
    }

}
