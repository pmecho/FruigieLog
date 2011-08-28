package com.smpete.frugieLog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.util.MathHelper;

import com.smpete.frugieLog.charting.*;

import com.smpete.frugieLog.Frugie.FrugieColumns;
import com.smpete.frugieLog.R;
import com.smpete.frugieLog.Frugie.FrugieType;
import com.smpete.frugieLog.Frugie.PortionSize;
import com.smpete.frugieLog.charting.IDemoChart;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FrugieLogActivity extends Activity implements OnClickListener {

	private Frugie currentFruit;
	private Frugie currentVeggie;
	private long currentId;
	
	private IDemoChart chart = new SensorValuesChart();


	private final String SAVED_DATE_KEY = "id";
	private final String SAVED_HALF_SERVING_KEY = "serving";
	
	private Date curDate;
	/** Whether a half serving is selected */
	private boolean halfServing;
	
	// For guestures
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Restore saved data
        if(savedInstanceState != null){
	        // Check for saved date
	        long savedDate = savedInstanceState.getLong(SAVED_DATE_KEY);
	        if(savedDate == 0L)
	        	curDate = new Date();
	        else
	        	curDate = new Date(savedDate);
	        
	        // Set saved serving size, if empty then full serving will be set
	        setHalfServing(savedInstanceState.getBoolean(SAVED_HALF_SERVING_KEY));
        }
        else{
        	curDate = new Date();
        	setHalfServing(false);
        }

        // Gesture detection
        View mainView = (View) findViewById(R.id.screen_layout);
     
        gestureDetector = new GestureDetector(new MyGestureDetector());
        mainView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        });
        
        
        
        // Chart Testing
        
        
    	SimpleDateFormat dateFormat = new SimpleDateFormat(FrugieColumns.DATE_FORMAT);
    	String nowDate = dateFormat.format(new Date());
    	
    	// Set new data
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -30);
		String pastDate = dateFormat.format(cal.getTime());
    	
    	Cursor cursor = managedQuery(FrugieColumns.CONTENT_URI, 
    									null, 
    									FrugieColumns.DATE + " BETWEEN '" + pastDate + "' AND '" + nowDate + "'", 
    									null, 
    									FrugieColumns.DATE + " DESC");
        
        if (cursor.moveToFirst()) {
            int fruitColumn = cursor.getColumnIndex(FrugieColumns.FRUIT); 
            int veggieColumn = cursor.getColumnIndex(FrugieColumns.VEGGIE);
            
            double[] fruits = new double[cursor.getCount()];
            double[] veggies = new double[cursor.getCount()];
            double[] count = new double[cursor.getCount()];
            do {
            	int i = cursor.getPosition();
                // Get the field values
                fruits[i] = cursor.getDouble(fruitColumn) / 10;
                veggies[i] = cursor.getDouble(veggieColumn) / 10;
                count[i] = i;

            } while (cursor.moveToNext());


        
        
        
        
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
        
        
        GraphicalView view = ChartFactory.getLineChartView(this, buildDataset(titles, x, values), renderer);
        
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart_layout);
        layout.addView(view, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        
        
        }
        
        
        
        
        
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
    
    
    public void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues,
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
    
    
//    /**
//     * Builds an XY multiple time dataset using the provided values.
//     * 
//     * @param titles the series titles
//     * @param xValues the values for the X axis
//     * @param yValues the values for the Y axis
//     * @return the XY multiple time dataset
//     */
//    private XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues,
//        List<double[]> yValues) {
//      XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
//      int length = titles.length;
//      for (int i = 0; i < length; i++) {
//        TimeSeries series = new TimeSeries(titles[i]);
//        Date[] xV = xValues.get(i);
//        double[] yV = yValues.get(i);
//        int seriesLength = xV.length;
//        for (int k = 0; k < seriesLength; k++) {
//          series.add(xV[k], yV[k]);
//        }
//        dataset.addSeries(series);
//      }
//      return dataset;
//    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.     
        
        currentFruit = new Frugie(FrugieType.FRUIT);
        currentVeggie = new Frugie(FrugieType.VEGGIE);
        updateData(curDate);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    @Override
    public void onPause(){
    	super.onPause();
    	saveData();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	// Save the current date
    	outState.putLong(SAVED_DATE_KEY, curDate.getTime());
    	//TODO Test!!
    	outState.putBoolean(SAVED_HALF_SERVING_KEY, halfServing);
    }

    
    private void updateData(Date date){
    	SimpleDateFormat dateFormat = new SimpleDateFormat(FrugieColumns.DATE_FORMAT);
    	String formattedDate = dateFormat.format(date);
    	
    	Cursor cursor = managedQuery(FrugieColumns.CONTENT_URI, 
    									null, 
    									FrugieColumns.DATE +"='" + formattedDate + "'", 
    									null, 
    									null);
    	if(cursor.moveToFirst()){
    		int idColumn = cursor.getColumnIndex(FrugieColumns._ID);
    		int fruitColumn = cursor.getColumnIndex(FrugieColumns.FRUIT);
    		int veggieColumn = cursor.getColumnIndex(FrugieColumns.VEGGIE);
    		currentId = cursor.getLong(idColumn);
    		currentFruit.setServingTenths(cursor.getShort(fruitColumn));
    		currentVeggie.setServingTenths(cursor.getShort(veggieColumn));
    	}
    	else{ // Need to insert new entry!
    		ContentValues values = new ContentValues();
    		
    		// Set defaults
    		values.put(FrugieColumns.DATE, formattedDate);
    		values.put(FrugieColumns.FRUIT, 0);
    		values.put(FrugieColumns.VEGGIE, 0);
    		
    		
    		Uri uri = getContentResolver().insert(FrugieColumns.CONTENT_URI, values);
    		currentId = ContentUris.parseId(uri);
    		currentFruit.setServingTenths((short) 0);
    		currentVeggie.setServingTenths((short) 0);
    	}
        
    	updateDateText();
    	updateStatsText();
    }
    
    private void saveData(){
    	Uri uri = ContentUris.withAppendedId(FrugieColumns.CONTENT_URI, currentId);
		ContentValues values = new ContentValues();
		values.put(FrugieColumns.FRUIT, currentFruit.getServingTenths());
		values.put(FrugieColumns.VEGGIE, currentVeggie.getServingTenths());
    	
    	getContentResolver().update(uri, values, null, null);
    }
    

    private void setHalfServing(boolean newServing){
    	halfServing = newServing;
    	RadioGroup radios = (RadioGroup) findViewById(R.id.serving_radio_group);
    	if(newServing)
    		radios.check(R.id.half_radio);
    	else
    		radios.check(R.id.full_radio);
    	
    	updateImages();
    }
    
    private void updateImages(){
    	ImageView fruitImage = (ImageView)findViewById(R.id.fruit_image);
    	ImageView veggieImage = (ImageView)findViewById(R.id.veggie_image);
    	if(halfServing){
        	fruitImage.setImageResource(R.drawable.banana_half);
        	veggieImage.setImageResource(R.drawable.carrot_half);
    	}
    	else{
        	fruitImage.setImageResource(R.drawable.banana);
        	veggieImage.setImageResource(R.drawable.carrot);
    	}
    }
    
    private void changeDate(int days){
    	// Save old data
    	saveData();

    	// Set new data
		Calendar cal = Calendar.getInstance();
		cal.setTime(curDate);
		cal.add(Calendar.DATE, days);
		curDate = cal.getTime();
		
		updateData(curDate);
    }
    
    public void changeDate(View view){
    	if(view.getId() == R.id.inc_day_button)
    		changeDate(1);
    	else
    		changeDate(-1);
    }
    
    public void changeToFullServing(View view){
    	halfServing = false;
    	// Change images
    	updateImages();
    }
    
    public void changeToHalfServing(View view){
    	halfServing = true;
    	// Change images
    	updateImages();
    }
    
    
    public void incrementPortion(View view){
    	ImageButton button = (ImageButton)view;
    	if(button.getId() == R.id.inc_fruit_button){
    		if(halfServing)
    			currentFruit.incServing(PortionSize.HALF);
    		else
    			currentFruit.incServing(PortionSize.FULL);
    	}
    	else{
    		if(halfServing)
    			currentVeggie.incServing(PortionSize.HALF);
    		else
    			currentVeggie.incServing(PortionSize.FULL);
    	}
    	
    	updateStatsText();
    }
    
    public void decrementPortion(View view){
    	ImageButton button = (ImageButton)view;
    	if(button.getId() == R.id.dec_fruit_button){
    		if(halfServing)
    			currentFruit.decServing(PortionSize.HALF);
    		else
    			currentFruit.decServing(PortionSize.FULL);
    	}
    	else{
    		if(halfServing)
    			currentVeggie.decServing(PortionSize.HALF);
    		else
    			currentVeggie.decServing(PortionSize.FULL);
    	}
    	updateStatsText();
    }
    
    private void updateDateText(){
    	SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
    	SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");

    	TextView dateText = (TextView)findViewById(R.id.date_text);
    	dateText.setText(dateFormat.format(curDate));

    	TextView dayText = (TextView)findViewById(R.id.day_text);
    	dayText.setText(dayFormat.format(curDate));
    }
    
    private void updateStatsText()
    {
    	DecimalFormat oneDigit = new DecimalFormat("#,##0.0");

    	TextView fruitText = (TextView)findViewById(R.id.current_fruit_text);
    	fruitText.setText("" + oneDigit.format((double)currentFruit.getServingTenths() / 10));

    	TextView veggieText = (TextView)findViewById(R.id.current_veggie_text);
    	veggieText.setText("" + oneDigit.format((double)currentVeggie.getServingTenths() / 10));
    }
    
    

    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(FrugieLogActivity.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                    changeDate(1);
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(FrugieLogActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                    changeDate(-1);
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
        
        // It is necessary to return true from onDown for the onFling event to register
        @Override
        public boolean onDown(MotionEvent e) {
            	return true;
        }

    }

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
    

    
}