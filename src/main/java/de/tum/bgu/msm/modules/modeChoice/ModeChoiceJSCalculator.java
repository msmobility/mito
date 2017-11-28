package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class ModeChoiceJSCalculator extends JavaScriptCalculator<Double[]>{

    protected ModeChoiceJSCalculator(Reader reader) {
        super(reader);
    }

    public void setPurpose(Purpose purpose) {
        bindings.put("purpose", purpose);
    }

    public void setHouseholdAttributes(MitoHousehold household){
        bindings.put("hhSize", household.getHhSize());
        bindings.put("hhIncome", household.getIncome());
        bindings.put("hhAutos", household.getAutos());
        bindings.put("nWorkers", MitoUtil.getNumberOfWorkersForHousehold(household));
    }

    public void setPersonAttributes(MitoPerson person){
        bindings.put("age", person.getAge());
        bindings.put("gender", person.getGender());
        bindings.put("driversLicense", person.hasDriversLicense());
    }

    public void setAreaTypesAndDistanceToTransit(MitoTrip trip){
        bindings.put("distanceToNearestTransitStop", trip.getTripOrigin().getDistanceToNearestTransitStop() );
        bindings.put("areaTypeHBW", trip.getTripOrigin().getAreaTypeHBWModeChoice());
        bindings.put("areaTypeNHBO", trip.getTripOrigin().getAreaTypeNHBOModeChoice());
    }

    /*public void setAreaTypes(Map<String, String> areaTypes){
        for(Map.Entry<String, String> entry : areaTypes.entrySet()){
            bindings.put(entry.getKey(), entry.getValue());
        }
    }*/

    public void setTravelTimeForMode(String mode, double travelTimeFromTo) {
        bindings.put("travelTime" + mode, travelTimeFromTo);
    }

    public void setTravelDistance(double distance) {
        bindings.put("travelDistance", distance);
    }

}
