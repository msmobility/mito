package de.tum.bgu.msm.io.output;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ModeChoicePlots {

    private final static Logger logger = Logger.getLogger(ModeChoicePlots.class);


    public static void writeModeChoice(DataSet dataSet, String scenarioName) {
        final String directory = Resources.instance.getBaseDirectory().toString()
                + "/scenOutput/" + scenarioName + "/" + dataSet.getYear() + "/modeChoice/";
        new File(directory).mkdirs();
        logger.info("Writing mode choice plots to " + directory);
        List<Chart> charts = new ArrayList<>();
        for(Purpose purpose: Purpose.values()) {
            // Create Chart
            org.knowm.xchart.PieChart chart = new PieChartBuilder().width(800).height(600).title(purpose.name()).theme(Styler.ChartTheme.GGPlot2).build();

            chart.getStyler().setAnnotationType(PieStyler.AnnotationType.Percentage);
            chart.getStyler().setDrawAllAnnotations(true);
            chart.getStyler().setPlotContentSize(0.75);
            chart.getStyler().setAnnotationDistance(1.2);
            // Customize Chart

            for (Mode mode : Mode.values()) {
                Double share = dataSet.getModeShareForPurpose(purpose, mode);
                if (share != null) {
                    logger.info("Mode " + mode + ": " + share + " share");
                    // Series
                    chart.addSeries(mode.name(), (int) (dataSet.getModeShareForPurpose(purpose, mode) * 100));
                }
            }
            charts.add(chart);
        }

        try {
            BitmapEncoder.saveBitmap(charts,(int) (charts.size()/3),3, directory + "/modeChoice", BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
