package com.smpete.frugieLog;

import java.util.Calendar;
import java.util.Date;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class ServingPagerAdapter extends
		android.support.v4.app.FragmentPagerAdapter {

	private static final int COUNT = 11;
	private Date date;
	private static ServingFragment[] fragments = new ServingFragment[COUNT];
	
	public ServingPagerAdapter(FragmentManager fm, Date date) {
		super(fm);
		this.date = date;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, -1);
		
	}

	public void setDate(Date date){
		this.date = date;
		//TODO Reload objects based on new date?
	}
	
	public void setServingSize(boolean half){
		for(ServingFragment frag : fragments){
			frag.setHalfServing(half);
		}
	}
	
	public ServingFragment getServingFragment(int position){
		return fragments[position];
	}
	
	@Override
	public Fragment getItem(int position) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, position - 5);
		
		ServingFragment frag = ServingFragment.newInstance(cal.getTimeInMillis());
		fragments[position] = frag;
		return frag;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return COUNT;
	}

}
