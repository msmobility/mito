package de.tum.bgu.msm.data;

public enum Day implements Id {
    monday (0),
    tuesday (1),
    wednesday (2),
    thursday(3),
    friday(4),
    saturday(5),
    sunday (6);

    private final int dayCode;

    Day(int dayCode) {
        this.dayCode = dayCode;
    }


    @Override
    public int getId() {
        return this.ordinal();
    }

    public int getDayCode() {
        return dayCode;
    }

    public static Day getDay(int code) {
        switch (code){
            case 0:
                return monday;
            case 1:
                return tuesday;
            case 2:
                return wednesday;
            case 3:
                return thursday;
            case 4:
                return friday;
            case 5:
                return saturday;
            case 6:
                return sunday;
            default:
                throw new RuntimeException("No such day code: " + code);

        }
    }

    public static Day getDay(char code) {
        switch(code){
            case 'M':
            case 'm':
                return monday;
            case 'T':
            case 't':
                return tuesday;
            case 'W':
            case 'w':
                return wednesday;
            case 'R':
            case 'r':
                return thursday;
            case 'F':
            case 'f':
                return friday;
            case 'S':
            case 's':
                return saturday;
            case 'U':
            case 'u':
                return sunday;
            default:
                throw new RuntimeException("No such day code: " + code);
        }
    }
}
