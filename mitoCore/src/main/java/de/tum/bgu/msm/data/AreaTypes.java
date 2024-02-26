package de.tum.bgu.msm.data;

public class AreaTypes {

    /**
     * This is the Rtyp area type classification from MID '08.
     * It classifies in {@code AGGLOMERATION - 1, URBAN - 2, RURAL - 3}
     */
    public enum RType {
        AGGLOMERATION(1),
        URBAN(2),
        RURAL(3);

        private final int code;

        RType(int code) {
            this.code = code;
        }

        public static RType valueOf(int code) {
            switch (code) {
                case 1:
                    return AGGLOMERATION;
                case 2:
                    return URBAN;
                case 3:
                    return RURAL;
                default:
                    throw new RuntimeException("Area Type for code " + code + "not specified in Rtyp classification.");
            }
        }

        public int code() {
            return code;
        }
    }

    /**
     * This is the SGtyp area type classification from MID '08.
     * It classifies in {@code CORE_CITY - 10, MEDIUM_SIZED_CITY - 20, TOWN - 30, RURAL - 40}
     */
    public enum SGType {
        CORE_CITY(10),
        MEDIUM_SIZED_CITY(20),
        TOWN(30),
        RURAL(40);

        private final int code;

        SGType(int code) {
            this.code = code;
        }

        public static SGType valueOf(int code) {
            switch (code) {
                case 10:
                    return CORE_CITY;
                case 20:
                    return MEDIUM_SIZED_CITY;
                case 30:
                    return TOWN;
                case 40:
                    return RURAL;
                default:
                    throw new RuntimeException("Area Type for code " + code + " not specified in SGtyp classification.");
            }
        }

        public int code() {
            return code;
        }
    }
}
