package de.tum.bgu.msm.data;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationData;
import org.matsim.api.core.v01.population.Population;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.sexp.*;

import java.util.*;
import java.util.function.Predicate;

public class DataSet {

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

    public TravelDistances getTravelDistancesAuto(){return this.travelDistancesAuto;}

    public TravelDistances getTravelDistancesNMT(){return this.travelDistancesNMT;}

    public void setTravelDistancesAuto(TravelDistances travelDistancesAuto){this.travelDistancesAuto = travelDistancesAuto;}

    public void setTravelDistancesNMT(TravelDistances travelDistancesNMT){this.travelDistancesNMT = travelDistancesNMT;}

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

    public static int countMembersByAttribute(MitoHousehold household,int minAge,int maxAge) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getAge() >= minAge & person.getAge() <= maxAge).count();
    }

    public static int countMembersByAttribute(MitoHousehold household,int minAge,int maxAge, MitoOccupationStatus occupation) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getAge() >= minAge & person.getAge() <= maxAge &
                        person.getMitoOccupationStatus().equals(occupation)).count();
    }

    public static int countMembersByFilter(MitoHousehold household, Predicate<MitoPerson> filter) {
        return (int) household.getPersons().values().stream().filter(filter).count();
    }


    public static int getLicenseHoldersForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(MitoPerson::hasDriversLicense).count();
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

    public EnumMap<Purpose, DoubleMatrix1D> getArrivalMinuteCumProbByPurpose() {
        return arrivalMinuteCumProbByPurpose;
    }

    public void setArrivalMinuteCumProbByPurpose(EnumMap<Purpose, DoubleMatrix1D> arrivalMinuteCumProbByPurpose) {
        this.arrivalMinuteCumProbByPurpose = arrivalMinuteCumProbByPurpose;
    }

    public EnumMap<Purpose, DoubleMatrix1D> getDurationMinuteCumProbByPurpose() {
        return durationMinuteCumProbByPurpose;
    }

    public void setDurationMinuteCumProbByPurpose(EnumMap<Purpose, DoubleMatrix1D> durationMinuteCumProbByPurpose) {
        this.durationMinuteCumProbByPurpose = durationMinuteCumProbByPurpose;
    }

    public EnumMap<Purpose, DoubleMatrix1D> getDepartureMinuteCumProbByPurpose() {
        return departureMinuteCumProbByPurpose;
    }

    public void setDepartureMinuteCumProbByPurpose(EnumMap<Purpose, DoubleMatrix1D> departureMinuteCumProbByPurpose) {
        this.departureMinuteCumProbByPurpose = departureMinuteCumProbByPurpose;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    public Population getPopulation() {
        return population;
    }

    public ModeChoiceCalibrationData getModeChoiceCalibrationData() {
        return modeChoiceCalibrationData;
    }

    private int determineAreaType(MitoHousehold hh) {
        int areaType = -1;
        if (hh.getHomeZone() != null) {
            areaType = hh.getHomeZone().getAreaTypeSG().code() / 10;
        } else {
            System.out.println("Home MitoZone for Household  " + hh.getId() + " is null!");
        }
        return areaType;
    }

    private ListVector RDataFrame;
    public ListVector getRdataFrame() {return this.RDataFrame; }

    // Method to create R data frame
    public void buildRdataFrame() {

        //Create Builders for each Variable
        IntArrayVector.Builder hhId = new IntArrayVector.Builder();

        IntArrayVector.Builder hhSize = new IntArrayVector.Builder();
        IntArrayVector.Builder hhSize1 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhSize2 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhSize3 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhSize23 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhSize4 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhSize5 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhSize345 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhSize45 = new IntArrayVector.Builder();

        IntArrayVector.Builder hhPersons0to6 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersons6to17 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersons18to29_worker = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersons18to29_student = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersons18to29_unemployed = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersons30to64_worker = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersons30to64_student = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersons18to64_student = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersons30to64_unemployed = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersons18to64_unemployed = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersons65up = new IntArrayVector.Builder();

        IntArrayVector.Builder hhPersonsFemale = new IntArrayVector.Builder();
        IntArrayVector.Builder hhPersonsWithMobilityRestriction = new IntArrayVector.Builder();

        IntArrayVector.Builder hhEconomicStatus = new IntArrayVector.Builder();
        IntArrayVector.Builder hhEconomicStatus2 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhEconomicStatus3 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhEconomicStatus23 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhEconomicStatus4 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhEconomicStatus5 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhEconomicStatus45 = new IntArrayVector.Builder();

        IntArrayVector.Builder hhAutos = new IntArrayVector.Builder();
        DoubleArrayVector.Builder hhPropAutos = new DoubleArrayVector.Builder();

        IntArrayVector.Builder hhRegionType = new IntArrayVector.Builder();
        IntArrayVector.Builder hhRegionType2 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhRegionType3 = new IntArrayVector.Builder();
        IntArrayVector.Builder hhRegionType4 = new IntArrayVector.Builder();

        // Loop through dataSet and assign values
        final Iterator<MitoHousehold> iterator = this.getHouseholds().values().iterator();
        for (; iterator.hasNext(); ) {
            MitoHousehold hh = iterator.next();

            hhId.add(hh.getId());
            int householdSize = hh.getHhSize();
            hhSize.add(householdSize);
            hhSize1.add(householdSize == 1 ? 1 : 0);
            hhSize2.add(householdSize == 2 ? 1 : 0);
            hhSize23.add(householdSize == 2 | householdSize == 3 ? 1 : 0);
            hhSize3.add(householdSize == 3 ? 1 : 0);
            hhSize4.add(householdSize == 4 ? 1 : 0);
            hhSize5.add(householdSize >= 5 ? 1 : 0);
            hhSize345.add(householdSize >= 3 ? 1 : 0);
            hhSize45.add(householdSize >= 4 ? 1 : 0);

            hhPersons0to6.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() < 6));
            hhPersons6to17.add(DataSet.countMembersByFilter(hh,  mitoPerson ->
                    mitoPerson.getAge() >= 6 & mitoPerson.getAge() <= 17));
            hhPersons18to29_worker.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() >= 18 & mitoPerson.getAge() <= 29 &
                            mitoPerson.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)));
            hhPersons18to29_student.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() >= 18 & mitoPerson.getAge() <= 29 &
                            mitoPerson.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)));
            hhPersons18to29_unemployed.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() >= 18 & mitoPerson.getAge() <= 29 &
                            mitoPerson.getMitoOccupationStatus().equals(MitoOccupationStatus.UNEMPLOYED)));
            hhPersons30to64_worker.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() >= 30 & mitoPerson.getAge() <= 64 &
                            mitoPerson.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)));
            hhPersons30to64_student.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() >= 30 & mitoPerson.getAge() <= 64 &
                            mitoPerson.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)));
            hhPersons18to64_student.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() >= 18 & mitoPerson.getAge() <= 64 &
                            mitoPerson.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)));
            hhPersons30to64_unemployed.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() >= 30 & mitoPerson.getAge() <= 64 &
                            mitoPerson.getMitoOccupationStatus().equals(MitoOccupationStatus.UNEMPLOYED)));
            hhPersons18to64_unemployed.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() >= 18 & mitoPerson.getAge() <= 64 &
                            mitoPerson.getMitoOccupationStatus().equals(MitoOccupationStatus.UNEMPLOYED)));
            hhPersons65up.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() >= 65));


            hhPersonsFemale.add(DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getMitoGender().equals(MitoGender.FEMALE)));
            hhPersonsWithMobilityRestriction.add(0); //todo: include actual disability status (currently 0)

            int economicStatus = hh.getEconomicStatus();
            hhEconomicStatus.add(economicStatus);
            hhEconomicStatus2.add(economicStatus == 2 ? 1 : 0);
            hhEconomicStatus3.add(economicStatus == 3 ? 1 : 0);
            hhEconomicStatus23.add(economicStatus == 2 || economicStatus == 3 ? 1 : 0);
            hhEconomicStatus4.add(economicStatus == 4 ? 1 : 0);
            hhEconomicStatus5.add(economicStatus == 5 ? 1 : 0);
            hhEconomicStatus45.add(economicStatus >= 4 ? 1 : 0);

            hhAutos.add(hh.getAutos());
            hhPropAutos.add(Math.min(1.0,((double) hh.getAutos() / DataSet.countMembersByFilter(hh, mitoPerson ->
                    mitoPerson.getAge() >= 15))));

            int hhAreaType = determineAreaType(hh);
            hhRegionType.add(hhAreaType);
            hhRegionType2.add(hhAreaType == 2 ? 1 : 0);
            hhRegionType3.add(hhAreaType == 3 ? 1 : 0);
            hhRegionType4.add(hhAreaType == 4 ? 1 : 0);
        }

        // Add all vectors to a R data frame
        ListVector.NamedBuilder RModelDataBuilder = new ListVector.NamedBuilder();
        RModelDataBuilder.setAttribute(Symbols.CLASS, StringVector.valueOf("data.frame"));
        RModelDataBuilder.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(this.getHouseholds().size()));
        RModelDataBuilder.add("hh.id",hhId.build());
        RModelDataBuilder.add("hh.size",hhSize.build());
        RModelDataBuilder.add("hh.size_1",hhSize1.build());
        RModelDataBuilder.add("hh.size_2",hhSize2.build());
        RModelDataBuilder.add("hh.size_3",hhSize3.build());
        RModelDataBuilder.add("hh.size_23",hhSize3.build());
        RModelDataBuilder.add("hh.size_4",hhSize4.build());
        RModelDataBuilder.add("hh.size_5",hhSize5.build());
        RModelDataBuilder.add("hh.size_345",hhSize345.build());
        RModelDataBuilder.add("hh.size_45",hhSize45.build());

        RModelDataBuilder.add("hh.pers_under6",hhPersons0to6.build());
        RModelDataBuilder.add("hh.pers_6to17",hhPersons6to17.build());
        RModelDataBuilder.add("hh.pers_18to29_w",hhPersons18to29_worker.build());
        RModelDataBuilder.add("hh.pers_18to29_s",hhPersons18to29_student.build());
        RModelDataBuilder.add("hh.pers_18to29_u",hhPersons18to29_unemployed.build());
        RModelDataBuilder.add("hh.pers_30to64_w",hhPersons30to64_worker.build());
        RModelDataBuilder.add("hh.pers_30to64_s",hhPersons30to64_student.build());
        RModelDataBuilder.add("hh.pers_18to64_s",hhPersons18to64_student.build());
        RModelDataBuilder.add("hh.pers_30to64_u",hhPersons30to64_unemployed.build());
        RModelDataBuilder.add("hh.pers_18to64_u",hhPersons18to64_unemployed.build());
        RModelDataBuilder.add("hh.pers_65up",hhPersons65up.build());

        RModelDataBuilder.add("hh.pers_female",hhPersonsFemale.build());
        RModelDataBuilder.add("hh.pers_mobilityRestriction",hhPersonsWithMobilityRestriction.build());

        RModelDataBuilder.add("hh.economicStatus",hhEconomicStatus.build());
        RModelDataBuilder.add("hh.economicStatus_2",hhEconomicStatus2.build());
        RModelDataBuilder.add("hh.economicStatus_3",hhEconomicStatus3.build());
        RModelDataBuilder.add("hh.economicStatus_23",hhEconomicStatus23.build());
        RModelDataBuilder.add("hh.economicStatus_4",hhEconomicStatus4.build());
        RModelDataBuilder.add("hh.economicStatus_5",hhEconomicStatus5.build());

        RModelDataBuilder.add("hh.autos",hhAutos.build());
        RModelDataBuilder.add("hh.propAutos",hhPropAutos.build());

        RModelDataBuilder.add("hh.BBSR",hhRegionType.build());
        RModelDataBuilder.add("hh.BBSR_2",hhRegionType2.build());
        RModelDataBuilder.add("hh.BBSR_3",hhRegionType3.build());
        RModelDataBuilder.add("hh.BBSR_4",hhRegionType4.build());

        this.RDataFrame = RModelDataBuilder.build();

    }











}
