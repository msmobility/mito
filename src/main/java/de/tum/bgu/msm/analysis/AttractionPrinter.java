package de.tum.bgu.msm.analysis;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

public class AttractionPrinter {

    public void printOutAttractions(DataSet dataSet, String outputFile) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(outputFile);
        pw.print("zone,hh,employment");
        for (Purpose purpose : Purpose.getAllPurposes()){
            pw.print(",");
            pw.print("attraction_" + purpose);
        }
        pw.println();
        final Map<Integer, MitoZone> zones = dataSet.getZones();
        zones.keySet().forEach(zoneId -> {
            pw.print(zoneId);
            pw.print(",");
            final MitoZone mitoZone = zones.get(zoneId);
            pw.print(mitoZone.getNumberOfHouseholds());
            pw.print(",");
            pw.print(mitoZone.getTotalEmpl());
            for (Purpose purpose : Purpose.getAllPurposes()){
                pw.print(",");
                pw.print(mitoZone.getTripAttraction(purpose));
            }
            pw.println();
        });
        pw.close();

    }

}
