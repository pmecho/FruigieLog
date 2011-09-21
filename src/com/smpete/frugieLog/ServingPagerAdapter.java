package com.smpete.frugieLog;

import java.util.Calendar;
import java.util.Date;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
public class ServingPagerAdapter extends
		android.support.v4.app.FragmentPagerAdapter {

	private Date date;
	
	public ServingPagerAdapter(FragmentManager fm, Date date) {
		super(fm);
		this.date = date;
		// TODO Auto-generated constructor stub
	}

	public void setDate(Date date){
		this.date = date;
		//TODO Reload objects based on new date?
	}
	
	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, position - 1);
		
		ServingFragment frag = new ServingFragment(cal.getTime());
		return frag;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3;
	}

}
