package de.tum.bgu.msm.resources;

public class Properties {

    public static final String WRITE_MATSIM_POPULATION = "write.matsim.population";

    static final String ZONES = "zonal.data.file";
    static final String ZONE_SHAPEFILE = "zone.shapefile";
    static final String REDUCTION_NEAR_BORDER_DAMPERS = "reduction.near.outer.border";
    static final String ECONOMIC_STATUS = "economic.status.definition";

    static final String JOBS = "job.file.ascii";
    static final String SCHOOLS = "school.file.ascii";
    static final String HOUSEHOLDS = "household.file.ascii";
    static final String PERSONS = "person.file.ascii";
    static final String DWELLINGS = "dwelling.file.ascii";

    public static final String SCENARIO_NAME = "scenario.name";
    public static final String SCENARIO_YEAR = "year";

    public static final String RANDOM_SEED = "random.seed";

    public static final String REMOVE_TRIPS_AT_BORDER = "reduce.trips.at.outer.border";

    public static final String AUTO_PEAK_SKIM = "auto.peak.travelTime";

    public static final String TRIP_ATTRACTION_RATES = "trip.attraction.rates";
    public static final String TRIP_PRODUCTION_OUTPUT = "trip.production.output";
    public static final String TRIP_ATTRACTION_OUTPUT = "trip.attraction.output";
    public static final String SCALE_FACTOR_FOR_TRIP_GENERATION = "tg.scale.factor";
    public static final String TG_BINARY_LOGIT_COEFFICIENTS = "tg.binary.logit.coeffs";
    public static final String TG_NEGATIVE_BINOMIAL_COEFFICIENTS = "tg.negative.binomial.coeffs";

    public static final String BUS_TRAVEL_TIME_SKIM = "bus.travelTime";
    public static final String TRAM_METRO_TRAVEL_TIME_SKIM = "tramMetro.travelTime";
    public static final String TRAIN_TRAVEL_TIME_SKIM = "train.travelTime";
    public static final String AUTO_TRAVEL_DISTANCE_SKIM = "auto.travelDistance";
    public static final String NMT_TRAVEL_DISTANCE_SKIM = "nmt.travelDistance";
    public static final String AREA_TYPES_AND_RAIL_DISTANCE = "areaTypes.distToRailStop";

    public static final String AUTONOMOUS_VEHICLE_CHOICE = "include.AVchoice";

    public static final String CREATE_CHARTS = "charts";
    public static final String PRINT_MICRO_DATA = "micro.data";
    public static final String FILL_MICRO_DATA_WITH_MICROLOCATION = "micro.data.with.microlocation";

    public static final String RUN_TIME_OF_DAY_CHOICE = "run.time.of.day.choice";
    public static final String TIME_OF_DAY_DISTRIBUTIONS = "time.of.day.distribution.file";

    public static final String RUN_TRIP_SCALING = "run.trip.scaling";
    public static final String TRIP_SCALING_FACTOR = "trip.scaling.factor";

    public static final String RUN_TRAFFIC_ASSIGNMENT = "run.traffic.assignment";
    public static final String MATSIM_NETWORK_FILE = "matsim.network";
    public static final String MATSIM_ITERATIONS = "matsim.iterations";
    public static final String ZONE_SHAPEFILE_ID_FIELD = "zone.shapefile.id.field";
    public static final String DEFAULT_BUDGET = "default.budget.";

    public static final String PRINT_OUT_SKIM = "print.skim";
    public static final String SKIM_FILE_NAME = "skim.file.name";

    public static final String ADD_EXTERNAL_FLOWS = "add.external.flows";
    public static final String EXTERNAL_DEPARTURE_TIME_FILE = "external.departure.time.file";
    public static final String EXTERNAL_ZONES_LIST_FILE = "external.zones.list";
    public static final String EXTERNAL_ZONES_SHAPEFILE = "external.zones.shp";
    public static final String EXTERNAL_ZONES_SHAPE_ID_FIELD = "external.zones.shp.id.field";
    public static final String EXTERNAL_MATRIX_PKW = "external.matrix.pkw";
    public static final String EXTERNAL_MATRIX_GV = "external.matrix.gv";
    public static final String EXTERNAL_MATRIX_PKW_PWV = "external.matrix.pkw_pwv";
    public static final String EXTERNAL_MATRIX_SZM = "external.matrix.szm";
    public static final String EXTERNAL_BASE_YEAR = "external.base.year";
    public static final String EXTERNAL_GROWTH_RATE = "external.growth.rate";

    public static final String AIRPORT_ZONE = "airport.zone";
    public static final String ADD_AIRPORT_DEMAND = "add.airport.demand";

    public static final String AIRPORT_Y = "airport.y";
    public static final String AIRPORT_X = "airport.x";
    public static final String MATSIM_NETWORK_MODES = "matsim.network.modes";
    public static final String MATSIM_TELEPORTED_MODES = "matsim.teleported.modes";

    public static final String MC_CALIBRATON_CONSTANTS_FILE = "mc.calibration.constants.file";
    public static final String MC_CALIBRATON_REGIONS_FILE = "mc.calibration.regions.file";
    public static final String MC_CALIBRATION_ITERATIONS = "mc.calibration.iterations";
}
