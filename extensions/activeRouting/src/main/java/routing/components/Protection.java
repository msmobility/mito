package routing.components;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;

public enum Protection {
    KERBED,
    PROTECTED,
    LANE,
    MIXED;

    public static Protection getType(Link link) {
        String cycleosm = (String) link.getAttributes().getAttribute("cycleosm");
        String cycleway = (String) link.getAttributes().getAttribute("cycleway");

        if (link.getAllowedModes().contains(TransportMode.walk) || link.getAllowedModes().contains(TransportMode.bike)) {
            switch (cycleosm) {
                case "offroad":
                case "kerbed":
                    return KERBED;
                case "protected":
                    return PROTECTED;
                case "painted":
                    return LANE;
                case "integrated":
                    return MIXED;
                default:
                    switch (cycleway) {
                        case "track":
                            return PROTECTED;
                        case "share_busway":
                        case "lane":
                            return LANE;
                        default:
                            return MIXED;
                    }
            }
        } else {
            return null;
        }
    }

}
