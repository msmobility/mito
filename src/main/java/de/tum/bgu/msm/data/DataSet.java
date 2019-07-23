package de.tum.bgu.msm.data;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

import java.util.*;

public class DataSet {

    private TravelTimes travelTimes;

    private TravelTime matsimTravelTime;
    private TravelDisutility matsimTravelDisutility;

    private TravelDistances travelDistancesAuto;
    private TravelDistances travelDistancesUAM;
    private TravelDistances travelDistancesNMT;
    private TravelDistances travelDistanceDisability;

    private double peakHour = Double.NaN;

    private final Map<Integer, MitoZone> zones= new LinkedHashMap<>();
    private final Map<Integer, MitoHousehold> households = new LinkedHashMap<>();
    private final Map<Integer, MitoPerson> persons = new LinkedHashMap<>();
    private final Map<Integer, MitoSchool> schools = new LinkedHashMap<>();
    private final Map<Integer, MitoJob> jobs = new LinkedHashMap<>();

    private final Map<Integer, MitoTrip> trips = new LinkedHashMap<>();
    private final Map<Integer, MitoTrip> tripSubsample = new LinkedHashMap<>();


    private final Table<Purpose, Mode, Double> modeSharesByPurpose
            = ArrayTable.create(Arrays.asList(Purpose.values()), Arrays.asList(Mode.values()));


    private final Table<Purpose, Mode, Double> modeSharesByPurposeWithoutDisability
            = ArrayTable.create(Arrays.asList(Purpose.values()), Arrays.asList(Mode.values()));

    private final Table<Purpose, Mode, Double> modeSharesByPurposeMentalDisability
            = ArrayTable.create(Arrays.asList(Purpose.values()), Arrays.asList(Mode.values()));

    private final Table<Purpose, Mode, Double> modeSharesByPurposePhysicalDisability
            = ArrayTable.create(Arrays.asList(Purpose.values()), Arrays.asList(Mode.values()));

    private final Table<Purpose, Disability, Integer> tripsByPurposeByDisability
            = ArrayTable.create(Arrays.asList(Purpose.values()), Arrays.asList(Disability.values()));


    private int year;

    public TravelDistances getTravelDistancesAuto(){return this.travelDistancesAuto;}

    public TravelDistances getTravelDistancesNMT(){return this.travelDistancesNMT;}

    public TravelDistances getTravelDistancesUAM(){return this.travelDistancesUAM;}

    public void setTravelDistancesAuto(TravelDistances travelDistancesAuto){this.travelDistancesAuto = travelDistancesAuto;}

    public void setTravelDistancesNMT(TravelDistances travelDistancesNMT){this.travelDistancesNMT = travelDistancesNMT;}

    public void setTravelDistancesUAM(TravelDistances travelDistancesUAM){this.travelDistancesUAM = travelDistancesUAM;}

    public TravelTimes getTravelTimes() {
        return this.travelTimes;
    }

    public TravelTimes setTravelTimes(TravelTimes travelTimes) {
        return this.travelTimes = travelTimes;
    }

    public Map<Integer, MitoPerson> getPersons() {
        return Collections.unmodifiableMap(persons);
    }

    public Map<Integer, MitoZone> getZones() {
        return Collections.unmodifiableMap(zones);
    }

    public Map<Integer, MitoHousehold> getHouseholds() {
        return Collections.unmodifiableMap(households);
    }

    public Map<Integer, MitoSchool> getSchools() {
        return Collections.unmodifiableMap(schools);
    }

    public Map<Integer, MitoJob> getJobs() {
        return Collections.unmodifiableMap(jobs);
    }

    public Map<Integer, MitoTrip> getTrips() {
        return Collections.unmodifiableMap(trips);
    }

    public Map<Integer, MitoTrip> getTripSubsample() {
        return Collections.unmodifiableMap(tripSubsample);
    }

    public void addTrip(final MitoTrip trip) {
        MitoTrip test = trips.putIfAbsent(trip.getId(), trip);
        if(test != null) {
            throw new IllegalArgumentException("MitoTrip id " + trip.getId() + " already exists!");
        }
    }

    public void addTrips(final Collection<MitoTrip> addedTrips) {
        for(MitoTrip trip: addedTrips) {
            addTrip(trip);
        }
    }

    public void addTripToSubsample(final MitoTrip trip) {
        MitoTrip test = tripSubsample.putIfAbsent(trip.getId(), trip);
        if(test != null) {
            throw new IllegalArgumentException("MitoTrip id " + trip.getId() + " already exists!");
        }
    }

    public void addZone(final MitoZone zone) {
        MitoZone test = zones.putIfAbsent(zone.getId(), zone);
        if(test != null) {
            throw new IllegalArgumentException("MitoZone id " + zone.getId() + " already exists!");
        }
    }

    public void addHousehold(final MitoHousehold household) {
        MitoHousehold test = households.putIfAbsent(household.getId(), household);
        if(test != null) {
            throw new IllegalArgumentException("MitoHousehold id " + household.getId() + " already exists!");
        }
    }

    public void addPerson(final MitoPerson person) {
        MitoPerson test = persons.putIfAbsent(person.getId(), person);
        if(test != null) {
            throw new IllegalArgumentException("MitoPerson id " + person.getId() + " already exists!");
        }
    }

    public void addJob(final MitoJob job) {
        MitoJob test = jobs.putIfAbsent(job.getId(), job);
        if(test != null) {
            throw new IllegalArgumentException("MitoJob id " + job.getId() + " already exists!");
        }
    }

    public void addSchool(final MitoSchool school) {
        MitoSchool test = schools.putIfAbsent(school.getId(), school);
        if(test != null) {
            throw new IllegalArgumentException("MitoSchool id " + school.getId() + " already exists!");
        }
    }

    public synchronized void removeTrip(final int tripId) {
        trips.remove(tripId);
    }

    public double getPeakHour() {
        return peakHour;
    }

    public void setPeakHour(double peakHour) {
        this.peakHour = peakHour;
    }

    public static int getFemalesForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getMitoGender().equals(MitoGender.FEMALE)).count();
    }

    public static int getChildrenForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getAge() < 18).count();
    }

    public static int getYoungAdultsForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getAge() >= 18 && person.getAge() <= 25).count();

    }

    public static int getRetireesForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getAge() > 65).count();
    }

    public static int getNumberOfWorkersForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getMitoOccupationStatus() == MitoOccupationStatus.WORKER).count();

    }

    public static int getStudentsForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getMitoOccupationStatus() == MitoOccupationStatus.STUDENT).count();

    }

    public static int getLicenseHoldersForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(MitoPerson::hasDriversLicense).count();
    }

    public static int getRestrictedMobility(MitoHousehold household) {
        if ((int) household.getPersons().values().stream().filter(person ->
                person.getDisability().equals(Disability.WITHOUT)).count() != household.getHhSize()){
            return 1;
        }
        return 0;
    }

    public void addModeShareForPurpose(Purpose purpose, Mode mode, Double share){
        modeSharesByPurpose.put(purpose, mode, share);
    }

    public Double getModeShareForPurpose(Purpose purpose, Mode mode){
        return modeSharesByPurpose.get(purpose, mode);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year){
        this.year = year;
    }

    public TravelTime getMatsimTravelTime() {
        return matsimTravelTime;
    }

    public void setMatsimTravelTime(TravelTime matsimTravelTime) {
        this.matsimTravelTime = matsimTravelTime;
    }

    public TravelDisutility getMatsimTravelDisutility() {
        return matsimTravelDisutility;
    }

    public void setMatsimTravelDisutility(TravelDisutility matsimTravelDisutility) {
        this.matsimTravelDisutility = matsimTravelDisutility;
    }

    public void addModeShareForPurposeWithoutDisability(Purpose purpose, Mode mode, Double share){
        modeSharesByPurposeWithoutDisability.put(purpose, mode, share);
    }

    public Double getModeSharesByPurposeWithoutDisability(Purpose purpose, Mode mode) {
        return modeSharesByPurposeWithoutDisability.get(purpose, mode);
    }

    public void addModeShareForPurposeMentalDisability(Purpose purpose, Mode mode, Double share){
        modeSharesByPurposeMentalDisability.put(purpose, mode, share);
    }

    public Double getModeSharesByPurposeMentalDisability(Purpose purpose, Mode mode) {
        return modeSharesByPurposeMentalDisability.get(purpose, mode);
    }

    public void addModeShareForPurposePhysicalDisability(Purpose purpose, Mode mode, Double share){
        modeSharesByPurposePhysicalDisability.put(purpose, mode, share);
    }

    public Double getModeSharesByPurposePhysicalDisability(Purpose purpose, Mode mode) {
        return modeSharesByPurposePhysicalDisability.get(purpose, mode);
    }

    public void addTripByPurposeByDisability(Purpose purpose, Disability disability, int trips){
        tripsByPurposeByDisability.put(purpose, disability, trips++);
    }

    public int getTripsByPurposeByDisability(Purpose purpose, Disability disability){
        return tripsByPurposeByDisability.get(purpose, disability);
    }
}
