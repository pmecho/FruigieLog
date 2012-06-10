package com.smpete.frugieLog;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.smpete.frugieLog.Frugie.FrugieColumns;
import com.smpete.frugieLog.charting.HistoryChart;

public class HistoryActivity extends SherlockFragmentActivity implements LoaderCallbacks<Cursor>{

	private HistoryChart mHistoryChart;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
		mHistoryChart = new HistoryChart(true);
        mHistoryChart.createChartView(this);
        mHistoryChart.hideTitle();
        layout.addView(mHistoryChart.getChartView());

        getSupportLoaderManager().initLoader(0, null, this);
        
        getSupportActionBar().setTitle(R.string.history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		
		SimpleDateFormat dateFormat = new SimpleDateFormat(FrugieColumns.DATE_FORMAT);
    	String nowDate = dateFormat.format(new Date());
    	Loader<Cursor> loader = new CursorLoader(this,
				FrugieColumns.CONTENT_URI, 
				new String[] {FrugieColumns.FRUIT, FrugieColumns.VEGGIE},
				"date <= ?",
				new String[] {nowDate},
				FrugieColumns.DATE + " DESC");
    	

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
