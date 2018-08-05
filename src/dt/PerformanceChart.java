package dt;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * PerformanceChart uses the JFreeChart Java library to plot time vs. # vertices for the Delaunay triangulation
 * @author Lee Glendenning
 */
public class PerformanceChart extends JFrame {
    
    private final ArrayList<int[]> data;
    private final JFreeChart chart;
    
    /**
     * 
     * @param title Title of the JFrame window
     * @param data Data set to plot
     */
    public PerformanceChart(String title, ArrayList<int[]> data) {
        this.data = data;
        
        // Create dataset
        XYDataset dataset = setData();

        // Create chart
        this.chart = ChartFactory.createScatterPlot(
            "Delaunay Triangulation Performance", 
            "# Vertices", "Milliseconds", dataset);

        //Changes background color
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(new Color(255,228,196));

        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    /**
     * 
     * @return XYDataset object used in the chart
     */
    private XYDataset setData() {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries series1 = new XYSeries("B2S");
        XYSeries series2 = new XYSeries("B3S");
        XYSeries series3 = new XYSeries("Triangulation");
        XYSeries series4 = new XYSeries("Shortest Path");

        for (int i = 0; i < this.data.size(); i ++) {
            series1.add(i, this.data.get(i)[0]);
            series2.add(i, this.data.get(i)[1]);
            series3.add(i, this.data.get(i)[2]);
            series4.add(i, this.data.get(i)[3]);
        }

        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);
        dataset.addSeries(series4);

        return dataset;
    }

}