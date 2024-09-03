package routing.components;
import org.matsim.api.core.v01.network.Link;

public enum Crossing {
    UNCONTROLLED,
    ZEBRA,
    SIGNAL_MIXED,
    SIGNAL_ACTIVE;

    public static Crossing getType(Link link, String mode) {
        String name = (String) link.getToNode().getAttributes().getAttribute(mode + "Crossing");
        switch (name) {
            case "null":
                return UNCONTROLLED;
            case "Parallel crossing point":
            case "crossing point":
                return ZEBRA;
            case "Car signal":
                return SIGNAL_MIXED;
            default:
                return SIGNAL_ACTIVE;
        }
    }
}