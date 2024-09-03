package routing.travelTime;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.bicycle.BicycleLinkSpeedCalculator;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

/**
 * @author dziemke
 */
public class BicycleTravelTimeFast implements TravelTime {

    private final Network network;
    private final Vehicle vehicle;
    private final double[] travelTimes = new double[Id.getNumberOfIds(Link.class)];

    @Inject
    private final BicycleLinkSpeedCalculator linkSpeedCalculator;
    private final WalkTravelTime dismountTravelTime = new WalkTravelTime();

    @Inject
    public BicycleTravelTimeFast(BicycleLinkSpeedCalculator calculator, Network network, Vehicle vehicle) {
        this.network = network;
        this.vehicle = vehicle;
        this.linkSpeedCalculator = calculator;
        precalculateTravelTimes();
    }

    private void precalculateTravelTimes() {
        for(Link link : network.getLinks().values()) {
            travelTimes[link.getId().index()] = calculateTravelTime(link);
        }
    }

    private double calculateTravelTime(Link link) {
        if((boolean) link.getAttributes().getAttribute("dismount")) {
            return dismountTravelTime.getLinkTravelTime(link,0.,null,null);
        }
        return link.getLength() / linkSpeedCalculator.getMaximumVelocityForLink(link, vehicle);
    }

    @Override
    public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
        return travelTimes[link.getId().index()];
    }
}
