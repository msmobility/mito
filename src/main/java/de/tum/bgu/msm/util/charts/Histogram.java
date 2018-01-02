package de.tum.bgu.msm.util.charts;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class Histogram {

    private final static Logger logger = Logger.getLogger(Histogram.class);

    private Histogram() {

    }

    public static void createFrequencyHistogram(String path, double[] values, String title, String xAxisLabel, String yAxisLabel, int bins, int minXValue, int maxXValue) {
        logger.info("Creating histogram \"" + title + "\"...");
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.FREQUENCY);
        double sum = 0;
        for(double value: values) {
            sum += value;
        }
        double average = sum / values.length;
        dataset.addSeries("Frequency", values, bins, minXValue, maxXValue);
        JFreeChart chart = ChartFactory.createHistogram(title + " | Average: " + average, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setSeriesPaint(0, Color.blue);
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setRange(minXValue, maxXValue);
        domain.setTickUnit(new NumberTickUnit(1.));
        domain.setVerticalTickLabels(true);
        renderer.setDrawBarOutline(true);
        renderer.setSeriesOutlinePaint(0, Color.white);
        BufferedImage chartImage = chart.createBufferedImage(1600,800);
        logger.info("Writing histogram \"" + title + "\" to " + path);
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
}
