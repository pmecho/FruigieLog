package com.smpete.frugieLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.smpete.frugieLog.Frugie.FrugieColumns;
import com.smpete.frugieLog.ServingFragment.OnServingChangedListener;
import com.smpete.frugieLog.charting.HistoryChart;

public class FrugieLogActivity extends SherlockFragmentActivity implements OnServingChangedListener, LoaderCallbacks<Cursor> {
    

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
	private static final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");

	public static final int ITEM_ID_7_DAYS = 10;
	public static final int ITEM_ID_14_DAYS = 11;
	public static final int ITEM_ID_30_DAYS = 12;
	
    private ServingPagerAdapter mAdapter;
    private ViewPager mPager;
    
	/** Whether a half serving is selected */
	private boolean mHalfServing;
	
	/** Constants for saved bundle keys */
	private final String SAVED_DATE_KEY = "id";
	private final String SAVED_HALF_SERVING_KEY = "serving";
	private final String SAVED_FOCUSED_PAGE_KEY = "focusedPage";
    
    private int mFocusedPage = -1;
    
    private Date mCentralDate;
    
    private ServingFragment mCurrentFrag;
    
    private HistoryChart mHistoryChart;
    private TextView mActionBarTitle;
    private TextView mActionBarSubtitle;
    
    private int mHistoryLimitId;
    
    private DatePickerDialog mDatePicker;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	getSupportActionBar().setDisplayShowCustomEnabled(true);
    	getSupportActionBar().setDisplayShowTitleEnabled(false);
    	getSupportActionBar().setCustomView(R.layout.action_bar_title);
    	
    	createDatePicker();
    	View v = getSupportActionBar().getCustomView();
    	mActionBarTitle = (TextView)v.findViewById(R.id.day);
    	mActionBarSubtitle = (TextView)v.findViewById(R.id.date);
    	v.setBackgroundResource(R.drawable.abs__list_selector_holo_dark);
    	v.setClickable(true);
    	v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDatePicker.show();
			}
		});
        
        // Restore saved data
        if(savedInstanceState != null){
	        // Check for saved date
	        long savedDate = savedInstanceState.getLong(SAVED_DATE_KEY);
	        if(savedDate == 0L)
	        	mCentralDate = new Date();
	        else
	        	mCentralDate = new Date(savedDate);
	        
	        // Set saved serving size, if empty then full serving will be set
	        mHalfServing = savedInstanceState.getBoolean(SAVED_HALF_SERVING_KEY);
	        
	        // Get focused page 
	        setFocusedPage(savedInstanceState.getInt(SAVED_FOCUSED_PAGE_KEY, -1));
        }
        else{
        	mCentralDate = new Date();
        	mHalfServing = false;
        }

        // Handle adapter and pager
	    mPager = (ViewPager)findViewById( R.id.viewpager );
	    mAdapter= new ServingPagerAdapter(getSupportFragmentManager(), mCentralDate);
	    
	    // Set focused page if it hasn't been saved to the middle.
	    if(mFocusedPage == -1){
	    	setFocusedPage(ServingPagerAdapter.MIDDLE);
	    }
	    
	    mPager.setAdapter(mAdapter);
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
	    
	    // Create the chart and chart's view and add to layout
        mHistoryChart = new HistoryChart(false);
        mHistoryChart.createChartView(this);
        
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        View historyView = mHistoryChart.getChartView();
        layout.addView(historyView);
        
        mHistoryLimitId = UserPrefs.getHistoryLengthId(this);
        
        getSupportLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	
    	// Save the current date
    	outState.putLong(SAVED_DATE_KEY, mCentralDate.getTime());
    	//TODO Test!!
    	outState.putBoolean(SAVED_HALF_SERVING_KEY, mHalfServing);
    	outState.putInt(SAVED_FOCUSED_PAGE_KEY, mFocusedPage);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	SubMenu historyLengthSubMenu = menu.addSubMenu("History Length");
    	historyLengthSubMenu.getItem().setIcon(R.drawable.action_bar_history_length).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	MenuItem days7 = historyLengthSubMenu.add(1, ITEM_ID_7_DAYS, 0, R.string.days_7);
    	MenuItem days14 = historyLengthSubMenu.add(1, ITEM_ID_14_DAYS, 1, R.string.days_14);
    	MenuItem days30 = historyLengthSubMenu.add(1, ITEM_ID_30_DAYS, 2, R.string.days_30);
    	
    	historyLengthSubMenu.setGroupCheckable(1, true, true);
    	switch (mHistoryLimitId) {
		case ITEM_ID_7_DAYS:
			days7.setChecked(true);
			mHistoryChart.show7Days();
			break;
		case ITEM_ID_14_DAYS:
			days14.setChecked(true);
			mHistoryChart.show14Days();
			break;
		case ITEM_ID_30_DAYS:
			days30.setChecked(true);
			mHistoryChart.show30Days();
			break;
		}
    	
    	MenuItem history = menu.add(R.string.history).setIcon(R.drawable.action_bar_history);
    	history.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	history.setIntent(new Intent(this, HistoryActivity.class));
    	
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case ITEM_ID_7_DAYS:
			item.setChecked(true);
			mHistoryChart.show7Days();
			UserPrefs.setHistoryLengthId(this, ITEM_ID_7_DAYS);
			mHistoryLimitId = ITEM_ID_7_DAYS;
			getSupportLoaderManager().restartLoader(0, null, this);
			break;
		case ITEM_ID_14_DAYS:
			item.setChecked(true);
			mHistoryChart.show14Days();
			UserPrefs.setHistoryLengthId(this, ITEM_ID_14_DAYS);
			mHistoryLimitId = ITEM_ID_14_DAYS;
			getSupportLoaderManager().restartLoader(0, null, this);
			break;
		case ITEM_ID_30_DAYS:
			item.setChecked(true);
			mHistoryChart.show30Days();
			UserPrefs.setHistoryLengthId(this, ITEM_ID_30_DAYS);
			mHistoryLimitId = ITEM_ID_30_DAYS;
			getSupportLoaderManager().restartLoader(0, null, this);
			break;
		}
    	
    	return super.onOptionsItemSelected(item);
    }

    private void createDatePicker() {
    	Calendar cal = Calendar.getInstance();
		mDatePicker = new DatePickerDialog(FrugieLogActivity.this, new OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
				mCentralDate = cal.getTime();
				mAdapter.setDate(mCentralDate);
				mAdapter.notifyDataSetChanged();
				setFocusedPage(ServingPagerAdapter.MIDDLE);
				mPager.setCurrentItem(mFocusedPage, false);
				
			}
		}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }
    
    /**
     * Set focused page and update the action bar's title
     * @param focusedPage
     */
    private void setFocusedPage(int focusedPage) {
    	mFocusedPage = focusedPage;
    	
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(mCentralDate);
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
    	mHalfServing = newServing;
    	if (mCurrentFrag != null) 
    		mCurrentFrag.setHalfServing(mHalfServing);
    }
    
    // BEGIN EVENT HANDLERS
    
    /**
     * Called from a view - changes serving size to full serving
     * 
     * @param view View of caller
     */
    public void changeToFullServing(View view){
    	setHalfServing(false, false);
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
	public void onVeggieChanged(double newServingValue, int dayOffset){
		if (dayOffset > -1) {
			mHistoryChart.updateVeggie(newServingValue, dayOffset);
		}
	}
	
	@Override
	public void onFruitChanged(double newServingValue, int dayOffset) {
		if (dayOffset > -1) {
			mHistoryChart.updateFruit(newServingValue, dayOffset);
		}
	}
    
	@Override
	public boolean onCheckHalfServing() {
		return mHalfServing;
	}
	
	
	private class ServingPagerAdapter extends FragmentStatePagerAdapter {

		private static final int COUNT = Short.MAX_VALUE;
		public static final int MIDDLE = Short.MAX_VALUE / 2;
		
		private Date date;
		
		public ServingPagerAdapter(FragmentManager fm, Date date) {
			super(fm);
			this.date = date;
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
			
			ServingFragment frag = ServingFragment.newInstance(cal.getTimeInMillis(), mHalfServing);
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
			mCurrentFrag.setHalfServing(mHalfServing);
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		int limit;
		switch (mHistoryLimitId) {
		case ITEM_ID_7_DAYS:
			limit = 8;
			break;
		case ITEM_ID_14_DAYS:
			limit = 15;
			break;
		case ITEM_ID_30_DAYS:
			limit = 31;
			break;
		default:
			limit = 31;
			break;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(FrugieColumns.DATE_FORMAT);
    	String nowDate = dateFormat.format(new Date());
    	Loader<Cursor> loader = new CursorLoader(this,
				FrugieColumns.CONTENT_URI, 
				new String[] {FrugieColumns.FRUIT, FrugieColumns.VEGGIE},
				"date <= ?",
				new String[] {nowDate},
				FrugieColumns.DATE + " DESC LIMIT " + limit);
    	

    	return loader;
    }

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int fruitColumn = data.getColumnIndex(FrugieColumns.FRUIT); 
            int veggieColumn = data.getColumnIndex(FrugieColumns.VEGGIE);
            
            double[] fruits = new double[data.getCount()];
            double[] veggies = new double[data.getCount()];
            do {
            	int i = data.getPosition();
                // Get the field values
                fruits[i] = data.getDouble(fruitColumn) / 10;
                veggies[i] = data.getDouble(veggieColumn) / 10;
            } while (data.moveToNext());
            
            mHistoryChart.updateDataset(fruits, veggies);
        }
        
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}

}