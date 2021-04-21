package de.tum.bgu.msm.data.timeOfDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AvailableTimeOfDay {

    private Map<Integer, Boolean> internalMap;
    private static final int INTERVAL_MIN = 5;
    private static final int MAP_SIZE = 48 * 60;


    public AvailableTimeOfDay() {
        internalMap = new HashMap<>();
        for (int i = 0; i < MAP_SIZE; i = i + INTERVAL_MIN) {
            internalMap.put(i, true);
        }
    }

    public void blockTime(int from, int until) {
        for (int i = 0; i < MAP_SIZE; i++) {
            if (i > from && i <= until) {
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


}
