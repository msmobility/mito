package de.tum.bgu.msm.resources;

import de.tum.bgu.msm.Implementation;
import de.tum.bgu.msm.modules.personTripAssignment.DefaultTripAssignmentFactory;
import de.tum.bgu.msm.modules.personTripAssignment.TripAssignmentFactory;
import de.tum.bgu.msm.util.MitoUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Nico on 19.07.2017.
 */
public class Resources {

    public static Resources INSTANCE;

    private final Properties properties;
    public final Implementation implementation;
    private TripAssignmentFactory tripAssignmentFactory = new DefaultTripAssignmentFactory();

    private Resources(Properties properties, Implementation implementation) {
        this.properties = properties;
        this.implementation = implementation;
    }

    public static void initializeResources(String fileName, Implementation implementation) throws IOException {
        FileInputStream in = new FileInputStream(fileName);
        Properties properties = new Properties();
        properties.load(in);
        MitoUtil.setBaseDirectory(properties.getProperty("base.directory"));
        INSTANCE = new Resources(properties, implementation);
    }

    public synchronized int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public synchronized String getString(String key) {
        return properties.getProperty(key);
    }

    public synchronized String[] getArray(String key) {
        return properties.getProperty(key).split(",");
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

    public TripAssignmentFactory getTripAssignmentFactory() {
        return tripAssignmentFactory;
    }

    public void setTripAssignmentFactory(TripAssignmentFactory tripAssignmentFactory) {
        this.tripAssignmentFactory = tripAssignmentFactory;
    }
}
