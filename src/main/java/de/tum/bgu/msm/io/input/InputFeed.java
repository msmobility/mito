package de.tum.bgu.msm.io.input;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;

/**
 * Created by Nico on 19.07.2017.
 */
public class InputFeed {

    public final int[] zones;
    public final Matrix autoTravelTimes;
    public final Matrix transitTravelTimes;
    public final MitoHousehold[] households;
    public final MitoPerson[] persons;
    public final int[] retailEmplByZone;
    public final int[] officeEmplByZone;
    public final int[] otherEmplByZone;
    public final int[] totalEmplByZone;
    public final float[] sizeOfZonesInAcre;


    public InputFeed(int[] zones, Matrix autoTravelTimes, Matrix transitTravelTimes, MitoHousehold[] households, MitoPerson[] persons, int[] retailEmplByZone, int[] officeEmplByZone, int[] otherEmplByZone, int[] totalEmplByZone, float[] sizeOfZonesInAcre) {
        this.zones = zones;
        this.autoTravelTimes = autoTravelTimes;
        this.transitTravelTimes = transitTravelTimes;
        this.households = households;
        this.persons = persons;
        this.retailEmplByZone = retailEmplByZone;
        this.officeEmplByZone = officeEmplByZone;
        this.otherEmplByZone = otherEmplByZone;
        this.totalEmplByZone = totalEmplByZone;
        this.sizeOfZonesInAcre = sizeOfZonesInAcre;
    }
}
