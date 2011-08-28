package com.smpete.frugieLog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.smpete.frugieLog.Frugie.FrugieColumns;
import com.smpete.frugieLog.R;
import com.smpete.frugieLog.Frugie.FrugieType;
import com.smpete.frugieLog.Frugie.PortionSize;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class FrugieLogActivity extends Activity implements OnClickListener {

	private Frugie currentFruit;
	private Frugie currentVeggie;
	private long currentId;

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
        
    }

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

    /**
     * Update date and fruit/veggie servings from database given a date
     * 
     * @param date Date to update the data to
     */
    private void updateData(Date date){
    	//TODO Read from current date, dont allow date to be passed!!
    	
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
    
    /**
     * Write current date's data to the database
     */
    private void saveData(){
    	Uri uri = ContentUris.withAppendedId(FrugieColumns.CONTENT_URI, currentId);
		ContentValues values = new ContentValues();
		values.put(FrugieColumns.FRUIT, currentFruit.getServingTenths());
		values.put(FrugieColumns.VEGGIE, currentVeggie.getServingTenths());
    	
    	getContentResolver().update(uri, values, null, null);
    }
    
	/**
	 * Sets the serving size to either half or full
	 * 
	 * @param newServing True to set serving size to a half serving
	 */
    private void setHalfServing(boolean newServing){
    	halfServing = newServing;
    	RadioGroup radios = (RadioGroup) findViewById(R.id.serving_radio_group);
    	if(newServing)
    		radios.check(R.id.half_radio);
    	else
    		radios.check(R.id.full_radio);
    	
    	updateImages();
    }
    
    /**
     * Update fruit and vegetable images based on the serving size
     */
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
    
    /**
     * Changes the current date based on the number of days to
     * increment or decrement
     * 
     * @param days Number of days to add to the current date
     */
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
    
    /**
     * Called from a view - changes the date
     * 
     * @param view View of caller
     */
    public void changeDate(View view){
    	if(view.getId() == R.id.inc_day_button)
    		changeDate(1);
    	else
    		changeDate(-1);
    }
    
    /**
     * Called from a view - changes serving size to full serving
     * 
     * @param view View of caller
     */
    public void changeToFullServing(View view){
    	halfServing = false;
    	// Change images
    	updateImages();
    }
    
    /**
     * Called from a view - changes serving size to half serving
     * 
     * @param view View of caller
     */
    public void changeToHalfServing(View view){
    	halfServing = true;
    	// Change images
    	updateImages();
    }
    
    /**
     * Called from a view - Increments portion of a fruit or veggie, 
     * based on the view
     * 
     * @param view View of caller
     */
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
    
    /**
     * Called from a view - Decrements portion of a fruit or veggie, 
     * based on the view
     * 
     * @param view View of caller
     */
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
    
    /**
     * Updates the date text cased on the current date
     */
    private void updateDateText(){
    	SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
    	SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");

    	TextView dateText = (TextView)findViewById(R.id.date_text);
    	dateText.setText(dateFormat.format(curDate));

    	TextView dayText = (TextView)findViewById(R.id.day_text);
    	dayText.setText(dayFormat.format(curDate));
    }
    
    /**
     * Updates the fruit and veggie portion text based on the current 
     * fruit and veggie objects
     */
    private void updateStatsText()
    {
    	DecimalFormat oneDigit = new DecimalFormat("#,##0.0");

    	TextView fruitText = (TextView)findViewById(R.id.current_fruit_text);
    	fruitText.setText("" + oneDigit.format((double)currentFruit.getServingTenths() / 10));

    	TextView veggieText = (TextView)findViewById(R.id.current_veggie_text);
    	veggieText.setText("" + oneDigit.format((double)currentVeggie.getServingTenths() / 10));
    }
    
	public void onClick(View v) {
		// TODO Auto-generated method stub	
	}
    
    
    
    /**
     * Rudimentary guesture handling, need to polish
     * 
     * @author peter
     *
     */
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
}