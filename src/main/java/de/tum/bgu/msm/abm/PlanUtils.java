package de.tum.bgu.msm.abm;

import de.tum.bgu.msm.data.plans.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;

public class PlanUtils {
    /**
     * Adds a main activity tour. Cuts the home activity into two pieces, one before the tour and another after the tour
     * Adds trip
     *
     * @param mainTourActivity
     */
    public static void addMainTour(Plan plan, Activity mainTourActivity, TravelTimes travelTimes) {
        //find the home activity
        Activity homeActivity = null;

        for (Activity candidateActivity : plan.getHomeActivities().values()) {
            if (mainTourActivity.getStartTime_s() > candidateActivity.getStartTime_s() && mainTourActivity.getEndTime_s() < candidateActivity.getEndTime_s()) {
                homeActivity = candidateActivity;
                break;
            }
        }
        //add a tour - adds the new activity there
        Tour tour = new Tour(mainTourActivity);
        plan.getTours().put(mainTourActivity.getStartTime_s(), tour);
        if (homeActivity != null) {
            double timeToMainActivity = 60 *  travelTimes.getTravelTime(homeActivity, mainTourActivity, mainTourActivity.getStartTime_s(), Mode.UNKNOWN.toString());
            double previousEndOfHomeActivity = homeActivity.getEndTime_s();
            homeActivity.setEndTime_s(mainTourActivity.getStartTime_s() - timeToMainActivity);
            tour.getTrips().put(homeActivity, new Leg(homeActivity, mainTourActivity));
            Activity secondHomeActivity = new Activity(Purpose.H, mainTourActivity.getEndTime_s() + timeToMainActivity, previousEndOfHomeActivity, homeActivity.getCoordinate());
            plan.getHomeActivities().put(secondHomeActivity.getStartTime_s(), secondHomeActivity);
            tour.getTrips().put(mainTourActivity, new Leg(mainTourActivity, secondHomeActivity));
        }
    }

    /**
     * Adds a main activity subtour. Cuts the main activity into two pieces, one before the tour and another after the tour
     * Adds trips
     *
     * @param subTourActivity
     */
    public static void addSubtour(Plan plan, Activity subTourActivity, TravelTimes travelTimes) {
        Activity mainActivity = null;
        Tour tour = null;
        //the search in the following may be not necessary or need to be adapted later
        for (Tour candidateTour : plan.getTours().values()) {
            Activity candidateActivity = candidateTour.getMainActivity();
            if (subTourActivity.getStartTime_s() > candidateActivity.getStartTime_s() && subTourActivity.getEndTime_s() < candidateActivity.getEndTime_s()) {
                mainActivity = candidateActivity;
                tour = candidateTour;
                break;
            }
        }
        //add subtour
        Tour subtour = new Tour(subTourActivity);
        tour.getSubtours().put(subTourActivity.getStartTime_s(), subtour);
        //add the new activity and break the main activity of the tour

        //todo here
        if (mainActivity != null) {
            double timeToSubTourActivity = 60 * travelTimes.getTravelTime(mainActivity, subTourActivity,subTourActivity.getStartTime_s(), Mode.UNKNOWN.toString() );
            double previousEndOfMainActivity = mainActivity.getEndTime_s();
            mainActivity.setEndTime_s(subTourActivity.getStartTime_s() - timeToSubTourActivity);
            Leg previousLegFromMainActivity = tour.getTrips().get(mainActivity);
            subtour.getTrips().put(mainActivity, new Leg(mainActivity, subTourActivity));
            Activity secondMainActivity = new Activity(mainActivity.getPurpose(), subTourActivity.getEndTime_s() + timeToSubTourActivity, previousEndOfMainActivity, mainActivity.getCoordinate());
            tour.getActivities().put(secondMainActivity.getStartTime_s(), secondMainActivity);
            subtour.getTrips().put(subTourActivity, new Leg(subTourActivity, secondMainActivity));
            tour.getTrips().remove(mainActivity);
            tour.getTrips().put(secondMainActivity, new Leg(secondMainActivity, previousLegFromMainActivity.getNextActivity()));
        } else {
            //trying to add a subtour without having a tour!
        }

    }

    /**
     * Adds one stop before the next main activity, modifying the home activity accordingly
     * Changes the outbound trip and splits it into two subtrips
     */
    public static void addStopBefore(Plan plan, Activity stopBefore, Activity mainActivity, TravelTimes travelTimes) {

        Activity candidatePreviousActivity = null;
        for (Tour candidateTour : plan.getTours().values()){
            for (Leg candidateLeg : candidateTour.getTrips().values()){
                if (candidateLeg.getNextActivity().equals(mainActivity)){
                    candidatePreviousActivity = candidateLeg.getPreviousActivity();
                }
            }
        }


        Tour tour = null;
        Leg legToRemove = null;
        for (Tour candidateTour : plan.getTours().values()) {
            for (Leg candidateLeg : candidateTour.getTrips().values()){
                if (candidateLeg.getNextActivity().equals(mainActivity)){
                    tour = candidateTour;
                    legToRemove = candidateLeg;
                    break;

                }
            }
        }

        tour.getActivities().put(stopBefore.getStartTime_s(), stopBefore);
        tour.getTrips().remove(legToRemove.getPreviousActivity());

        Leg firstLeg = new Leg(candidatePreviousActivity, stopBefore);
        double timeForFirstTrip = 60 * travelTimes.getTravelTime(candidatePreviousActivity, stopBefore, candidatePreviousActivity.getEndTime_s(), Mode.UNKNOWN.toString());
        Leg secondLeg = new Leg(stopBefore, mainActivity);
        double timeForSecondTrip = 60 * travelTimes.getTravelTime(stopBefore, mainActivity, stopBefore.getEndTime_s(), Mode.UNKNOWN.toString());
        tour.getTrips().put(firstLeg.getPreviousActivity(), firstLeg);
        tour.getTrips().put(secondLeg.getPreviousActivity(), secondLeg);

        candidatePreviousActivity.setEndTime_s(stopBefore.getStartTime_s() - 60 *
                travelTimes.getTravelTime(candidatePreviousActivity, stopBefore, stopBefore.getStartTime_s(), Mode.UNKNOWN.toString()));

    }


    /**
     * Adds one stop before the previous main activity, modifying the home activity accordingly
     * Changes the inbound trip and splits it into two subtrips
     *
     * @param stopAfter
     * @param mainActivity
     */
    public static void addStopAfter(Plan plan, Activity stopAfter, Activity mainActivity, TravelTimes travelTimes) {
        Activity candidateAfterActivity = null;
        for (Tour candidateTour : plan.getTours().values()){
            for (Leg candidateLeg : candidateTour.getTrips().values()){
                if (candidateLeg.getPreviousActivity().equals(mainActivity)){
                    candidateAfterActivity = candidateLeg.getNextActivity();
                }
            }
        }


        Leg legToRemove = null;
        Tour tour = null;
        for (Tour candidateTour : plan.getTours().values()){
            for (Leg candidateLeg : candidateTour.getTrips().values()){
                if (candidateLeg.getPreviousActivity().equals(mainActivity)){
                    tour = candidateTour;
                    legToRemove = candidateLeg;
                    break;
                }

            }
        }

        tour.getActivities().put(stopAfter.getStartTime_s(), stopAfter);
        tour.getTrips().remove(legToRemove.getPreviousActivity());

        Leg firstLeg = new Leg(mainActivity, stopAfter);
        Leg secondLeg = new Leg(stopAfter, candidateAfterActivity);
        tour.getTrips().put(firstLeg.getPreviousActivity(), firstLeg);
        tour.getTrips().put(secondLeg.getPreviousActivity(), secondLeg);

        candidateAfterActivity.setStartTime_s(stopAfter.getEndTime_s() + 60 * travelTimes.getTravelTime(stopAfter, candidateAfterActivity,
                stopAfter.getEndTime_s(), Mode.UNKNOWN.toString()));
    }
}
