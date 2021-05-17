package de.tum.bgu.msm.data.plans;

public class Leg {

    Activity previousActivity;
    Activity nextActivity;
    LegMode legMode;

    public Leg(Activity previousActivity, Activity nextActivity) {
        this.previousActivity = previousActivity;
        this.nextActivity = nextActivity;
        this.legMode = LegMode.UNKNOWN;
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

    public LegMode getMode() {
        return legMode;
    }

    public void setMode(LegMode legMode) {
        this.legMode = legMode;
    }
}
