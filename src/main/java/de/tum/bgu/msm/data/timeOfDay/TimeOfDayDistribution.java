package de.tum.bgu.msm.data.timeOfDay;

import de.tum.bgu.msm.util.MitoUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TimeOfDayDistribution {

    private Map<Integer, Double> internalMap;
    private static final int INTERVAL_MIN = 5;
    private static final int MINUTES_IN_DAY = 24 * 60 + 1;


    public TimeOfDayDistribution() {
        internalMap = new HashMap<>();
        for (int i = 0; i < MINUTES_IN_DAY; i = i+ INTERVAL_MIN) {
            internalMap.put(i, 0.);
        }
    }

    public void setProbability(int minute, double probability) {
        if (internalMap.containsKey(minute)) {
            internalMap.put(minute, probability);
        } else {

        }
    }


    public double probability(int minute) {
        if (internalMap.containsKey(minute)) {
            return internalMap.get(minute);
        } else {
            int newIndex = Math.round(minute/INTERVAL_MIN) * INTERVAL_MIN;
            return internalMap.get(newIndex);
        }


    }

    public int selectTime() {
        if (MitoUtil.getSum(internalMap.values()) > 0){
            return MitoUtil.select(internalMap);
        } else {
            return -1;
        }
    }

    public List<Integer> getMinutes() {
        return internalMap.keySet().stream().collect(Collectors.toList());
    }

}
