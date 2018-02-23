package de.tum.bgu.msm.data;

import java.util.Arrays;
import java.util.Collection;

public enum Mode implements Id {
    autoDriver,
    autoPassenger,
    bicycle,
    bus,
    train,
    tramOrMetro,
    walk;

    @Override
    public int getId(){
        return this.ordinal();
    }

    public static Mode valueOf(int code){
        switch (code){
            case 0:
                return autoDriver;
            case 1:
                return autoPassenger;
            case 2:
                return bicycle;
            case 3:
                return bus;
            case 4:
                return train;
            case 5:
                return tramOrMetro;
            case 6:
                return walk;
            default:
                throw new RuntimeException("Mode for code " + code + "not specified.");
        }
    }

    public static Collection<Mode> valuesAsList(){
        return Arrays.asList(values());
    }
}
