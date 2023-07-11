/*
package de.tum.bgu.msm.analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.TransitDriverStartsEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.TransitDriverStartsEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

import java.util.HashSet;
import java.util.Set;

public class PtEventHandler implements PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler,
        VehicleArrivesAtFacilityEventHandler, VehicleDepartsAtFacilityEventHandler, TransitDriverStartsEventHandler {

    private final Set<Id<Person>> transitDrivers = new HashSet<>();
    private final Set<Id<Vehicle>> transitVehicles = new HashSet<>();

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (this.transitDrivers.contains(event.getPersonId()) || !this.transitVehicles.contains(event.getVehicleId())) {
            return; // ignore transit drivers or persons entering non-transit vehicles
        }

        Id<Vehicle> vehId = event.getVehicleId();
        Id<TransitStopFacility> stopId = this.vehStops.get(vehId);
        double time = event.getTime();
        // --------------------------getOns---------------------------
        int[] getOn = this.boards.get(stopId);
        if (getOn == null) {
            getOn = new int[this.maxSlotIndex + 1];
            this.boards.put(stopId, getOn);
        }
        getOn[getTimeSlotIndex(time)]++;
        // ------------------------veh_passenger---------------------------
        Integer nPassengers = this.vehPassengers.get(vehId);
        this.vehPassengers.put(vehId, (nPassengers != null) ? (nPassengers + 1) : 1);
        this.occupancyRecord.append("time :\t").append(time).append(" veh :\t").append(vehId).append(" has Passenger\t").append(this.vehPassengers.get(vehId)).append(" \tat stop :\t").append(stopId).append(" ENTERING PERSON :\t").append(event.getPersonId()).append("\n");

    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {

    }

    @Override
    public void handleEvent(TransitDriverStartsEvent event) {
        this.transitDrivers.add(event.getDriverId());
        this.transitVehicles.add(event.getVehicleId());
    }

    @Override
    public void handleEvent(VehicleArrivesAtFacilityEvent event) {

    }

    @Override
    public void handleEvent(VehicleDepartsAtFacilityEvent event) {

    }
}
*/
