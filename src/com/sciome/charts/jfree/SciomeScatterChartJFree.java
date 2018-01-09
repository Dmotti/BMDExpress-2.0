package com.sciome.charts.jfree;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.controlsfx.control.RangeSlider;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.ColorBlock;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import com.sciome.bmdexpress2.mvp.model.ChartKey;
import com.sciome.bmdexpress2.mvp.model.IMarkable;
import com.sciome.charts.SciomeChartListener;
import com.sciome.charts.SciomeScatterChart;
import com.sciome.charts.data.ChartConfiguration;
import com.sciome.charts.data.ChartDataPack;
import com.sciome.charts.model.SciomeData;
import com.sciome.charts.model.SciomeSeries;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;

public class SciomeScatterChartJFree extends SciomeScatterChart
{

	private List<AbstractXYAnnotation>	chattingAnnotations	= new ArrayList<>();
	private List<AbstractXYAnnotation>	markedAnnotations	= new ArrayList<>();
	private JFreeChart					chart;
	private double						lowX;
	private double						lowY;
	private double						highX;
	private double						highY;

	public SciomeScatterChartJFree(String title, List<ChartDataPack> chartDataPacks, ChartKey key1,
			ChartKey key2, boolean allowXLogAxis, boolean allowYLogAxis, SciomeChartListener chartListener)
	{
		super(title, chartDataPacks, key1, key2, allowXLogAxis, allowYLogAxis, true, true, chartListener);
	}

	public SciomeScatterChartJFree(String title, List<ChartDataPack> chartDataPacks, ChartKey key1,
			ChartKey key2, SciomeChartListener chartListener)
	{
		this(title, chartDataPacks, key1, key2, true, true, chartListener);
	}

	@Override
	protected Node generateChart(ChartKey[] keys, ChartConfiguration chartConfig)
	{
		ChartKey key1 = keys[0];
		ChartKey key2 = keys[1];
		Double max1 = getMaxMax(key1);
		Double min1 = getMinMin(key1);

		Double max2 = getMaxMax(key2);
		Double min2 = getMinMin(key2);

		DefaultXYDataset dataset = new DefaultXYDataset();

		for (SciomeSeries<Number, Number> series : getSeriesData())
		{
			double[] domains = new double[series.getData().size()];
			double[] ranges = new double[series.getData().size()];

			int i = 0;
			for (SciomeData<Number, Number> chartData : series.getData())
			{
				double domainvalue = chartData.getXValue().doubleValue();
				double rangevalue = chartData.getYValue().doubleValue();
				domains[i] = domainvalue;
				ranges[i++] = rangevalue;
			}
			dataset.addSeries(series.getName(), new double[][] { domains, ranges });
		}

		chart = ChartFactory.createScatterPlot(key1.toString() + " Vs. " + key2.toString(), key1.toString(),
				key2.toString(), dataset, PlotOrientation.VERTICAL, true, true, false);
		XYPlot plot = chart.getXYPlot();
		// plot.setForegroundAlpha(0.1f);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
		plot.setDomainAxis(
				SciomeNumberAxisGeneratorJFree.generateAxis(getLogXAxis().isSelected(), key1.toString()));
		plot.setRangeAxis(
				SciomeNumberAxisGeneratorJFree.generateAxis(getLogYAxis().isSelected(), key2.toString()));

		// add the annotations (if they exist). This is mainly for when the user
		// changes the axis or chartconfiguration
		if (chattingAnnotations != null)
			for (AbstractXYAnnotation ann : chattingAnnotations)
				plot.addAnnotation(ann);
		if (markedAnnotations != null)
			for (AbstractXYAnnotation ann : markedAnnotations)
				plot.addAnnotation(ann);

		// Only want to zoom in if we any values have been set in chartConfig
		if (chartConfig != null)
		{
			// Find the values for the min and max values for x and y
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
		setSliders(min1, max1, min2, max2);
		
		XYLineAndShapeRenderer renderer = ((XYLineAndShapeRenderer) plot.getRenderer());

		// Set tooltip string
		XYToolTipGenerator tooltipGenerator = new XYToolTipGenerator() {
			@Override
			public String generateToolTip(XYDataset dataset, int series, int item)
			{
				return ((ChartExtraValue) getSeriesData().get(series).getData().get(item)
						.getExtraValue()).userData.toString();
			}
		};
		renderer.setDefaultToolTipGenerator(tooltipGenerator);
		plot.setBackgroundPaint(Color.white);
		// chart.getPlot().setForegroundAlpha(0.5f);

		// Create Panel
		SciomeChartViewer chartView = new SciomeChartViewer(chart);

		// Add plot point clicking interaction
		chartView.addChartMouseListener(new ChartMouseListenerFX() {

			@Override
			public void chartMouseClicked(ChartMouseEventFX e)
			{
				if (e.getEntity() != null && e.getEntity().getToolTipText() != null // Check to see if an
																					// entity was clicked
						&& e.getTrigger().getButton().equals(MouseButton.PRIMARY)) // Check to see if it was
																					// the left mouse button
																					// clicked
				{
					if (!(e.getEntity() instanceof XYItemEntity))
						return;
					int seriesIndex = ((XYItemEntity) e.getEntity()).getSeriesIndex();
					int item = ((XYItemEntity) e.getEntity()).getItem();

					// get the object associated with with the click and post it to the other charts
					// so they can highlight it.
					@SuppressWarnings("unchecked")
					Object userData = ((ChartExtraValue) getSeriesData().get(seriesIndex).getData().get(item)
							.getExtraValue()).userData;
					postObjectsForChattingCharts(Arrays.asList(userData));
					if (e.getTrigger().getClickCount() == 2)
						showObjectText(e.getEntity().getToolTipText());
				}
				else if (e.getTrigger().getClickCount() == 2)
					postObjectsForChattingCharts(new ArrayList<>());
			}

			@Override
			public void chartMouseMoved(ChartMouseEventFX e)
			{
				// ignore for now
			}
		});

		return chartView;
	}

	@Override
	public void reactToChattingCharts()
	{
		for (AbstractXYAnnotation annotation : chattingAnnotations)
			chart.getXYPlot().removeAnnotation(annotation, false);
		Set<String> conversationalSet = new HashSet<>();
		for (Object obj : getConversationalObjects())
			conversationalSet.add(obj.toString().toLowerCase());

		chattingAnnotations.clear();

		for (SciomeSeries<Number, Number> series : getSeriesData())
		{
			for (SciomeData<Number, Number> chartData : series.getData())
			{
				if (conversationalSet.contains(
						((ChartExtraValue) chartData.getExtraValue()).userData.toString().toLowerCase()))
				{
					XYDrawableAnnotation ann = new XYDrawableAnnotation(chartData.getXValue().doubleValue(),
							chartData.getYValue().doubleValue(), 10, 10, new ColorBlock(Color.pink, 10, 10));

					XYDrawableAnnotation ann2 = new XYDrawableAnnotation(chartData.getXValue().doubleValue(),
							chartData.getYValue().doubleValue(), 12, 12, new ColorBlock(Color.BLACK, 12, 12));

					// ann2 will give us black outline
					chattingAnnotations.add(ann2);
					chattingAnnotations.add(ann);
					chart.getXYPlot().addAnnotation(ann2, false);
					chart.getXYPlot().addAnnotation(ann, false);
					if (((ChartExtraValue) chartData.getExtraValue()).userData instanceof IMarkable)
					{
						IMarkable markable = (IMarkable) ((ChartExtraValue) chartData
								.getExtraValue()).userData;
						DraggableXYPointerAnnotation labelann = new DraggableXYPointerAnnotation(
								markable.getMarkableLabel(), chartData.getXValue().doubleValue(),
								chartData.getYValue().doubleValue(), Math.PI * 4 / 3);
						labelann.setBaseRadius(40.0);
						labelann.setLabelOffset(5.0);
						labelann.setBackgroundPaint(Color.pink);
						labelann.setOutlineVisible(true);
						labelann.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 12));
						labelann.setTipRadius(5);
						labelann.setTextAnchor(TextAnchor.HALF_ASCENT_RIGHT);
						chattingAnnotations.add(labelann);
						chart.getXYPlot().addAnnotation(labelann, false);
					}

				}
			}
		}
		chart.fireChartChanged();
	}

	@Override
	public void markData(Set<String> markings)
	{
		for (AbstractXYAnnotation annotation : markedAnnotations)
			chart.getXYPlot().removeAnnotation(annotation, false);

		for (SciomeSeries<Number, Number> series : getSeriesData())
		{
			for (SciomeData<Number, Number> chartData : series.getData())
			{
				if (((ChartExtraValue) chartData.getExtraValue()).userData instanceof IMarkable)
				{
					IMarkable markable = (IMarkable) ((ChartExtraValue) chartData.getExtraValue()).userData;

					if (!dataIsMarked(markings, markable.getMarkableKeys()))
						continue;
					XYDrawableAnnotation ann = new XYDrawableAnnotation(chartData.getXValue().doubleValue(),
							chartData.getYValue().doubleValue(), 15, 15,
							new ColorBlock(markable.getMarkableColor(), 15, 15));
					XYDrawableAnnotation ann2 = new XYDrawableAnnotation(chartData.getXValue().doubleValue(),
							chartData.getYValue().doubleValue(), 17, 17, new ColorBlock(Color.BLACK, 17, 17));

					DraggableXYPointerAnnotation labelann = new DraggableXYPointerAnnotation(
							markable.getMarkableLabel(), chartData.getXValue().doubleValue(),
							chartData.getYValue().doubleValue(), Math.PI * 4 / 3);
					labelann.setBaseRadius(40.0);
					labelann.setLabelOffset(5.0);
					labelann.setBackgroundPaint(Color.yellow);
					labelann.setOutlineVisible(true);
					labelann.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 12));
					labelann.setTipRadius(5);
					labelann.setTextAnchor(TextAnchor.HALF_ASCENT_RIGHT);

					// ann2 will give us black outline
					markedAnnotations.add(ann2);
					markedAnnotations.add(ann);
					markedAnnotations.add(labelann);
					chart.getXYPlot().addAnnotation(ann2, false);
					chart.getXYPlot().addAnnotation(ann, false);
					chart.getXYPlot().addAnnotation(labelann, false);
				}
			}
		}
		chart.fireChartChanged();

	}

	private boolean dataIsMarked(Set<String> markings, Set<String> data)
	{
		for (String d : data)
			if (markings.contains(d))
				return true;
		return false;
	}
	
	private void setSliders(double minX, double maxX, double minY, double maxY) {
		lowX = minX;
		highX = maxX;
		lowY = minY;
		highY = maxY;
		
		RangeSlider hSlider = new RangeSlider(minX, maxX, minX, maxX);
		RangeSlider vSlider = new RangeSlider(minY, maxY, minY, maxY);
		vSlider.setOrientation(Orientation.VERTICAL);
		hSlider.lowValueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number oldValue, Number newValue) {
				lowX = newValue.doubleValue();
				if(lowX != highX)
					chart.getXYPlot().getDomainAxis().setRange(new Range(lowX, highX));
			}
		});
		
		hSlider.highValueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number oldValue, Number newValue) {
				highX = newValue.doubleValue();
				if(lowX != highX)
					chart.getXYPlot().getDomainAxis().setRange(new Range(lowX, highX));
			}
		});
		
		vSlider.lowValueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number oldValue, Number newValue) {
				lowY = newValue.doubleValue();
				if(lowY != highY)
					chart.getXYPlot().getRangeAxis().setRange(new Range(lowY, highY));
			}
		});
		
		vSlider.highValueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number oldValue, Number newValue) {
				highY = newValue.doubleValue();
				if(lowY != highY)
					chart.getXYPlot().getRangeAxis().setRange(new Range(lowY, highY));
			}
		});
		
		sethSlider(hSlider);
		setvSlider(vSlider);
	}

}
