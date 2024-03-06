package de.tum.bgu.msm.data;

import java.util.EnumSet;
import static de.tum.bgu.msm.data.Mode.*;

public enum ModeSet implements Id {

    Auto,
    AutoPt,
    AutoCycle,
    AutoWalk,
    AutoPtCycle,
    AutoPtWalk,
    AutoPtCycleWalk,
    Pt,
    PtCycle,
    PtWalk,
    PtCycleWalk,
    Cycle,
    CycleWalk,
    Walk;

    @Override
    public int getId(){
        return this.ordinal();
    }

    public EnumSet<Mode> getModes() {
        switch (this) {
            case Auto:
                return EnumSet.of(autoDriver, autoPassenger);
            case AutoPt:
                return EnumSet.of(autoDriver, autoPassenger, train, tramOrMetro, bus);
            case AutoCycle:
                return EnumSet.of(autoDriver, autoPassenger, bicycle);
            case AutoWalk:
                return EnumSet.of(autoDriver, autoPassenger, walk);
            case AutoPtCycle:
                return EnumSet.of(autoDriver, autoPassenger, train, tramOrMetro, bus, bicycle);
            case AutoPtWalk:
                return EnumSet.of(autoDriver, autoPassenger, train, tramOrMetro, bus, walk);
            case AutoPtCycleWalk:
                return EnumSet.of(autoDriver, autoPassenger, train, tramOrMetro, bus, bicycle, walk);
            case Pt:
                return EnumSet.of(train, tramOrMetro, bus);
            case PtCycle:
                return EnumSet.of(train, tramOrMetro, bus, bicycle);
            case PtWalk:
                return EnumSet.of(train, tramOrMetro, bus, walk);
            case PtCycleWalk:
                return EnumSet.of(train, tramOrMetro, bus, bicycle, walk);
            case Cycle:
                return EnumSet.of(bicycle);
            case CycleWalk:
                return EnumSet.of(bicycle, walk);
            case Walk:
                return EnumSet.of(walk);
            default:
                return null;
        }
    }
}
