package com.smpete.frugieLog.charting;

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
 * Contains a dataset and renderer for a XY multiple series line chart
 * 
 * @author peter
 *
 */
public class HistoryChart {

	private static final int FRUIT_SERIES_INDEX = 0;
	private static final int VEGGIE_SERIES_INDEX = 1;
	
	private XYMultipleSeriesDataset mDataset;
	private XYSeries mFruitSeries;
	private XYSeries mVeggieSeries;
	
	private XYMultipleSeriesRenderer mRenderer;
	private GraphicalView mChartView;
	private double mMaxY;
	
	public HistoryChart(boolean scrollable) {
        mDataset = new XYMultipleSeriesDataset();
        mFruitSeries = new XYSeries("Fruit");
        mVeggieSeries = new XYSeries("Veggie");
        mDataset.addSeries(FRUIT_SERIES_INDEX, mFruitSeries);
        mDataset.addSeries(VEGGIE_SERIES_INDEX, mVeggieSeries);
        
        // Create renderer
        int[] colors = new int[] { Color.RED, Color.GREEN };
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND };
        mRenderer = createRenderer(scrollable, colors, styles);
        show30Days();
        
        int length = mRenderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
          ((XYSeriesRenderer) mRenderer.getSeriesRendererAt(i)).setFillPoints(true);
        }
	}
	
	public void hideTitle() {
		mRenderer.setChartTitle("");
	}
	
	public void show7Days() {
		mRenderer.setXLabels(8);
		mRenderer.setXAxisMin(-0.1);
		mRenderer.setXAxisMax(7.1);
		if (mChartView != null) {
			mChartView.repaint();
		}
	}
	
	public void show14Days() {
		mRenderer.setXLabels(15);
		mRenderer.setXAxisMin(-0.25);
		mRenderer.setXAxisMax(14.25);
		if (mChartView != null) {
			mChartView.repaint();
		}
	}
	
	public void show30Days() {
		mRenderer.setXLabels(15);
		mRenderer.setXAxisMin(-0.5);
		mRenderer.setXAxisMax(30.5);
		if (mChartView != null) {
			mChartView.repaint();
		}
	}
	
	public void createChartView(Context context) {
		mChartView = ChartFactory.getLineChartView(context, mDataset, mRenderer);
	}
	
	public GraphicalView getChartView() {
		return mChartView;
	}
	
	private double getMaxY() {
		return Math.max(mFruitSeries.getMaxY(), mVeggieSeries.getMaxY()) + .5;
	}
	
	/**
	 * Creates the chart renderer with the correct color and style
	 * 
	 * @param colors
	 * @param styles
	 * @return
	 */
    private XYMultipleSeriesRenderer createRenderer(boolean scrollable, int[] colors, PointStyle[] styles) {
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
        renderer.setXAxisMin(-0.5);
        renderer.setXAxisMax(30);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(mMaxY);
        renderer.setAxesColor(Color.LTGRAY);
        renderer.setLabelsColor(Color.LTGRAY);
        renderer.setYLabels(7);
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Align.CENTER);
        renderer.setYLabelsAlign(Align.RIGHT);

        renderer.setPanEnabled(scrollable, false);
        renderer.setZoomEnabled(scrollable, false);
    	
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
    public void updateDataset(double[] fruits, double[] veggies) {
    	mFruitSeries.clear();
    	mVeggieSeries.clear();

		for (int i = 0; i < fruits.length; i++) {
			mFruitSeries.add(i, fruits[i]);
			mVeggieSeries.add(i, veggies[i]);
		}
		
		mRenderer.setYAxisMax(getMaxY());
        mRenderer.setPanLimits(new double[]{-0.5,mFruitSeries.getItemCount(),0,0});
        mRenderer.setZoomLimits(new double[]{-0.5,mFruitSeries.getItemCount(),0,0});
		mChartView.repaint();
    }

}