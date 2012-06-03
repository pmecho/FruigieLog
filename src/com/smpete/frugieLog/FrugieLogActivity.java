package com.smpete.frugieLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.smpete.frugieLog.Frugie.FrugieColumns;
import com.smpete.frugieLog.ServingFragment.OnServingChangedListener;
import com.smpete.frugieLog.charting.HistoryChart;

public class FrugieLogActivity extends SherlockFragmentActivity implements OnServingChangedListener {
    

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
	private static final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
	
    // Fragments
//    private ServingFragment servingFrag;
    private ServingPagerAdapter adapter;
    private ViewPager mPager;
    
	/** Whether a half serving is selected */
	private boolean halfServing;
	
	/** Constants for saved bundle keys */
	private final String SAVED_DATE_KEY = "id";
	private final String SAVED_HALF_SERVING_KEY = "serving";
	private final String SAVED_FOCUSED_PAGE_KEY = "focusedPage";
    
    private int mFocusedPage = -1;
    
    private Date centralDate;
    
    private ServingFragment mCurrentFrag;
    
    private XYMultipleSeriesDataset mDataSet;
    private GraphicalView mChartView;
    private TextView mActionBarTitle;
    private TextView mActionBarSubtitle;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	getSupportActionBar().setDisplayShowCustomEnabled(true);
    	getSupportActionBar().setDisplayShowTitleEnabled(false);
    	getSupportActionBar().setCustomView(R.layout.action_bar_title);
    	
    	View v = getSupportActionBar().getCustomView();
    	mActionBarTitle = (TextView)v.findViewById(R.id.day);
    	mActionBarSubtitle = (TextView)v.findViewById(R.id.date);
    	v.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Calendar cal = Calendar.getInstance();
				new DatePickerDialog(FrugieLogActivity.this, new OnDateSetListener() {
					
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
							int dayOfMonth) {
						Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
						centralDate = cal.getTime();
						adapter.setDate(centralDate);
						adapter.notifyDataSetChanged();
						setFocusedPage(ServingPagerAdapter.MIDDLE);
						mPager.setCurrentItem(mFocusedPage, false);
						
					}
				}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
        
        // Restore saved data
        if(savedInstanceState != null){
	        // Check for saved date
	        long savedDate = savedInstanceState.getLong(SAVED_DATE_KEY);
	        if(savedDate == 0L)
	        	centralDate = new Date();
	        else
	        	centralDate = new Date(savedDate);
	        
	        // Set saved serving size, if empty then full serving will be set
	        halfServing = savedInstanceState.getBoolean(SAVED_HALF_SERVING_KEY);
	        
	        // Get focused page 
	        setFocusedPage(savedInstanceState.getInt(SAVED_FOCUSED_PAGE_KEY, -1));
        }
        else{
        	centralDate = new Date();
        	halfServing = false;
        }

        // Handle adapter and pager
	    mPager = (ViewPager)findViewById( R.id.viewpager );
	    adapter= new ServingPagerAdapter(getSupportFragmentManager(), centralDate);
	    
	    // Set focused page if it hasn't been saved to the middle.
	    if(mFocusedPage == -1){
	    	setFocusedPage(ServingPagerAdapter.MIDDLE);
	    }
	    
	    mPager.setAdapter(adapter);
	    mPager.setCurrentItem(mFocusedPage);
	    
	    mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                    setFocusedPage(position);
            }

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
	    });
	    
        // Only create the chart onCreate, no need for persistence
        createHistoryChart();
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
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	
    	// Save the current date
    	outState.putLong(SAVED_DATE_KEY, centralDate.getTime());
    	//TODO Test!!
    	outState.putBoolean(SAVED_HALF_SERVING_KEY, halfServing);
    	outState.putInt(SAVED_FOCUSED_PAGE_KEY, mFocusedPage);
    }
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//        return true;
//    }

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
	        mDataSet = chart.getDataset();
	        mChartView = ChartFactory.getLineChartView(this, 
	        		chart.getDataset(), chart.getRenderer());
	        
	        LinearLayout layout = (LinearLayout) findViewById(R.id.chart_layout);
	        layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
	                LayoutParams.FILL_PARENT));
        }
    }
    
    /**
     * Set focused page and update the action bar's title
     * @param focusedPage
     */
    private void setFocusedPage(int focusedPage) {
    	mFocusedPage = focusedPage;
    	
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(centralDate);
    	cal.add(Calendar.DATE, mFocusedPage - ServingPagerAdapter.MIDDLE);
    	
    	Calendar today = Calendar.getInstance(); // today
    	Calendar yesterday = Calendar.getInstance();
    	yesterday.add(Calendar.DAY_OF_YEAR, -1); // yesterday

    	// Use "Today" if date is today
    	if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
    			&& cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
    		mActionBarTitle.setText(R.string.today);
    	} else if (cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)
    			&& cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
    		mActionBarTitle.setText(R.string.yesterday);
    	} else {
    		mActionBarTitle.setText(dayFormat.format(cal.getTime()));
    	}
    	
    	mActionBarSubtitle.setText(dateFormat.format(cal.getTime()));
    }
    
    public void setHalfServing(boolean newServing, boolean updateRadios){
    	if(updateRadios){
        	RadioGroup radios = (RadioGroup) findViewById(R.id.serving_radio_group);
        	if(newServing)
        		radios.check(R.id.half_radio);
        	else
        		radios.check(R.id.full_radio);
    	}
    	halfServing = newServing;
    	if (mCurrentFrag != null) 
    		mCurrentFrag.setHalfServing(halfServing);
    }
    
    // BEGIN EVENT HANDLERS
    
    /**
     * Called from a view - changes serving size to full serving
     * 
     * @param view View of caller
     */
    public void changeToFullServing(View view){
    	setHalfServing(false, false);
    	// TODO Yeah!!! Test code to dynamically change a point
//    	XYSeries series = mDataSet.getSeriesAt(0);
//    	double y = series.getY(0);
//    	series.add(0,y+1);
//    	mChartView.repaint();
    }
    
    /**
     * Called from a view - changes serving size to half serving
     * 
     * @param view View of caller
     */
    public void changeToHalfServing(View view){
    	setHalfServing(true, false);
    }
    
    // END EVENT HANDLERS
    

    
    // Callbacks from serving fragment
	@Override
	public void onServingChanged() {
		// TODO Auto-generated method stub
		
	}
    
    /**
     * Write current date's data to the database
     */
	@Override
	public void onSaveState(ServingFragment fragment) {
		// TODO Auto-generated method stub
    	Uri uri = ContentUris.withAppendedId(FrugieColumns.CONTENT_URI, fragment.getFruigieId());
		ContentValues values = new ContentValues();
		values.put(FrugieColumns.FRUIT, fragment.getFruitTenths());
		values.put(FrugieColumns.VEGGIE, fragment.getVeggieTenths());
    	
    	getContentResolver().update(uri, values, null, null);
	}

    /**
     * Update date and fruit/veggie servings from database given a date
     * 
     * @param date Date to update the data to
     */
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
    		return new Frugie(ContentUris.parseId(uri), (short)0, (short)0);
    	}
	}

	@Override
	public boolean onCheckHalfServing() {
		// TODO Auto-generated method stub
		return halfServing;
	}
	
	
	private class ServingPagerAdapter extends FragmentStatePagerAdapter {

		private static final int COUNT = Short.MAX_VALUE;
		public static final int MIDDLE = Short.MAX_VALUE / 2;
		
		private Date date;
		
		public ServingPagerAdapter(FragmentManager fm, Date date) {
			super(fm);
			this.date = date;
		}

		public ServingFragment getServingFragment(int position){
			return (ServingFragment)getItem(mPager.getCurrentItem());
		}
		
		public void setDate(Date date) {
			this.date = date;
		}
		
		@Override
		public int getItemPosition(Object object) {
		    return POSITION_NONE;
		}
		
		@Override
		public Fragment getItem(int position) {
			Log.v("ServingPagerAdapter", "getItem: " + position);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, position - MIDDLE);
			
			ServingFragment frag = ServingFragment.newInstance(cal.getTimeInMillis(), halfServing);
			return frag;
		}
		
		@Override
		public int getCount() {
			return COUNT;
		}
		
		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			super.setPrimaryItem(container, position, object);
			mCurrentFrag = (ServingFragment)object;
			mCurrentFrag.setHalfServing(halfServing);
		}
	}

}