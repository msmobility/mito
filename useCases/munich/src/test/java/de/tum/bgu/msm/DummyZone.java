package de.tum.bgu.msm;

import de.tum.bgu.msm.data.AreaTypes;
import de.tum.bgu.msm.data.MitoZone;

public class DummyZone extends MitoZone {

    public static final DummyZone dummy = new DummyZone();

    private DummyZone() {
        super(1, AreaTypes.SGType.CORE_CITY);
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public int getZoneId() {
        return 0;
    }
}
