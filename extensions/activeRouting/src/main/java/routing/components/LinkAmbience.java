package routing.components;

import org.matsim.api.core.v01.network.Link;

public class LinkAmbience {


    public static double getVgviFactor (Link link){
        return (double) link.getAttributes().getAttribute("vgvi");
    }

    public static double getDarknessFactor(Link link){
        int lights = (int) link.getAttributes().getAttribute("streetLights");
        int idealSpacing = (boolean) link.getAttributes().getAttribute("primary") ? 30 : 15;
        return 1 - Math.min(1., idealSpacing * lights / link.getLength());
    }

    public static double getShannonFactor (Link link){
        double shannon = (double) link.getAttributes().getAttribute("shannon");
        return Math.min(1., shannon / 1.6);
    }

    public static double getPoiFactor (Link link){
        int pois = (int) link.getAttributes().getAttribute("POIs");
        return Math.min(1., 5 * pois / link.getLength());
    }

    public static double getNegativePoiFactor (Link link){
        int negPois = (int) link.getAttributes().getAttribute("negPOIs");
        return Math.min(1., 5 * negPois / link.getLength());
    }

    public static double getCrimeFactor (Link link){
        int crime = (int) link.getAttributes().getAttribute("crime");
        return Math.min(1., 4 * crime / link.getLength());
    }

    public static double getDayAmbience(Link link){
        double vgvi = getVgviFactor(link);
        double pois = getPoiFactor(link);
        double shannon = getShannonFactor(link);
        double negativePois = getNegativePoiFactor(link);
        double crime = getCrimeFactor(link);

        double good = 0.5 * vgvi + Math.min(0.5,0.5 * pois + 0.5 * shannon);
        double bad = 0.5 * negativePois + 0.5 * crime;

        return Math.max(0.,Math.min(1.,0.5 - good + bad));
    }

    public static double getNightAmbience(Link link){
        double vgvi = getVgviFactor(link);
        double pois = getPoiFactor(link);
        double shannon = getShannonFactor(link);
        double negativePois = getNegativePoiFactor(link);
        double crime = getCrimeFactor(link);
        double darkness = getDarknessFactor(link);

        double good = 0.25 * vgvi + Math.min(0.5,0.5 * pois + 0.5 * shannon);
        double bad = 0.25 * darkness + 0.5 * negativePois + 0.5 * crime;

        return Math.max(0.,Math.min(1.,0.5 - good + bad));
    }

}
