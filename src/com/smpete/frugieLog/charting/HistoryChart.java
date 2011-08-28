package com.smpete.frugieLog.charting;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;


/**
 * Warning!!!! Major hacking!!!
 * 
 * Test code just thrown into class, need to come up with a design...
 * Also just pulled methods from aChartDemo
 * 
 * @author peter
 *
 */
public class HistoryChart {
	
	private GraphicalView view;
	
	public HistoryChart(Context context, double[] fruits, double[] veggies, double[] count){
        // Chart Testing

        
        
        
        String[] titles = new String[] { "Veggie", "Fruit" };
        List<double[]> x = new ArrayList<double[]>();
        List<double[]> values = new ArrayList<double[]>();

        x.add(count);
        x.add(count);
        values.add(fruits);
        values.add(veggies);


        int[] colors = new int[] { Color.GREEN, Color.RED };
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND };
        XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
          ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
        }
        
        // Set up chart
        renderer.setChartTitle("History");
        renderer.setXTitle("Days ago");
        renderer.setYTitle("Servings");
        renderer.setXAxisMin(30);
        renderer.setXAxisMax(0);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(7);
        renderer.setAxesColor(Color.LTGRAY);
        renderer.setLabelsColor(Color.LTGRAY);
        renderer.setXLabels(15);
        renderer.setYLabels(7);
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Align.CENTER);
        renderer.setYLabelsAlign(Align.RIGHT);
        
        
        view = ChartFactory.getLineChartView(context, buildDataset(titles, x, values), renderer);
        
        
	}
        
	
	public GraphicalView getView(){
		return view;
	}
	
    /**
     * Builds an XY multiple series renderer.
     * 
     * @param colors the series rendering colors
     * @param styles the series point styles
     * @return the XY multiple series renderers
     */
    private XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
      XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
      setRenderer(renderer, colors, styles);
      return renderer;
    }

    private void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
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
    }
    
    
    /**
     * Builds an XY multiple dataset using the provided values.
     * 
     * @param titles the series titles
     * @param xValues the values for the X axis
     * @param yValues the values for the Y axis
     * @return the XY multiple dataset
     */
    private XYMultipleSeriesDataset buildDataset(String[] titles, List<double[]> xValues,
        List<double[]> yValues) {
      XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
      addXYSeries(dataset, titles, xValues, yValues, 0);
      return dataset;
    }
    
    
    private void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues,
    	      List<double[]> yValues, int scale) {
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
    	  }

}
