package de.tum.bgu.msm.data;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationData;
import org.matsim.api.core.v01.population.Population;

import java.util.*;
import java.util.stream.Collectors;

public class DataSetImpl implements DataSet {

    private TravelTimes travelTimes;

    private TravelDistances travelDistancesAuto;
    private TravelDistances travelDistancesNMT;

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


    private int year;

    private EnumMap<Purpose, DoubleMatrix1D> arrivalMinuteCumProbByPurpose;
    private EnumMap<Purpose, DoubleMatrix1D> durationMinuteCumProbByPurpose;
    private EnumMap<Purpose, DoubleMatrix1D> departureMinuteCumProbByPurpose;

    private Population population;
    private final ModeChoiceCalibrationData modeChoiceCalibrationData = new ModeChoiceCalibrationData();

    @Override
    public TravelDistances getTravelDistancesAuto(){return this.travelDistancesAuto;}

    @Override
    public TravelDistances getTravelDistancesNMT(){return this.travelDistancesNMT;}

    @Override
    public void setTravelDistancesAuto(TravelDistances travelDistancesAuto){this.travelDistancesAuto = travelDistancesAuto;}

    @Override
    public void setTravelDistancesNMT(TravelDistances travelDistancesNMT){this.travelDistancesNMT = travelDistancesNMT;}

    @Override
    public TravelTimes getTravelTimes() {
        return this.travelTimes;
    }

    @Override
    public TravelTimes setTravelTimes(TravelTimes travelTimes) {
        return this.travelTimes = travelTimes;
    }

    @Override
    public Map<Integer, MitoPerson> getPersons() {
        return Collections.unmodifiableMap(persons);
    }

    @Override
    public Map<Integer, MitoZone> getZones() {
        return Collections.unmodifiableMap(zones);
    }

    @Override
    public Map<Integer, MitoHousehold> getHouseholds() {
        return Collections.unmodifiableMap(households);
    }

    @Override
    public Map<Integer, MitoSchool> getSchools() {
        return Collections.unmodifiableMap(schools);
    }

    @Override
    public Map<Integer, MitoJob> getJobs() {
        return Collections.unmodifiableMap(jobs);
    }

    @Override
    public Map<Integer, MitoTrip> getTrips() {
        return Collections.unmodifiableMap(trips);
    }

    @Override
    public Map<Integer, MitoTrip> getTripSubsample() {
        return Collections.unmodifiableMap(tripSubsample);
    }

    @Override
    public void addTrip(final MitoTrip trip) {
        MitoTrip test = trips.putIfAbsent(trip.getId(), trip);
        if(test != null) {
            throw new IllegalArgumentException("MitoTrip id " + trip.getId() + " already exists!");
        }
    }

    @Override
    public void addTrips(final Collection<MitoTrip> addedTrips) {
        for(MitoTrip trip: addedTrips) {
            addTrip(trip);
        }
    }

    @Override
    public void addTripToSubsample(final MitoTrip trip) {
        MitoTrip test = tripSubsample.putIfAbsent(trip.getId(), trip);
        if(test != null) {
            throw new IllegalArgumentException("MitoTrip id " + trip.getId() + " already exists!");
        }
    }

    @Override
    public void addZone(final MitoZone zone) {
        MitoZone test = zones.putIfAbsent(zone.getId(), zone);
        if(test != null) {
            throw new IllegalArgumentException("MitoZone id " + zone.getId() + " already exists!");
        }
    }

    @Override
    public void addHousehold(final MitoHousehold household) {
        MitoHousehold test = households.putIfAbsent(household.getId(), household);
        if(test != null) {
            throw new IllegalArgumentException("MitoHousehold id " + household.getId() + " already exists!");
        }
    }

    @Override
    public void addPerson(final MitoPerson person) {
        MitoPerson test = persons.putIfAbsent(person.getId(), person);
        if(test != null) {
            throw new IllegalArgumentException("MitoPerson id " + person.getId() + " already exists!");
        }
    }

    @Override
    public void addJob(final MitoJob job) {
        MitoJob test = jobs.putIfAbsent(job.getId(), job);
        if(test != null) {
            throw new IllegalArgumentException("MitoJob id " + job.getId() + " already exists!");
        }
    }

    @Override
    public void addSchool(final MitoSchool school) {
        MitoSchool test = schools.putIfAbsent(school.getId(), school);
        if(test != null) {
            throw new IllegalArgumentException("MitoSchool id " + school.getId() + " already exists!");
        }
    }

    @Override
    public synchronized void removeTrip(final int tripId) {
        trips.remove(tripId);
    }

    @Override
    public double getPeakHour() {
        return peakHour;
    }

    @Override
    public void setPeakHour(double peakHour) {
        this.peakHour = peakHour;
    }

    @Override
    public void addModeShareForPurpose(Purpose purpose, Mode mode, Double share){
        modeSharesByPurpose.put(purpose, mode, share);
    }

    @Override
    public Double getModeShareForPurpose(Purpose purpose, Mode mode){
        return modeSharesByPurpose.get(purpose, mode);
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public void setYear(int year){
        this.year = year;
    }

    @Override
    public EnumMap<Purpose, DoubleMatrix1D> getArrivalMinuteCumProbByPurpose() {
        return arrivalMinuteCumProbByPurpose;
    }

    @Override
    public void setArrivalMinuteCumProbByPurpose(EnumMap<Purpose, DoubleMatrix1D> arrivalMinuteCumProbByPurpose) {
        this.arrivalMinuteCumProbByPurpose = arrivalMinuteCumProbByPurpose;
    }

    @Override
    public EnumMap<Purpose, DoubleMatrix1D> getDurationMinuteCumProbByPurpose() {
        return durationMinuteCumProbByPurpose;
    }

    @Override
    public void setDurationMinuteCumProbByPurpose(EnumMap<Purpose, DoubleMatrix1D> durationMinuteCumProbByPurpose) {
        this.durationMinuteCumProbByPurpose = durationMinuteCumProbByPurpose;
    }

    @Override
    public EnumMap<Purpose, DoubleMatrix1D> getDepartureMinuteCumProbByPurpose() {
        return departureMinuteCumProbByPurpose;
    }

    @Override
    public void setDepartureMinuteCumProbByPurpose(EnumMap<Purpose, DoubleMatrix1D> departureMinuteCumProbByPurpose) {
        this.departureMinuteCumProbByPurpose = departureMinuteCumProbByPurpose;
    }

    @Override
    public void setPopulation(Population population) {
        this.population = population;
    }

    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public ModeChoiceCalibrationData getModeChoiceCalibrationData() {
        return modeChoiceCalibrationData;
    }

    public Map<Integer, MitoPerson> getModelledPersons() {
        return persons.entrySet().stream()
                .filter(person -> person.getValue().getHousehold().isModelled())
                .collect(Collectors.toUnmodifiableMap(e -> e.getKey(), e -> e.getValue()));
    }

    public Map<Integer, MitoHousehold> getModelledHouseholds() {
        return households.entrySet().stream()
                .filter(household -> household.getValue().isModelled())
                .collect(Collectors.toUnmodifiableMap(e -> e.getKey(), e -> e.getValue()));
    }


}
