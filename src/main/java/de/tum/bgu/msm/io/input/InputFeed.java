package de.tum.bgu.msm.io.input;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Zone;

import java.util.Map;

public class InputFeed {

    public final Map<Integer, Zone> zones;
    public final Matrix autoTravelTimes;
    public final Matrix transitTravelTimes;
    public final Map<Integer, MitoHousehold> households;

    public InputFeed(Map<Integer, Zone> zones, Matrix autoTravelTimes, Matrix transitTravelTimes, Map<Integer, MitoHousehold> households) {
        this.zones = zones;
        this.autoTravelTimes = autoTravelTimes;
        this.transitTravelTimes = transitTravelTimes;
        this.households = households;
    }
}
