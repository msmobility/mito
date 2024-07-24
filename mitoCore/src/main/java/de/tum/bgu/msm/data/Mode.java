package de.tum.bgu.msm.data;

import org.matsim.api.core.v01.TransportMode;

import java.util.Arrays;
import java.util.Collection;

public enum Mode implements Id {
    autoDriver,
    autoPassenger,
    bicycle,
    pt,
    bus,
    train,
    tramOrMetro,
    walk,
    privateAV,
    sharedAV,
    pooledTaxi,
    taxi;

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
            case 7:
                return privateAV;
            case 8:
                return sharedAV;
            case 9:
                return pooledTaxi;
            case 10:
                return taxi;
            default:
                throw new RuntimeException("Mode for code " + code + "not specified.");
        }
    }

    public static String getMatsimMode(Mode mitoMode){
        switch (mitoMode) {
            case autoDriver:
                return TransportMode.car;
            case autoPassenger:
                return "car_passenger";
            case pt:
                return TransportMode.pt;
            case bus:
                return TransportMode.pt;
            case tramOrMetro:
                return TransportMode.pt;
            case train:
                return TransportMode.pt;
            case walk:
                return TransportMode.walk;
            case bicycle:
                return TransportMode.bike;
            case pooledTaxi:
                return TransportMode.drt;
            case taxi:
                return TransportMode.taxi;
            default:
                return null;
        }
    }
}
