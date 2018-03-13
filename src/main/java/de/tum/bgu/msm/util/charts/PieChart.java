package de.tum.bgu.msm.util.charts;

import com.google.common.collect.Multiset;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public final class PieChart {
    private final static Logger logger = Logger.getLogger(PieChart.class);

    private PieChart() {

    }

    public static <T> void createPieChart(String path, Multiset<T> values, String title) {
        logger.info("Creating scatter plot \"" + title + "\"...");

        DefaultPieDataset dataset = createDataSet(values);
        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true,
                true,
                false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSimpleLabels(true);

        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", new DecimalFormat("0"), new DecimalFormat("0%"));
        plot.setLabelGenerator(gen);
        logger.info("Writing Pie Chart\"" + title + "\" to " + path);
        File outputFile = new File(path + ".png");
        if(outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }
        try {
            ChartUtilities.saveChartAsJPEG(outputFile, chart, 1600, 800);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <T> DefaultPieDataset createDataSet(Multiset<T> values) {
        DefaultPieDataset dataSet = new DefaultPieDataset();
        for(Multiset.Entry<T> entry: values.entrySet()) {
            dataSet.setValue(entry.getElement().toString(), entry.getCount());
        }
        return dataSet;
    }

}
