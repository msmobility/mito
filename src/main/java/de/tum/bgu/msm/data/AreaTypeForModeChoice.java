package de.tum.bgu.msm.data;

public enum AreaTypeForModeChoice {
    HBW_coreCity,
    HBW_mediumSizedCity,
    HBW_townOrRural,
    NHBO_agglomeration,
    NHBO_urban,
    NHBO_rural;

    public static AreaTypeForModeChoice valueOf(int code){
        switch (code){
            case 10:
                return HBW_coreCity;
            case 20:
                return HBW_mediumSizedCity;
            case 30:
                return HBW_townOrRural;
            case 40:
                return HBW_townOrRural;
            case 1:
                return NHBO_agglomeration;
            case 2:
                return NHBO_urban;
            case 3:
                return NHBO_rural;
            default:
                throw new RuntimeException("Area Type for code " + code + "not specified.");
        }
    }
}
