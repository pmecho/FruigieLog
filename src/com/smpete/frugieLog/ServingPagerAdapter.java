package com.smpete.frugieLog;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
public class ServingPagerAdapter extends
		android.support.v4.app.FragmentStatePagerAdapter {

	
	public ServingPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		ServingFragment frag = new ServingFragment();
		return frag;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3;
	}
	



}
