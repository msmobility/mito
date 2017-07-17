package de.tum.bgu.msm.io;

/**
 * Created by Nico on 17.07.2017.
 */
public interface CSVAdapter {
    void processHeader(String[] header);
    void processRecord(String[] record);
}
