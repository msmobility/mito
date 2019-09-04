package de.tum.bgu.msm.data;

import org.matsim.api.core.v01.TransportMode;

import java.util.Arrays;
import java.util.Collection;

public enum Mode implements Id {
    autoDriver,
    autoPassenger,
    bicycle,
    bus,
    train,
    tramOrMetro,
    walk,
    privateAV,
    sharedAV,
    uam;//Urban Air Mobility (Flying taxi)

    //for testing Mengying's model
//    auto,
//    transit,
//    autonomousTaxi,
//    flyingTaxi;

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
                return uam;
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
            case uam:
            	return "uam";
            case privateAV:
                return TransportMode.car;
            case sharedAV:
                return TransportMode.car;
            default:
                //TODO needs more attention probably
                return TransportMode.walk;

        }

    }

    //for testing Mengying's model
//    public static Mode valueOf(int code){
//        switch (code){
//            case 0:
//                return auto;
//            case 1:
//                return transit;
//            case 2:
//                return autonomousTaxi;
//            case 3:
//                return flyingTaxi;
//            default:
//                throw new RuntimeException("Mode for code " + code + "not specified.");
//        }
//    }
}
