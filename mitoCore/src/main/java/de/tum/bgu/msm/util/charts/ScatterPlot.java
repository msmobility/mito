package de.tum.bgu.msm.util.charts;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public final class ScatterPlot {

    private final static Logger logger = Logger.getLogger(ScatterPlot.class);

    private ScatterPlot() {

    }

    public static void createScatterPlot(String path, Map<Double, Double> entries, String title, String xAxisLabel, String yAxisLabel) {
        logger.info("Creating scatter plot \"" + title + "\"...");

        XYDataset dataset = createDataSet(entries);
        JFreeChart chart = ChartFactory.createScatterPlot(
                title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL,
                true, false, false);


        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        BufferedImage chartImage = chart.createBufferedImage(1600,800);
        logger.info("Writing Scatter Plot \"" + title + "\" to " + path);
        File outputFile = new File(path + ".png");
        if(outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }
        try {
            ImageIO.write(chartImage, "png", new BufferedOutputStream(new FileOutputStream(outputFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static XYDataset createDataSet(Map<Double, Double> entries) {
        XYSeries series = new XYSeries("MitoZone");
        for(Map.Entry<Double, Double> entry: entries.entrySet()) {
            series.add(entry.getKey(), entry.getValue());
        }
        return new XYSeriesCollection(series);
    }
}
