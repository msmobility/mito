package de.tum.bgu.msm.io.output;

import com.google.common.math.DoubleMath;
import com.google.common.math.Stats;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Mode;
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
import java.util.Collections;
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

        List<Chart> individualChartsByPurpose = new ArrayList<>();
        List<Chart> individualChartsByMode = new ArrayList<>();

        // Create Chart
        CategoryChart stackedChartByPurpose = new CategoryChartBuilder().width(1600).height(900).title("Trip Length Frequency Distributions")
                .xAxisTitle("Trip Length").yAxisTitle("Frequency").theme(Styler.ChartTheme.GGPlot2).build();


        // Customize Chart
        stackedChartByPurpose.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        stackedChartByPurpose.getStyler().setAvailableSpaceFill(1);
        stackedChartByPurpose.getStyler().setStacked(true);
        stackedChartByPurpose.getStyler().setXAxisLabelRotation(90);

        final Map<Purpose, List<MitoTrip>> tripsByPurpose = dataSet.getTrips().values().stream()
                .filter(trip -> trip.getTripOrigin() != null && trip.getTripDestination() != null)
                .collect(Collectors.groupingBy(MitoTrip::getTripPurpose));

        for(Purpose purpose: Purpose.values()) {
            if(tripsByPurpose.containsKey(purpose)) {

                // Create Chart
                CategoryChart individualChart = new CategoryChartBuilder().width(800).height(600).xAxisTitle("Trip Length").yAxisTitle("Frequency").theme(Styler.ChartTheme.GGPlot2).build();

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
                Histogram histogram = new Histogram(distances, 50, 0, 100);
                stackedChartByPurpose.addSeries(purpose.name(), histogram.getxAxisData(), histogram.getyAxisData());
                individualChart.addSeries(purpose.name(), histogram.getxAxisData(), histogram.getyAxisData());
                double avg = Stats.meanOf(distances);
                individualChart.setTitle("Trip Length Frequency Distribution - " + purpose.name() + " - Avg: " + avg);
                individualChartsByPurpose.add(individualChart);
            }
        }

        // Create Chart
        CategoryChart stackedChartByMode = new CategoryChartBuilder().width(1600).height(900).title("Trip Length Frequency Distributions")
                .xAxisTitle("Trip Length").yAxisTitle("Frequency").theme(Styler.ChartTheme.GGPlot2).build();


        // Customize Chart
        stackedChartByMode.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        stackedChartByMode.getStyler().setAvailableSpaceFill(1);
        stackedChartByMode.getStyler().setStacked(true);
        stackedChartByMode.getStyler().setXAxisLabelRotation(90);

        final Map<Mode, List<MitoTrip>> tripsByMode = dataSet.getTrips().values().stream()
                .filter(trip -> trip.getTripOrigin() != null && trip.getTripDestination() != null)
                .collect(Collectors.groupingBy(MitoTrip::getTripMode));

        for(Mode mode: Mode.values()) {
            if(tripsByMode.containsKey(mode)) {

                // Create Chart
                CategoryChart individualChart = new CategoryChartBuilder().width(800).height(600).xAxisTitle("Trip Length").yAxisTitle("Frequency").theme(Styler.ChartTheme.GGPlot2).build();

                // Customize Chart
                individualChart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
                individualChart.getStyler().setAvailableSpaceFill(1);
                individualChart.getStyler().setXAxisLabelRotation(90);

                List<Double> distances = new ArrayList<>();
                for (MitoTrip t : tripsByMode.get(mode)) {
                    double travelDistance = dataSet.getTravelDistancesAuto()
                            .getTravelDistance(t.getTripOrigin().getZoneId(), t.getTripDestination().getZoneId());
                    distances.add(travelDistance);
                }
                Histogram histogram = new Histogram(distances, 50, 0, 100);
                stackedChartByMode.addSeries(mode.name(), histogram.getxAxisData(), histogram.getyAxisData());

                individualChart.addSeries(mode.name(), histogram.getxAxisData(), histogram.getyAxisData());
                double avg = Stats.meanOf(distances);
                individualChart.setTitle("Trip Length Frequency Distribution - " + mode.name() + " - Avg: " + avg);
                individualChartsByMode.add(individualChart);
            }
        }

        try {
            BitmapEncoder.saveBitmap(stackedChartByPurpose, directory + "/tripLengthsStackedByPurpose", BitmapEncoder.BitmapFormat.PNG);
            BitmapEncoder.saveBitmap(stackedChartByMode, directory + "/tripLengthsStackedByMode", BitmapEncoder.BitmapFormat.PNG);

            Histogram fakeHistogram = new Histogram(new ArrayList<>(Collections.nCopies(50, 0)), 50, 0, 100);
            List<Double> fakeXData = fakeHistogram.getxAxisData();
            List<Double> fakeYData = new ArrayList<>(Collections.nCopies(50, 0.));

            int numberOfRowsPurposes = (int) Math.ceil(individualChartsByPurpose.size() / 3.);
            int numberOfEmptyChartsPurposes = numberOfRowsPurposes*3-individualChartsByPurpose.size();
            for(int i = 0; i<numberOfEmptyChartsPurposes;i++){
                CategoryChart EmptyChart = new CategoryChartBuilder().width(800).height(600).xAxisTitle("Trip Length").yAxisTitle("Frequency").theme(Styler.ChartTheme.GGPlot2).build();
                EmptyChart.addSeries("NA",fakeXData, fakeYData);
                EmptyChart.setTitle("Empty Chart");
                individualChartsByPurpose.add(EmptyChart);
            }
            BitmapEncoder.saveBitmap(individualChartsByPurpose, numberOfRowsPurposes, 3, directory + "/tripLengthsIndividualByPurpose", BitmapEncoder.BitmapFormat.PNG);

            int numberOfRowsModes = (int) Math.ceil(individualChartsByMode.size() / 3.);
            int numberOfEmptyChartsModes = numberOfRowsModes*3-individualChartsByMode.size();
            for(int i = 0; i<numberOfEmptyChartsModes;i++){
                CategoryChart EmptyChart = new CategoryChartBuilder().width(800).height(600).xAxisTitle("Trip Length").yAxisTitle("Frequency").theme(Styler.ChartTheme.GGPlot2).build();
                EmptyChart.addSeries("NA",fakeXData, fakeYData);
                EmptyChart.setTitle("Empty Chart");
                individualChartsByMode.add(EmptyChart);
            }
            BitmapEncoder.saveBitmap(individualChartsByMode,numberOfRowsModes,3, directory + "/tripLengthsIndividualByMode", BitmapEncoder.BitmapFormat.PNG);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
