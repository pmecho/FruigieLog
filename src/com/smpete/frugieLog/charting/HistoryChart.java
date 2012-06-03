package com.smpete.frugieLog.charting;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;


/**
 * Contains a dataset and renderer for a XY multiple series line chart
 * 
 * @author peter
 *
 */
public class HistoryChart {
	
	private XYMultipleSeriesDataset dataset;
	private XYMultipleSeriesRenderer renderer;
	
	/**
	 * Makes a LineChart with 2 sets of x values and 1 set of y values
	 * 
	 * @param fruits 1st array of x values
	 * @param veggies 2nd array of x values
	 * @param count Array of y values
	 */
	public HistoryChart(Context context, double[] fruits, double[] veggies, double[] count){
        // Build dataset
        String[] titles = new String[] { "Fruit", "Veggie" };
        List<double[]> xValues = new ArrayList<double[]>();
        List<double[]> yValues = new ArrayList<double[]>();
        xValues.add(count);
        xValues.add(count);
        yValues.add(fruits);
        yValues.add(veggies);
        dataset = createDataset(titles, xValues, yValues, 0);

        // Create renderer
        int[] colors = new int[] { Color.RED, Color.GREEN };
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND };
        renderer = createRenderer(colors, styles, getMax(fruits, veggies));
        
        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
          ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
        }
	}
	
	/**
	 * Simple utility method to find the maximum value of two equal length arrays
	 * 
	 * @param fruits First array to find max of
	 * @param veggies Second array to find max of
	 * @return Maximum value of either array.  -1 returned if arrays are not of equal length
	 */
	private double getMax(double[] fruits, double[] veggies){
		double max = 0;
		
		// Something is wrong, they should be the same length
		if(fruits.length != veggies.length)
			return -1;
		
		for(int i = 0; i < fruits.length; i++){
			if(fruits[i] > max)
				max = fruits[i];
			if(veggies[i] > max)
				max = veggies[i];
		}
		
		return max;
	}
	
	/**
	 * Creates the chart renderer with the correct color and style
	 * 
	 * @param colors
	 * @param styles
	 * @return
	 */
    private XYMultipleSeriesRenderer createRenderer(int[] colors, PointStyle[] styles, double maxY) {
    	XYMultipleSeriesRenderer renderer = new  XYMultipleSeriesRenderer();

    	renderer.setAxisTitleTextSize(16);
    	renderer.setChartTitleTextSize(20);
    	renderer.setLabelsTextSize(15);
    	renderer.setLegendTextSize(15);
    	renderer.setPointSize(5f);
    	renderer.setMargins(new int[] { 20, 30, 15, 20 });
    	int length = colors.length;
    	for (int i = 0; i < length; i++) {
    		XYSeriesRenderer r = new XYSeriesRenderer();
    		r.setColor(colors[i]);
    		r.setPointStyle(styles[i]);
    		renderer.addSeriesRenderer(r);
    	}
    	
        renderer.setChartTitle("History");
        renderer.setXTitle("Days ago");
        renderer.setYTitle("Servings");
        renderer.setXAxisMin(0);
        renderer.setXAxisMax(28);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(maxY);
        renderer.setAxesColor(Color.LTGRAY);
        renderer.setLabelsColor(Color.LTGRAY);
        renderer.setXLabels(15);
        renderer.setYLabels(7);
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Align.CENTER);
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setPanEnabled(true, false);
        renderer.setZoomEnabled(true, false);
        renderer.setPanLimits(new double[]{0,365,0,0});
        renderer.setZoomLimits(new double[]{0,365,0,0});
    	
    	return renderer;
    }
    
    /**
     * Creates a dataset with the x and y values
     * 
     * @param titles Titles for the axes
     * @param xValues Values for the x axis
     * @param yValues Values for the y axis
     * @param scale
     */
    private XYMultipleSeriesDataset createDataset(String[] titles, List<double[]> xValues, List<double[]> yValues, int scale) {
    	XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    	
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			XYSeries series = new XYSeries(titles[i], scale);
			double[] xV = xValues.get(i);
			double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
		
		return dataset;
    }

    /**
     * Accessor method for dataset
     * @return Current dataset
     */
	public XYMultipleSeriesDataset getDataset() {
		return dataset;
	}

	/**
	 * Accessor method for renderer
	 * @return Current renderer
	 */
	public XYMultipleSeriesRenderer getRenderer() {
		return renderer;
	}
}
