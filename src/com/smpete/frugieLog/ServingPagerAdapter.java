package com.smpete.frugieLog;

import java.util.Calendar;
import java.util.Date;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

public class ServingPagerAdapter extends
		android.support.v4.app.FragmentStatePagerAdapter {

	private int count = 3;
	private Date date;
//	private static ServingFragment[] fragments = new ServingFragment[count];
	private ServingFragment currentFrag;
	private ViewPager mPager;
	
	public ServingPagerAdapter(FragmentManager fm, Date date, ViewPager pager) {
		super(fm);
		this.date = date;
	}

	public void setDate(Date date){
		this.date = date;
		//TODO Reload objects based on new date?
	}
	
	public void setServingSize(boolean half){
//		for(ServingFragment frag : fragments){
//			frag.setHalfServing(half);
//		}
	}
	
	public ServingFragment getServingFragment(int position){
		return currentFrag;
//		return fragments[position];
	}
	
	@Override
	public Fragment getItem(int position) {
		Log.v("ServingPagerAdapter", "getItem: " + position);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, position - 1);
		
		ServingFragment frag = ServingFragment.newInstance(cal.getTimeInMillis());
		currentFrag = frag;
		return frag;
//		return fragments[position];
	}
	
	public void setCount(int newCount){
		count = newCount;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return count;
	}

}
