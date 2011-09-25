package com.smpete.frugieLog;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;


import com.smpete.frugieLog.charting.*;

import com.smpete.frugieLog.Frugie.FrugieColumns;
import com.smpete.frugieLog.MainControlFragment.OnMainControlChangedListener;
import com.smpete.frugieLog.R;
import com.smpete.frugieLog.ServingFragment.OnServingChangedListener;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class FrugieLogActivity extends FragmentActivity implements OnMainControlChangedListener, OnServingChangedListener {
	private long currentId;
    
    // Fragments
    private MainControlFragment mainControlFrag;
//    private ServingFragment servingFrag;
    private ServingPagerAdapter adapter;
    
    private int focusedPage = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Date date = new Date(111,8,1);
        
        
	    adapter = new ServingPagerAdapter(this, getSupportFragmentManager(), date);
	    ViewPager pager =
	        (ViewPager)findViewById( R.id.viewpager );
	    pager.setAdapter( adapter );
	    pager.setCurrentItem(focusedPage);

	    
	    
	    
	    pager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                    focusedPage = position;
                    ServingFragment servingFrag = adapter.getServingFragment(focusedPage);
                    mainControlFrag.setDate(servingFrag.getDate());
//                    adapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                            int positionOffsetPixels) {

//                adapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                            Log.d("ElectricSleep", "IDLE at page " + focusedPage);
                    }
            }
    });
	    
        
        // Get fragments
    	mainControlFrag = (MainControlFragment) getSupportFragmentManager().findFragmentById(R.id.main_control_fragment);


        // Only create the chart onCreate, no need for persistence
        createHistoryChart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.     
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    
    @Override
    public void onPause(){
    	super.onPause();
//    	saveData();
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

    /**
     * Update date and fruit/veggie servings from database given a date
     * 
     * @param date Date to update the data to
     */
    public void updateData(Date date){    	
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
//    		servingFrag.setFruitTenths(cursor.getShort(fruitColumn));
//    		servingFrag.setVeggieTenths(cursor.getShort(veggieColumn));
    	}
    	else{ // Need to insert new entry!
    		ContentValues values = new ContentValues();
    		
    		// Set defaults
    		values.put(FrugieColumns.DATE, formattedDate);
    		values.put(FrugieColumns.FRUIT, 0);
    		values.put(FrugieColumns.VEGGIE, 0);
    		
    		
    		Uri uri = getContentResolver().insert(FrugieColumns.CONTENT_URI, values);
    		currentId = ContentUris.parseId(uri);
//    		servingFrag.setFruitTenths((short) 0);
//    		servingFrag.setVeggieTenths((short) 0);
    	}
        
//    	servingFrag.updateStatsText();
    }
    
    /**
     * Write current date's data to the database
     */
    private void saveData(){
    	Uri uri = ContentUris.withAppendedId(FrugieColumns.CONTENT_URI, currentId);
		ContentValues values = new ContentValues();
//		values.put(FrugieColumns.FRUIT, servingFrag.getFruitTenths());
//		values.put(FrugieColumns.VEGGIE, servingFrag.getVeggieTenths());
    	
    	getContentResolver().update(uri, values, null, null);
    }

	/**
     * Creates the history chart and adds it to the view
     */
    private void createHistoryChart(){
    	SimpleDateFormat dateFormat = new SimpleDateFormat(FrugieColumns.DATE_FORMAT);
    	String nowDate = dateFormat.format(new Date());
    	
    	// Pull data prior to and including the current date
    	Cursor cursor = managedQuery(FrugieColumns.CONTENT_URI, 
    									null, 
    									"date <= '" + nowDate + "'", 
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
        
            // Create the chart and chart's view and add to layout
	        HistoryChart chart = new HistoryChart(this, fruits, veggies, count);
	        GraphicalView chartView = ChartFactory.getLineChartView(this, 
	        		chart.getDataset(), chart.getRenderer());
	        
	        LinearLayout layout = (LinearLayout) findViewById(R.id.chart_layout);
	        layout.addView(chartView, new LayoutParams(LayoutParams.FILL_PARENT,
	                LayoutParams.FILL_PARENT));
        }
    }
    
    
    
    // BEGIN EVENT HANDLERS
    
    /**
     * Called from a view - Increments portion of a fruit or veggie, 
     * based on the view
     * 
     * @param view View of caller
     */
    public void incrementPortion(View view){
    	ServingFragment servingFrag = adapter.getServingFragment(focusedPage);
    	ImageButton button = (ImageButton)view;
    	if(button.getId() == R.id.inc_fruit_button)
    		servingFrag.modifyFruit(true);
    	else
    		servingFrag.modifyVeggie(true);
    }
    
    /**
     * Called from a view - Decrements portion of a fruit or veggie, 
     * based on the view
     * 
     * @param view View of caller
     */
    public void decrementPortion(View view){
    	ServingFragment servingFrag = adapter.getServingFragment(focusedPage);
    	ImageButton button = (ImageButton)view;
    	if(button.getId() == R.id.dec_fruit_button)
    		servingFrag.modifyFruit(false);
    	else
    		servingFrag.modifyVeggie(false);
    }
    
    /**
     * Called from a view - changes serving size to full serving
     * 
     * @param view View of caller
     */
    public void changeToFullServing(View view){
    	mainControlFrag.setHalfServing(false, false);
    }
    
    /**
     * Called from a view - changes serving size to half serving
     * 
     * @param view View of caller
     */
    public void changeToHalfServing(View view){
    	mainControlFrag.setHalfServing(true, false);
    }
    
    // END EVENT HANDLERS
    
 
    


	@Override
	public void onPreDateChange() {
		// Handle work prior to date being changed
		saveData();
		
	}

	@Override
	public void onPostDateChange(Date date) {
		// Handle work afer date is changed
		updateData(date);
	}

	@Override
	public void onServingSizeChanged(boolean halfServing) {
		// Handle work on a serving size change
//		servingFrag.setHalfServing(halfServing);
		
	}

	@Override
	public void onServingChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSaveState(ServingFragment fragment) {
		// TODO Auto-generated method stub
    	Uri uri = ContentUris.withAppendedId(FrugieColumns.CONTENT_URI, fragment.getFruigieId());
		ContentValues values = new ContentValues();
		values.put(FrugieColumns.FRUIT, fragment.getFruitTenths());
		values.put(FrugieColumns.VEGGIE, fragment.getVeggieTenths());
    	
    	getContentResolver().update(uri, values, null, null);
	}

	@Override
	public Frugie onLoadData(Date date) {
		// TODO Auto-generated method stub
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
    		return new Frugie(cursor.getLong(idColumn), 
    				cursor.getShort(fruitColumn), 
    				cursor.getShort(veggieColumn));
    	}
    	else{ // Need to insert new entry!
    		ContentValues values = new ContentValues();
    		
    		// Set defaults
    		values.put(FrugieColumns.DATE, formattedDate);
    		values.put(FrugieColumns.FRUIT, 0);
    		values.put(FrugieColumns.VEGGIE, 0);
    		
    		
    		Uri uri = getContentResolver().insert(FrugieColumns.CONTENT_URI, values);
    		currentId = ContentUris.parseId(uri);
    		return new Frugie(ContentUris.parseId(uri), (short)0, (short)0);
    	}
	}
}