package de.tum.bgu.msm.data.plans;

import de.tum.bgu.msm.data.MicroLocation;
import org.locationtech.jts.geom.Coordinate;

public class Activity implements Comparable<Activity>, MicroLocation {

    private Purpose purpose;
    private double startTime_s;
    private double endTime_s;
    private Coordinate coordinate;


    public Activity(Purpose purpose, double startTime_s, double endTime_s, Coordinate coordinate) {
        this.purpose = purpose;
        this.startTime_s = startTime_s;
        this.endTime_s = endTime_s;
        this.coordinate = coordinate;
    }

    public Purpose getPurpose() {
        return purpose;
    }

    public double getStartTime_s() {
        return startTime_s;
    }

    public double getEndTime_s() {
        return endTime_s;
    }

    public void setStartTime_s(double startTime_s) {
        this.startTime_s = startTime_s;
    }

    public void setEndTime_s(double endTime_s) {
        this.endTime_s = endTime_s;
    }


    @Override
    public int compareTo(Activity activity) {
        if (activity.getStartTime_s() < this.getStartTime_s()){
            return 1;
        } else if (activity.getStartTime_s() == this.getStartTime_s()) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "Activity{" +
                "purpose=" + purpose +
                ", startTime_s=" + startTime_s +
                ", endTime_s=" + endTime_s +
                ", coordinate=" + coordinate +
                '}';
    }

    @Override
    public int getZoneId() {
        return 0;
    }

    @Override
    public Coordinate getCoordinate() {
        return this.coordinate;
    }
}
