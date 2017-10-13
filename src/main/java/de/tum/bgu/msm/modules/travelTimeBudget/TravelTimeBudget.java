package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Occupation;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.EnumSet;

/**
 * Runs calculation of travel time budget for the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Apr 2, 2017 in Mannheim, Germany
 */

public class TravelTimeBudget extends Module {

    private static final Logger logger = Logger.getLogger(TravelTimeBudget.class);

    private int ignoredStudents = 0;
    private int ignoredWorkers = 0;

    private TravelTimeBudgetJSCalculator travelTimeCalc;

    private EnumSet<Purpose> discretionaryPurposes = EnumSet.of(Purpose.HBS, Purpose.HBO, Purpose.NHBW, Purpose.NHBO);

    public TravelTimeBudget(DataSet dataSet) {
        super(dataSet);
        setupTravelTimeBudgetModel();
    }

    @Override
    public void run() {
        calculateTravelTimeBudget();
    }


    private void setupTravelTimeBudgetModel() {
        logger.info("  Creating Utility Expression Calculators for microscopic travel time budget calculation.");
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TravelTimeBudgetCalc"));
        travelTimeCalc = new TravelTimeBudgetJSCalculator(reader, "Total");
    }

    private void calculateTravelTimeBudget() {
        // main method to calculate the travel time budget for every household
        logger.info("  Started microscopic travel time budget calculation.");
        // loop over every household and calculate travel time budget by purpose
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            travelTimeCalc.setPurpose("Total");
            travelTimeCalc.bindHousehold(household);
            double totalTravelTimeBudget = travelTimeCalc.calculate();
            calculateDiscretionaryPurposeBudgets(household);
            calculateHBWBudgets(household);
            calculateHBEBudgets(household);
            adjustDiscretionaryPurposeBudget(household, totalTravelTimeBudget);
        }
        logger.info("  Finished microscopic travel time budget calculation.");
        if (ignoredStudents > 0 || ignoredWorkers > 0) {
            logger.warn("There have been " + ignoredWorkers + " workers and " + ignoredStudents
                    + " students that were ignored in the HBW/HBE travel time budgets"
                    + " because they had no workzone assigned.");
        }
    }

    private void calculateHBWBudgets(MitoHousehold household) {
        double hbwBudget = 0;
        for (MitoPerson person : household.getPersons().values()) {
            if (person.getOccupation().equals(Occupation.WORKER)) {
                if (person.getWorkzone() == null) {
                    logger.debug("Worker with workzone null will not be considered for travel time budget.");
                    ignoredWorkers++;
                    continue;
                }
                hbwBudget += dataSet.getTravelTimes("car").getTravelTimeFromTo(household.getHomeZone().getZoneId(), person.getWorkzone().getZoneId());
            }
        }
        household.setTravelTimeBudgetByPurpose(Purpose.HBW, hbwBudget);
    }

    private void calculateHBEBudgets(MitoHousehold household) {
        double hbeBudget = 0;
        for (MitoPerson person : household.getPersons().values()) {
            if (person.getOccupation().equals(Occupation.STUDENT)) {
                if (person.getWorkzone() == null) {
                    logger.debug("Student with workzone null will not be considered for travel time budget.");
                    ignoredStudents++;
                    continue;
                }
                hbeBudget += dataSet.getTravelTimes("pt").getTravelTimeFromTo(household.getHomeZone().getZoneId(), person.getWorkzone().getZoneId());
            }
        }
        household.setTravelTimeBudgetByPurpose(Purpose.HBE, hbeBudget);
    }

    private void calculateDiscretionaryPurposeBudgets(MitoHousehold household) {
        travelTimeCalc.setPurpose(Purpose.HBS.name());
        travelTimeCalc.bindHousehold(household);
        household.setTravelTimeBudgetByPurpose(Purpose.HBS, travelTimeCalc.calculate());
        travelTimeCalc.setPurpose(Purpose.HBO.name());
        household.setTravelTimeBudgetByPurpose(Purpose.HBO, travelTimeCalc.calculate());
        travelTimeCalc.setPurpose(Purpose.NHBW.name());
        household.setTravelTimeBudgetByPurpose(Purpose.NHBW, travelTimeCalc.calculate());
        travelTimeCalc.setPurpose(Purpose.NHBO.name());
        household.setTravelTimeBudgetByPurpose(Purpose.NHBO, travelTimeCalc.calculate());
    }


    private void adjustDiscretionaryPurposeBudget(MitoHousehold household, double totalTravelTimeBudget) {
        double discretionaryTTB = totalTravelTimeBudget - household.getTravelTimeBudgetForPurpose(Purpose.HBW) -
                household.getTravelTimeBudgetForPurpose(Purpose.HBE);

        discretionaryTTB = Math.max(discretionaryTTB, 0);

        double calcDiscretionaryTTB = 0;
        for (Purpose purpose : discretionaryPurposes) {
            calcDiscretionaryTTB += household.getTravelTimeBudgetForPurpose(purpose);
        }
        for (Purpose purpose : discretionaryPurposes) {
            household.setTravelTimeBudgetByPurpose(purpose, household.getTravelTimeBudgetForPurpose(purpose) * discretionaryTTB / calcDiscretionaryTTB);
        }
    }
}
