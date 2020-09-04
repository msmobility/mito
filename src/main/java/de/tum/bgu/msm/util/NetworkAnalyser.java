package de.tum.bgu.msm.util;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class NetworkAnalyser {
    private static Network network = NetworkUtils.createNetwork();
    private static ArrayList<Link> questionableLinks = new ArrayList<>();

    private static final String CSV_SEPARATOR = ", ";
    private static final String QUESTIONABLE_LINKS_FILE_NAME = "questionableLinks.csv";

    private static final Logger logger = Logger.getLogger(NetworkAnalyser.class);

    public NetworkAnalyser() {
    }

    private static void readNetworkFile(String path) {
        new MatsimNetworkReader(network).readFile(path);
    }

    private static void checkLength(double threshold) {
        // Check length of each lane, if less than zero or more than threshold the link needs to be checked 
        for (Link link : network.getLinks().values()) {
            if (link.getLength() < 0 || link.getLength() > threshold) {
                if (!questionableLinks.contains(link))
                    questionableLinks.add(link);
            }
        }
    }

    private static void checkLanes(int threshold) {
        // For each link in the network
        for (Link link : network.getLinks().values()) {
            // For each link coming out of the second node of the current link
            for (Link nextLink : link.getToNode().getOutLinks().values()) {
                if (Math.abs(nextLink.getNumberOfLanes() - link.getNumberOfLanes()) > threshold) {
                    for (Link nextToNextLink : nextLink.getToNode().getOutLinks().values()) {
                        if (Math.abs(nextLink.getNumberOfLanes() - nextToNextLink.getNumberOfLanes()) > threshold) {
                            if (!questionableLinks.contains(nextLink))
                                questionableLinks.add(nextLink);
                        }
                    }
                }
            }
        }
    }

    public static void writeToCsv(String path) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
            bw.write("Id, From, To, Length, Capacity, FreeSpeed, Modes, Lanes, Flow Capacity");
            bw.newLine();
            for (Link link : questionableLinks) {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(link.getId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(link.getFromNode().getId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(link.getToNode().getId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(link.getLength());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(link.getCapacity());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(link.getFreespeed());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(link.getAllowedModes());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(link.getCapacity());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(link.getFlowCapacityPerSec());

                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            logger.error("Encoding/File not found exception");
        } catch (IOException e) {
            logger.error("I/O Error at the .csv file");
        }
    }

    public static void main(String[] args) {
        logger.info("Reading network");
        // First argument contains the network file
        readNetworkFile(args[0]);
        logger.info("Checking links' length");
        checkLength(10000);
        logger.info("Checking number of lanes");
        checkLanes(4);
        logger.info("Writing results to .csv");
        // Second argument contains the output folder
        writeToCsv(args[1] + QUESTIONABLE_LINKS_FILE_NAME);
    }
}
