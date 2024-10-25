package routing.travelTime;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.bicycle.BicycleLinkSpeedCalculator;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;
import routing.components.LinkStressDiscrete;

import static routing.components.LinkStressDiscrete.*;

/**
 * @author dziemke
 */
public class BicycleTravelTime implements TravelTime {

    private static final Logger logger = Logger.getLogger(BicycleTravelTime.class);

    @Inject
    private BicycleLinkSpeedCalculator linkSpeedCalculator;
    private final WalkTravelTime dismountTravelTime = new WalkTravelTime();
    private LinkStressDiscrete stressThreshold = null;

    @Inject
    private BicycleTravelTime() {}

    public BicycleTravelTime(BicycleLinkSpeedCalculator linkSpeedCalculator) {
        this.linkSpeedCalculator =  linkSpeedCalculator;
    }

    public void setLinkStressThreshold(String threshold) {
        this.stressThreshold = LinkStressDiscrete.valueOf(threshold.toUpperCase());
        logger.info("Set bicycle stress threshold to " + this.stressThreshold);
    }

    @Override
    public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {

        if((boolean) link.getAttributes().getAttribute("dismount")) {
            return dismountTravelTime.getLinkTravelTime(link,time,person,vehicle);
        }

        if(stressThreshold != null) {
            LinkStressDiscrete stress = LinkStressDiscrete.getCycleStress(link);
            assert stress != null;
            if((AMBER.equals(stressThreshold) && stress.equals(RED)) ||
                    (GREEN.equals(stressThreshold) && stress.equals(RED)) ||
                    (GREEN.equals(stressThreshold) && stress.equals(AMBER))) {
                return dismountTravelTime.getLinkTravelTime(link,time,person,vehicle);
            }
        }

        return link.getLength() / linkSpeedCalculator.getMaximumVelocityForLink(link, vehicle);
    }
}
