package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.CloseableThreadContext;

import java.util.*;

/**
 * Holds aggregate persona objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Ana Moreno
 * Created on May 8, 2024 in Valencia, Spain
 *
 */
public class MitoAggregatePersona implements Id {

    private static final Logger logger = Logger.getLogger(MitoAggregatePersona.class);

    private final int id;
    private final String name;

    private Set<MitoTrip> trips = new LinkedHashSet<>();

    private Map<String, Double> aggregateAttributes = new LinkedHashMap<>();

    private Map<String, String> aggregateStringAttributes = new LinkedHashMap<>();


    private final EnumMap<Purpose, List<MitoTrip>> tripsByPurpose = new EnumMap<>(Purpose.class);

    public MitoAggregatePersona(int id, String name) {
        this.id = id;
        this.name = name;
    }


    @Override
    public int getId() {
        return this.id;
    }


    public Set<MitoTrip> getTrips() {
        return Collections.unmodifiableSet(this.trips);
    }

    public void addTrip(MitoTrip trip) {
        this.trips.add(trip);
        if(trip.getPersona() != this) {
            trip.setPersona(this);
        }
    }

    public void removeTripFromPerson(MitoTrip trip){
        trips.remove(trip);
    }

    @Override
    public int hashCode() {
        return id;
    }


    public Map<String, Double> getAggregateAttributes() {
        return aggregateAttributes;
    }

    public void setAggregateAttributes(Map<String, Double> aggregateAttributes) {
        this.aggregateAttributes = aggregateAttributes;
    }

    public Map<String, String> getAggregateStringAttributes() {
        return aggregateStringAttributes;
    }

    public void setAggregateStringAttributes(Map<String, String> aggregateStringAttributes) {
        this.aggregateStringAttributes = aggregateStringAttributes;
    }

    public synchronized void setTripsByPurpose(List<MitoTrip> trips, Purpose purpose) {
        tripsByPurpose.put(purpose, trips);
    }

    public List<MitoTrip> getTripsForPurpose(Purpose purpose) {
        if(tripsByPurpose.get(purpose) != null) {
            return tripsByPurpose.get(purpose);
        } else {
            return Collections.emptyList();
        }
    }


}
