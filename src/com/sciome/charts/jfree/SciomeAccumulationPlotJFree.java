package com.sciome.charts.jfree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import com.sciome.charts.SciomeAccumulationPlot;
import com.sciome.charts.SciomeChartListener;
import com.sciome.charts.data.ChartConfiguration;
import com.sciome.charts.data.ChartDataPack;
import com.sciome.charts.model.SciomeSeries;

import javafx.scene.Node;
import javafx.scene.input.MouseButton;

public class SciomeAccumulationPlotJFree extends SciomeAccumulationPlot{

	public SciomeAccumulationPlotJFree(String title, List<ChartDataPack> chartDataPacks, String key, Double bucketsize,
			SciomeChartListener chartListener) {
		super(title, chartDataPacks, key, bucketsize, chartListener);
	}

	@Override
	protected Node generateChart(String[] keys, ChartConfiguration chartConfig) {
		String key1 = keys[0];
		String key2 = "Accumulation";
		Double min1 = getMinMin(key1);
		Double min2 = 0.0;
		Double max1 = 0.0;
		Double max2 = 0.0;

		DefaultXYDataset dataset = new DefaultXYDataset();
		
		for (SciomeSeries<Number, Number> series : getSeriesData())
		{
			double[] domains = new double[series.getData().size()];
			double[] ranges = new double[series.getData().size()];
			int i = 0;
			for (Object chartData : series.getData())
			{
				AccumulationData value = (AccumulationData)chartData;
				double domainvalue = value.getXValue().doubleValue();
				double rangevalue = value.getYValue().doubleValue();
				domains[i] = domainvalue;
				ranges[i++] = rangevalue;
			}
			dataset.addSeries(series.getName(), new double[][] { domains, ranges });
		}

		// Create chart
		JFreeChart chart = ChartFactory.createXYLineChart(key1 + "Accumulation Plot",
				key1, key2, dataset, PlotOrientation.VERTICAL, true, true, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setForegroundAlpha(0.1f);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
		plot.setDomainAxis(SciomeNumberAxisGeneratorJFree.generateAxis(getLogXAxis().isSelected(), key1));
		plot.setRangeAxis(SciomeNumberAxisGeneratorJFree.generateAxis(getLogYAxis().isSelected(), key2));
		
		//Only want to zoom in if we any values have been set in chartConfig
		if(chartConfig != null) {
			//Find the values for the min and max values for x and y
			if (chartConfig.getMaxX() != null && chartConfig.getMinX() != null)
			{
				max1 = chartConfig.getMaxX();
				min1 = chartConfig.getMinX();
			}
		
			if (chartConfig.getMaxY() != null && chartConfig.getMinY() != null)
			{
				max2 = chartConfig.getMaxY();
				min2 = chartConfig.getMinY();
			}
			if (min1.equals(max1))
			{
				min1 -= 1;
			}
			else if (min1 > max1)
			{
				min1 = 0.0;
				max1 = 0.1;
			}
			if (min2.equals(max2))
			{
				min2 -= 1;
			}
			else if (min2 > max2)
			{
				min2 = 0.0;
				max2 = 0.1;
			}
			
			// Set the domain and range based on these x and y values
			NumberAxis range = (NumberAxis) plot.getRangeAxis();
			NumberAxis domain = (NumberAxis) plot.getDomainAxis();
			domain.setRange(min1, max1);
			range.setRange(min2, max2);
		}
		
		XYLineAndShapeRenderer renderer = ((XYLineAndShapeRenderer) plot.getRenderer());
		renderer.setDefaultShapesVisible(true);
		renderer.setDrawOutlines(true);
		renderer.setUseFillPaint(true);
        renderer.setDefaultFillPaint(Color.white);
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0));
		renderer.setSeriesPaint(0, new Color(0.0f, 0.0f, .82f, .5f));
		
		//Set tooltip string
		XYToolTipGenerator tooltipGenerator = new XYToolTipGenerator()
		{
			@Override
			public String generateToolTip(XYDataset dataset, int series, int item) {
				AccumulationData data = (AccumulationData)getSeriesData().get(series).getData().get(item);
				List<Object> objects = (List<Object>)(data.getExtraValue());
				return String.valueOf(joinObjects(objects, data.getYValue().doubleValue(),
						data.getValuesList(), key1, MAX_TO_POPUP));
			}
		};
		renderer.setDefaultToolTipGenerator(tooltipGenerator);
		plot.setBackgroundPaint(Color.white);
		chart.getPlot().setForegroundAlpha(0.5f);

		// Create Panel
		SciomeChartViewer chartView = new SciomeChartViewer(chart);

		// LogarithmicAxis yAxis = new LogarithmicAxis();
		chartView.addChartMouseListener(new ChartMouseListenerFX() {

			@Override
			public void chartMouseClicked(ChartMouseEventFX e) {
				if(e.getEntity() != null && e.getEntity().getToolTipText() != null //Check to see if an entity was clicked
						&& e.getTrigger().getButton().equals(MouseButton.PRIMARY)) //Check to see if it was the left mouse button clicked
				showObjectText(e.getEntity().getToolTipText());
			}

			@Override
			public void chartMouseMoved(ChartMouseEventFX e) {
				//ignore for now
			}
		});

		return chartView;
	}

	
}
