package de.tum.bgu.msm.resources;

import com.pb.common.calculator2.UtilityExpressionCalculator;
import com.pb.common.calculator2.VariableTable;
import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.modules.personTripAssignment.SimpleTripDistributionFactory;
import de.tum.bgu.msm.modules.personTripAssignment.TripDistributionFactory;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Created by Nico on 19.07.2017.
 */
public enum Resources {

    INSTANCE;

    private ResourceBundle resources;
    private TripDistributionFactory tripDistributionFactory = new SimpleTripDistributionFactory();

    Resources() {

    }

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    public UtilityExpressionCalculator getUtilityExpressionCalculator2(String fileName, int totalTtbSheetNumber,
                                                                        int dataSheetNumber, VariableTable dmuObject) {
        return new UtilityExpressionCalculator(new File(fileName), totalTtbSheetNumber,
                dataSheetNumber, ResourceUtil.changeResourceBundleIntoHashMap(resources), dmuObject);

    }

    public com.pb.common.calculator.UtilityExpressionCalculator getUtilityExpressionCalculator1(String fileName, int totalTtbSheetNumber,
                                                                                                                            int dataSheetNumber, Class<?> userClass) {
        return new com.pb.common.calculator.UtilityExpressionCalculator(new File(fileName), totalTtbSheetNumber,
                dataSheetNumber, ResourceUtil.changeResourceBundleIntoHashMap(resources), userClass);

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

    public TripDistributionFactory getTripDistributionFactory() {
        return tripDistributionFactory;
    }

    public void setTripDistributionFactory(TripDistributionFactory tripDistributionFactory) {
        this.tripDistributionFactory = tripDistributionFactory;
    }
}
