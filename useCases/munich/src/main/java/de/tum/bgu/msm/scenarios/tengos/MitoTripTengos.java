package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.Day;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.MitoTrip7days;
import org.matsim.api.core.v01.Coord;

public class MitoTripTengos extends MitoTrip7days {

    private int coordinatedTripId = 0;
    private Coord destinationCoord;
    private Day arrivalDay;
    private int arrivalInMinutes;

    public MitoTripTengos(MitoTrip delegate) {
        super(delegate);
    }

    public int getCoordinatedTripId() {
        return coordinatedTripId;
    }

    public void setDestinationCoord(Coord destinationCoord) {
        this.destinationCoord = destinationCoord;}

    public Coord getDestinationCoord() {

        return destinationCoord;
    }

    public void setCoordinatedTripId(int coordinatedTripId) {

        this.coordinatedTripId = coordinatedTripId;
    }

    public void setArrivalDay(Day arrivalDay) {
        this.arrivalDay = arrivalDay;}

    public void setArrivalInMinutes(int arrivalInMinutes) {

        this.arrivalInMinutes = arrivalInMinutes;
    }

    public Day getArrivalDay() {
        return arrivalDay;}

    public int getArrivalInMinutes() {

        return arrivalInMinutes;
    }
}
