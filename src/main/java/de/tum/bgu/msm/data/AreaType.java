package de.tum.bgu.msm.data;

public enum AreaType {
    URBAN,      //1
    SUBURBAN,   //2
    RURAL;      //3

    public static AreaType valueOf(int code) {
        switch (code) {
            case 1: return URBAN;
            case 2: return URBAN;
            case 3: return SUBURBAN;
            case 4: return RURAL;
            default: throw new RuntimeException("Undefined area type code " + code);
        }
    }
}
