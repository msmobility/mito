package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class TravelTimeBudgetJSCalculatorTest {

    private final int NUMBER_OF_CALCULATED_OBJECTS = 100000;

    private TravelTimeBudgetJSCalculator jsCalculator;

    private List<MitoHousehold> households = new ArrayList();

    @Before
    public void setup() {
        setupMitoData();
        ResourceBundle bundle = MitoUtil.createResourceBundle("./testInput/test.properties");
        Resources.INSTANCE.setResources(bundle);
    }

    private void setupJSCalculator() throws ScriptException, FileNotFoundException {
        Reader reader = new FileReader(Resources.INSTANCE.getString(Properties.TRAVEL_TIME_BUDGET_JS));
        jsCalculator = new TravelTimeBudgetJSCalculator(reader, "Total");
    }

    private void setupMitoData() {
        Zone zone = new Zone(1);
        zone.setRegion(1);
        MitoHousehold emptyHousehold = new MitoHousehold(1, 10000, 0, zone);
        for (int i = 0; i < NUMBER_OF_CALCULATED_OBJECTS; i++) {
            households.add(emptyHousehold);
        }
    }

    @Test
    public void testAndMeasureTimeJS() {
        long time = System.currentTimeMillis();
        try {
            setupJSCalculator();
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (MitoHousehold household : households) {
            jsCalculator.setPurpose("Total");
            jsCalculator.bindHousehold(household);
            assertEquals(50.121, jsCalculator.calculate(false), 0.001);
            jsCalculator.setPurpose(Purpose.HBO.name());
            assertEquals(30.005, jsCalculator.calculate(false), 0.001);
            jsCalculator.setPurpose(Purpose.HBS.name());
            assertEquals(16.586, jsCalculator.calculate(false), 0.001);
            jsCalculator.setPurpose(Purpose.NHBW.name());
            assertEquals(15.481, jsCalculator.calculate(false), 0.001);
            jsCalculator.setPurpose(Purpose.NHBO.name());
            assertEquals(17.881, jsCalculator.calculate(false), 0.001);
        }
        System.out.println("time: " + (System.currentTimeMillis() - time));
    }

//    @Test
//    public void testAndMeasureTimeUEC() {
//        long time2 = System.currentTimeMillis();
//        setupUECCalculator();
//        for (MitoHousehold household : households) {
//            assertEquals(50.121, uecCalculatorTot.calculate(false, household), 0.001);
//            assertEquals(30.005, uecCalculatorHBO.calculate(false, household), 0.001);
//            assertEquals(16.586, uecCalculatorHBS.calculate(false, household), 0.001);
//            assertEquals(15.481, uecCalculatorNHBW.calculate(false, household), 0.001);
//            assertEquals(17.881, uecCalculatorNHBO.calculate(false, household), 0.001);
//        }
//        System.out.println("time: " + (System.currentTimeMillis() - time2));
//    }
}
