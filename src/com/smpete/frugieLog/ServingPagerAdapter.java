package com.smpete.frugieLog;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
public class ServingPagerAdapter extends
		android.support.v4.app.FragmentPagerAdapter {

	private Date date;
	private ServingFragment[] fragments = new ServingFragment[3];
	private Activity mActivity;
	
	public ServingPagerAdapter(Activity activity, FragmentManager fm, Date date) {
		super(fm);
		this.date = date;
		mActivity = activity;
		// TODO Auto-generated constructor stub
		
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(date);
//		cal.add(Calendar.DATE, -1);
//		
//		for(int i = 0; i < 3; i++, cal.add(Calendar.DATE, 1)){
//			fragments[i] = new ServingFragment(cal.getTime());
//		}

		
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
		// TODO Auto-generated method stub
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, position - 1);
		
		ServingFragment frag = new ServingFragment(cal.getTime());
		fragments[position] = frag;
		return frag;
		
//		return fragments[position];
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3;
	}

}
