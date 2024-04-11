package de.tum.bgu.msm.resources;

import de.tum.bgu.msm.data.Purpose;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import static de.tum.bgu.msm.resources.Properties.*;

/**
 * Created by Nico on 19.07.2017.
 */
public class Resources {

    //TODO: provide defaults.

    public static Resources instance;

    private final Properties properties;

    private final Path baseDirectory;


    private Resources(Properties properties, String baseDirectory) {
        this.properties = properties;
        this.baseDirectory = Paths.get(baseDirectory).getParent();
    }

    public static void initializeResources(String fileName) {
        try (FileInputStream in = new FileInputStream(fileName)) {
            Properties properties = new Properties();
            properties.load(in);
            instance = new Resources(properties, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Path getZonesInputFile() {
        return baseDirectory.resolve(getString(ZONES));
    }

    public Path getZoneShapesInputFile() {
        return baseDirectory.resolve(getString(ZONE_SHAPEFILE));
    }

    public Path getBaseDirectory() {
        return baseDirectory.toAbsolutePath();
    }

    public Path getBorderReductionDamperFilePath() {
        return baseDirectory.resolve(getString(REDUCTION_NEAR_BORDER_DAMPERS));
    }

    public Path getJobsFilePath() {
        return baseDirectory.resolve(getString(JOBS));
    }

    public Path getSchoolsFilePath() {
        return baseDirectory.resolve(getString(SCHOOLS));
    }

    public Path getHouseholdsFilePath() {
        return baseDirectory.resolve(getString(HOUSEHOLDS));
    }

    public Path getPersonsFilePath() {
        return baseDirectory.resolve(getString(PERSONS));
    }

    public Path getDwellingsFilePath() {
        return baseDirectory.resolve(getString(DWELLINGS));
    }

    public Path getEconomicStatusFilePath() {
        return baseDirectory.resolve(getString(ECONOMIC_STATUS));
    }

    public synchronized int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public synchronized int getInt(String key, int defaultValue) {
        if (properties.containsKey(key)) {
            return Integer.parseInt(properties.getProperty(key));
        } else {
            return defaultValue;
        }
    }

    public synchronized String getString(String key) {
        return properties.getProperty(key);
    }

    public synchronized String[] getArray(String key) {
        return properties.getProperty(key).split(",");
    }

    public synchronized String[] getArray(String key, String[] defaultValue) {
        if (properties.containsKey(key)) {
            return properties.getProperty(key).split(",");
        } else {
            return defaultValue;
        }
    }

    public synchronized boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public synchronized boolean getBoolean(String key, boolean defaultValue) {
        if (properties.containsKey(key)) {
            return Boolean.parseBoolean(properties.getProperty(key));
        } else {
            return defaultValue;
        }
    }

    public synchronized double getDouble(String key, double defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Double.parseDouble(value) : defaultValue;
    }

    public Path getAreaTypesAndRailDistancesFilePath() {
        return baseDirectory.resolve(getString((AREA_TYPES_AND_RAIL_DISTANCE)));
    }

    public Path getExternalDepartureTimeFilePath() {
        return baseDirectory.resolve(EXTERNAL_DEPARTURE_TIME_FILE);
    }

    public Path getTimeOfDayDistributionsFilePath() {
        return baseDirectory.resolve(getString(TIME_OF_DAY_DISTRIBUTIONS));
    }

    public Path getTripAttractionRatesFilePath() {
        return baseDirectory.resolve(getString(TRIP_ATTRACTION_RATES));
    }

    public Path getOutputHouseholdPath() {
        String scenarioName = Resources.instance.getString(SCENARIO_NAME);
        return baseDirectory.resolve("scenOutput").resolve(scenarioName).resolve("microData").resolve("hhInclTrips.csv");
    }

    public Path getOutputPersonsPath() {
        String scenarioName = Resources.instance.getString(SCENARIO_NAME);
        return baseDirectory.resolve("scenOutput").resolve(scenarioName).resolve("microData").resolve("ppInclTrips.csv");
    }

    public Path getExternalZonesListFilePath() {
        return baseDirectory.resolve(getString(EXTERNAL_ZONES_LIST_FILE));
    }

    public Path getRelativePath(String property) {
        return baseDirectory.resolve(getString(property));
    }

    public Path getTripFrequenciesFilePath(Purpose purpose) {
        return baseDirectory.resolve(Resources.instance.getString(purpose + ".trip.frequencies"));
    }

    public Path getCalibrationFactorsPath() {
        return baseDirectory.resolve(Resources.instance.getString(MC_CALIBRATON_CONSTANTS_FILE));
    }

    public Path getCalibrationRegionsPath() {
        return baseDirectory.resolve(Resources.instance.getString(MC_CALIBRATON_REGIONS_FILE));
    }

    public Path getTripGenerationCoefficientsHurdleBinaryLogit() {
        return baseDirectory.resolve(Resources.instance.getString(TG_BINARY_LOGIT_COEFFICIENTS));
    }

    public Path getTripGenerationCoefficientsHurdleNegativeBinomial() {
        return baseDirectory.resolve(Resources.instance.getString(TG_NEGATIVE_BINOMIAL_COEFFICIENTS));
    }

    public Path getTripGenerationCoefficientsHurdleOrderedLogit() {
        return baseDirectory.resolve(Resources.instance.getString(TG_ORDERED_LOGIT_COEFFICIENTS));
    }

    public Path getModeChoiceCoefficients(Purpose purpose) {
        return baseDirectory.resolve(Resources.instance.getString(MC_COEFFICIENTS) +
                "_" +
                purpose.toString().toLowerCase() +
                ".csv");
    }

    public Path getBicycleOwnershipInputFile() {
        return baseDirectory.resolve(Resources.instance.getString(BIKE_OWNERSHIP_COEFFICIENTS));
    }

    public Path getModeSetCoefficients() {
        return baseDirectory.resolve(Resources.instance.getString(MODE_SET_COEFFICIENTS));
    }

    public Path getModeSetConstants() {
        return baseDirectory.resolve(Resources.instance.getString(MODE_SET_CONSTANTS));
    }

    public Path getDayProbabilitiesFilePath() {
        return baseDirectory.resolve(Resources.instance.getString(DAY_OF_WEEK_PROBABILITIES));
    }

}
