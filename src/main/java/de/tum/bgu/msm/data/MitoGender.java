package de.tum.bgu.msm.data;

import java.util.Random;

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

    //// added by Alona, to assign drivers license
    public static Random rand;  // was private
    public static double getRandomNumberAsDouble() {
        return rand.nextDouble();
    }

    public static boolean obtainLicense(MitoGender gender, int age){
        boolean license = true; // true - to assign license to everyone
        int row = 1;
        int threshold = 0;
        if (age > 17) {
            if (age < 29) {
                if (gender == MitoGender.MALE) {
                    threshold = 86;
                } else {
                    threshold = 87;
                }
            } else if (age < 39) {
                if (gender == MitoGender.MALE) {
                    threshold = 95;
                } else {
                    threshold = 94;
                }
            } else if (age < 49) {
                if (gender == MitoGender.MALE) {
                    threshold = 97;
                } else {
                    threshold = 95;
                }
            } else if (age < 59) {
                if (gender == MitoGender.MALE) {
                    threshold = 96;
                } else {
                    threshold = 89;
                }
            } else if (age < 64) {
                if (gender == MitoGender.MALE) {
                    threshold = 95;
                } else {
                    threshold = 86;
                }
            } else if (age < 74) {
                if (gender == MitoGender.MALE) {
                    threshold = 95;
                } else {
                    threshold = 71;
                }
            } else {
                if (gender == MitoGender.MALE) {
                    threshold = 88;
                } else {
                    threshold = 44;
                }
            }
            //if (getRandomNumberAsDouble() * 100 < threshold) {
               //license = true;
            //}
        }
        return license;
    }

    //

}
