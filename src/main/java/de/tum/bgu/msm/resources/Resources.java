package de.tum.bgu.msm.resources;

import com.pb.common.calculator.UtilityExpressionCalculator;
import com.pb.common.util.ResourceUtil;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Created by Nico on 19.07.2017.
 */
public enum Resources {

    INSTANCE;

    private ResourceBundle resources;

    Resources() {

    }

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    public UtilityExpressionCalculator getUtilityExpressionCalculator(String fileName, int totalTtbSheetNumber,
                                                                      int dataSheetNumber, Class<?> userClass) {
        return new UtilityExpressionCalculator(new File(fileName), totalTtbSheetNumber,
                dataSheetNumber, resources, userClass);
    }

    public int getInt(String key) {
        return ResourceUtil.getIntegerProperty(resources, key);
    }

    public String getString(String key) {
        return ResourceUtil.getProperty(resources, key);
    }

    public String[] getArray(String key) {
        return ResourceUtil.getArray(resources, key);
    }

    public boolean getBoolean(String key) {
        return ResourceUtil.getBooleanProperty(resources, key);
    }

    public double getDouble(String key) {
        return ResourceUtil.getDoubleProperty(resources, key);
    }
}
