package de.tum.bgu.msm;

import com.pb.common.util.ResourceUtil;

import java.util.ResourceBundle;

/**
 * Created by Nico on 17.07.2017.
 */
public class Properties {

    private static ResourceBundle resources;

    public static final String RANDOM_SEED = "random.seed";

    public static final String PURPOSES = "trip.purposes";

    public static final String REMOVE_TRIPS_AT_BORDER = "reduce.trips.at.outer.border";

    public static final String AUTO_PEAK_SKIM = "auto.peak.sov.skim";
    public static final String DISTANCE_SKIM = "distanceODmatrix";
    public static final String EMPLOYMENT = "employment.forecast";
    public static final String JOBS = "job.file.ascii";
    public static final String HOUSEHOLDS = "household.file.ascii";
    public static final String PERSONS = "person.file.ascii";
    public static final String REGIONS = "household.travel.survey.reg";
    public static final String REDUCTION_NEAR_BORDER_DAMPERS = "reduction.near.outer.border";
    public static final String SCHOOL_ENROLLMENT = "school.enrollment.data";
    public static final String TRANSIT_PEAK_SKIM = "transit.peak.time";
    public static final String ZONES = "zonal.data.file";

    public static final String TRAVEL_TIME_BUDGET_UEC_FILE          = "travel.time.budget.UEC.File";
    public static final String TRAVEL_TIME_BUDGET_UEC_DATA_SHEET    = "ttb.UEC.DataSheetNumber";
    public static final String TOTAL_TRAVEL_TIME_BUDGET_UEC_UTILITY = "total.ttb.UEC.Utility";
    public static final String HBS_TRAVEL_TIME_BUDGET_UEC_UTILITY   = "hbs.ttb.UEC.Utility";
    public static final String HBO_TRAVEL_TIME_BUDGET_UEC_UTILITY   = "hbo.ttb.UEC.Utility";
    public static final String NHBW_TRAVEL_TIME_BUDGET_UEC_UTILITY  = "nhbw.ttb.UEC.Utility";
    public static final String NHBO_TRAVEL_TIME_BUDGET_UEC_UTILITY  = "nhbo.ttb.UEC.Utility";
    public static final String LOG_UTILITY_CALCULATION_TOTAL_TTB    = "log.util.total.ttb";
    public static final String LOG_UTILITY_CALCULATION_HBS_TTB      = "log.util.hbs.ttb";
    public static final String LOG_UTILITY_CALCULATION_HBO_TTB      = "log.util.hbo.ttb";
    public static final String LOG_UTILITY_CALCULATION_NHBW_TTB     = "log.util.nhbw.ttb";
    public static final String LOG_UTILITY_CALCULATION_NHBO_TTB     = "log.util.nhbo.ttb";

    public static final String TRAVEL_SURVEY_HOUSEHOLDS = "household.travel.survey.hh";
    public static final String TRAVEL_SURVEY_TRIPS = "household.travel.survey.trips";

    public static final String TRIP_ATTRACTION_RATES = "trip.attraction.rates";
    public static final String TRIP_PRODUCTION_OUTPUT = "trip.production.output";
    public static final String TRIP_ATTRACTION_OUTPUT = "trip.attraction.output";

    public static final String BASE_DIRECTORY = "base.directory";

    public static void load(ResourceBundle resources) {
        Properties.resources = resources;
    }

    public static int getInt(String key) {
        return ResourceUtil.getIntegerProperty(resources, key);
    }

    public static String getString(String key) {
        return ResourceUtil.getProperty(resources, key);
    }

    public static String[] getArray(String key) {
        return ResourceUtil.getArray(resources, key);
    }

    public static boolean getBoolean(String key) {
        return ResourceUtil.getBooleanProperty(resources, key);
    }
}
