package de.tum.bgu.msm.resources;

import com.pb.common.calculator2.UtilityExpressionCalculator;
import com.pb.common.calculator2.VariableTable;
import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.modules.personTripAssignment.DefaultTripAssignmentFactory;
import de.tum.bgu.msm.modules.personTripAssignment.TripAssignmentFactory;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Created by Nico on 19.07.2017.
 */
public enum Resources {

    INSTANCE;

    private ResourceBundle resources;
    private TripAssignmentFactory tripAssignmentFactory = new DefaultTripAssignmentFactory();

    Resources() {

    }

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    public synchronized int getInt(String key) {
        return ResourceUtil.getIntegerProperty(resources, key);
    }

    public synchronized String getString(String key) {
        return ResourceUtil.getProperty(resources, key);
    }

    public synchronized String[] getArray(String key) {
        return ResourceUtil.getArray(resources, key);
    }

    public synchronized  boolean getBoolean(String key) {
        return ResourceUtil.getBooleanProperty(resources, key);
    }

    public synchronized double getDouble(String key) {
        return ResourceUtil.getDoubleProperty(resources, key);
    }

    public TripAssignmentFactory getTripAssignmentFactory() {
        return tripAssignmentFactory;
    }

    public void setTripAssignmentFactory(TripAssignmentFactory tripAssignmentFactory) {
        this.tripAssignmentFactory = tripAssignmentFactory;
    }
}
