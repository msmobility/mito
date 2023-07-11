package de.tum.bgu.msm.data.timeOfDay;

import java.util.*;


/**
This class defines a map of availability, with a key start time in minutes of the time window and a value boolean that defines
if the window is available (true) or not (false)
 **/

public class AvailableTimeOfDay {

    private SortedMap<Integer, Boolean> internalMap;
    private static final int INTERVAL_MIN = 5;
    private static final int MAP_SIZE = 48 * 60;


    public AvailableTimeOfDay() {
        internalMap = new TreeMap<>();
        for (int i = 0; i < MAP_SIZE; i = i + INTERVAL_MIN) {
            internalMap.put(i, true);
        }
    }

    public void blockTime(int from, int until) {
        for (int i = 0; i < MAP_SIZE; i = i + INTERVAL_MIN) {
            if (i >= from && i <= until) {
                internalMap.put(i, false);
            }
        }
    }

    public int isAvailable(int minute) {
        if (internalMap.containsKey(minute)) {
            return internalMap.get(minute) ? 1 : 0;
        } else {
            int newIndex = Math.round(minute/INTERVAL_MIN) * INTERVAL_MIN;
            return internalMap.get(newIndex) ? 1 : 0;
        }
    }

    public List<Integer> getMinutes() {
        return new ArrayList<>(internalMap.keySet());
    }


    @Override
    public String toString() {
        StringBuilder value =  new StringBuilder();
        internalMap.values().forEach(v-> value.append(v?1:0));
        return value.toString();
    }
}
