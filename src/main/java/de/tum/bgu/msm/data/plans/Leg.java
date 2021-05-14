package de.tum.bgu.msm.data.plans;

public class Leg {

    Activity previousActivity;
    Activity nextActivity;
    Mode mode;

    public Leg(Activity previousActivity, Activity nextActivity) {
        this.previousActivity = previousActivity;
        this.nextActivity = nextActivity;
        this.mode = Mode.UNKNOWN;
    }

    public Activity getPreviousActivity() {
        return previousActivity;
    }

    public void setPreviousActivity(Activity previousActivity) {
        this.previousActivity = previousActivity;
    }

    public Activity getNextActivity() {
        return nextActivity;
    }

    public void setNextActivity(Activity nextActivity) {
        this.nextActivity = nextActivity;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }
}
