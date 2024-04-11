package de.tum.bgu.msm.data;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationData;
import org.matsim.api.core.v01.population.Population;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public interface DataSet {
    TravelDistances getTravelDistancesAuto();

    TravelDistances getTravelDistancesNMT();

    void setTravelDistancesAuto(TravelDistances travelDistancesAuto);

    void setTravelDistancesNMT(TravelDistances travelDistancesNMT);

    TravelTimes getTravelTimes();

    TravelTimes setTravelTimes(TravelTimes travelTimes);

    Map<Integer, MitoPerson> getPersons();

    Map<Integer, MitoZone> getZones();

    Map<Integer, MitoHousehold> getHouseholds();

    Map<Integer, MitoSchool> getSchools();

    Map<Integer, MitoJob> getJobs();

    Map<Integer, MitoTrip> getTrips();

    Map<Integer, MitoTrip> getTripSubsample();

    void addTrip(MitoTrip trip);

    void addTrips(Collection<MitoTrip> addedTrips);

    void addTripToSubsample(MitoTrip trip);

    void addZone(MitoZone zone);

    void addHousehold(MitoHousehold household);

    void addPerson(MitoPerson person);

    void addJob(MitoJob job);

    void addSchool(MitoSchool school);

    void removeTrip(int tripId);

    double getPeakHour();

    void setPeakHour(double peakHour);

    void addModeShareForPurpose(Purpose purpose, Mode mode, Double share);

    Double getModeShareForPurpose(Purpose purpose, Mode mode);

    int getYear();

    void setYear(int year);

    EnumMap<Purpose, DoubleMatrix1D> getArrivalMinuteCumProbByPurpose();

    void setArrivalMinuteCumProbByPurpose(EnumMap<Purpose, DoubleMatrix1D> arrivalMinuteCumProbByPurpose);

    EnumMap<Purpose, DoubleMatrix1D> getDurationMinuteCumProbByPurpose();

    void setDurationMinuteCumProbByPurpose(EnumMap<Purpose, DoubleMatrix1D> durationMinuteCumProbByPurpose);

    EnumMap<Purpose, DoubleMatrix1D> getDepartureMinuteCumProbByPurpose();

    void setDepartureMinuteCumProbByPurpose(EnumMap<Purpose, DoubleMatrix1D> departureMinuteCumProbByPurpose);

    ModeChoiceCalibrationData getModeChoiceCalibrationData();

    void setPopulation(Population population);

    Population getPopulation();

    Map<Integer, MitoPerson> getModelledPersons();

    Map<Integer, MitoHousehold>  getModelledHouseholds();
}
