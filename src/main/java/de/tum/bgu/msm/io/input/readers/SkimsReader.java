package de.tum.bgu.msm.io.input.readers;

public interface SkimsReader {

    void readSkimDistancesAuto();

    void readSkimDistancesNMT();

    void readOnlyTransitTravelTimes();

    void read();

}
