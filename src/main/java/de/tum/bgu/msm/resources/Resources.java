package de.tum.bgu.msm.resources;

import com.pb.common.calculator.UtilityExpressionCalculator;
import de.tum.bgu.msm.modules.TravelTimeBudgetDMU;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Created by Nico on 19.07.2017.
 */
public enum Resources {

    INSTANCE;

    private ResourceBundle resources;


    private Resources() {

    }

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    public UtilityExpressionCalculator getUtilityExpressionCalculator(String fileName, int totalTtbSheetNumber,
    int dataSheetNumber, Class<?> userClass) {
        return new UtilityExpressionCalculator(new File(fileName), totalTtbSheetNumber,
                dataSheetNumber, resources, userClass);
    }




}
