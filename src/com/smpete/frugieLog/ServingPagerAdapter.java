package com.smpete.frugieLog;

import java.util.Calendar;
import java.util.Date;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
public class ServingPagerAdapter extends
		android.support.v4.app.FragmentPagerAdapter {

	
	public ServingPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		Date date = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, position - 5);
		
		ServingFragment frag = new ServingFragment(cal.getTime());
		return frag;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 11;
	}
	



}
