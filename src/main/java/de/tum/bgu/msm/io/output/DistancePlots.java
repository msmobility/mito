package de.tum.bgu.msm.io.output;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.Histogram;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DistancePlots {

    private final static Logger logger = Logger.getLogger(DistancePlots.class);

    public static void writeDistanceDistributions(DataSet dataSet, String scenarioName) {

        final String directory = Resources.instance.getBaseDirectory().toString()
                + "/scenOutput/" + scenarioName + "/" + dataSet.getYear() + "/tripLengths/";
        new File(directory).mkdirs();
        logger.info("Writing trip length plots to " + directory);

        List<Chart> individualCharts = new ArrayList<>();

        // Create Chart
        CategoryChart stackedChart = new CategoryChartBuilder().width(1600).height(900).title("Trip Length Frequency Distributions")
                .xAxisTitle("Trip Length").yAxisTitle("Frequency").theme(Styler.ChartTheme.GGPlot2).build();


        // Customize Chart
        stackedChart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        stackedChart.getStyler().setAvailableSpaceFill(1);
        stackedChart.getStyler().setStacked(true);
        stackedChart.getStyler().setXAxisLabelRotation(90);

        final Map<Purpose, List<MitoTrip>> tripsByPurpose = dataSet.getTrips().values().stream()
                .filter(trip -> trip.getTripOrigin() != null && trip.getTripDestination() != null)
                .collect(Collectors.groupingBy(MitoTrip::getTripPurpose));

        for(Purpose purpose: Purpose.values()) {
            if(tripsByPurpose.containsKey(purpose)) {

                // Create Chart
                CategoryChart individualChart = new CategoryChartBuilder().width(800).height(600).title("Trip Length Frequency Distribution - " + purpose.name())
                        .xAxisTitle("Trip Length").yAxisTitle("Frequency").theme(Styler.ChartTheme.GGPlot2).build();

                // Customize Chart
                individualChart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
                individualChart.getStyler().setAvailableSpaceFill(1);
                individualChart.getStyler().setXAxisLabelRotation(90);

                List<Double> distances = new ArrayList<>();
                for (MitoTrip t : tripsByPurpose.get(purpose)) {
                    double travelDistance = dataSet.getTravelDistancesAuto()
                            .getTravelDistance(t.getTripOrigin().getZoneId(), t.getTripDestination().getZoneId());
                    distances.add(travelDistance);
                }
                Histogram histogram = new Histogram(distances, 50, 0, 50);
                stackedChart.addSeries(purpose.name(), histogram.getxAxisData(), histogram.getyAxisData());
                individualChart.addSeries(purpose.name(), histogram.getxAxisData(), histogram.getyAxisData());
                individualCharts.add(individualChart);
            }
        }

        try {
            BitmapEncoder.saveBitmap(stackedChart, directory + "/tripLengthsStacked", BitmapEncoder.BitmapFormat.PNG);
            BitmapEncoder.saveBitmap(individualCharts,(int) (individualCharts.size()/3),3, directory + "/tripLengthsIndividual", BitmapEncoder.BitmapFormat.PNG);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
