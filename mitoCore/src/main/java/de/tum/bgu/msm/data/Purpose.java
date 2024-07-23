package de.tum.bgu.msm.data;

import java.util.*;
import java.util.stream.Collectors;
import de.tum.bgu.msm.modules.tripGeneration.ExplanatoryVariable;

public enum Purpose implements Id {
    HBW,
    HBE,
    HBS,
    HBO,
    NHBW,
    NHBO,
    AIRPORT,
    HBR,
    RRT,
    HBA;


    @Override
    public int getId(){
        return this.ordinal();
    }

    private final Map<ExplanatoryVariable, Double> tripAttractionByVariable = new EnumMap<>(ExplanatoryVariable.class);

    public void setTripAttractionForVariable(ExplanatoryVariable variable, double rate) {
        this.tripAttractionByVariable.put(variable, rate);
    }

    public Double getTripAttractionForVariable(ExplanatoryVariable variable) {
        return this.tripAttractionByVariable.get(variable);
    }

    public static List<Purpose> getMandatoryPurposes(){
        List<Purpose> list = new ArrayList<>();
        list.add(HBW);
        list.add(HBE);
        return list;
    }

    public static List<Purpose> getHomeBasedPurposes(){
        List<Purpose> list = new ArrayList<>();
        list.add(HBW);
        list.add(HBE);
        list.add(HBS);
        list.add(HBR);
        list.add(HBO);
        return list;
    }

    public static List<Purpose> getDiscretionaryPurposes(){
        List<Purpose> list = new ArrayList<>();
        list.add(HBS);
        list.add(HBO);
        list.add(HBR);
        list.add(NHBO);
        list.add(NHBW);
        return list;
    }

    public static List<Purpose> getOtherPurposes(){
        List<Purpose> list = new ArrayList<>();
        list.add(AIRPORT);
        return list;
    }

    public static List<Purpose> getAllPurposes(){
        List<Purpose> list = new ArrayList<>();
        list.add(HBW);
        list.add(HBE);
        list.add(HBS);
        list.add(HBO);
        list.add(HBR);
        list.add(NHBO);
        list.add(NHBW);
        return list;
    }

    public static List<Purpose> getListedPurposes(String string) {
        return Arrays.stream(string.split(",")).map(Purpose::valueOf).collect(Collectors.toList());
    }

}
