package com.zhan_dui.dictionary.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

public class ViewPagerAdapter extends PagerAdapter {

	private List<View> pageViews = new ArrayList<View>();

	@Override
	public Object instantiateItem(View container, int position) {
		((ViewPager) container).addView(pageViews.get(position), 0);
		return pageViews.get(position);
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(pageViews.get(position));
	}

	public void clear() {
		pageViews.clear();
	}

	public void replaceItem(View replace, View toReplace) {
		int replacePosition = pageViews.indexOf(replace);
		pageViews.add(replacePosition, toReplace);
		pageViews.remove(replacePosition + 1);
	}
	@Override
	public int getCount() {
		return pageViews.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	public void setPageViews(List<View> allViews) {
		this.pageViews = allViews;
	}

	public void addPageView(View view) {
		this.pageViews.add(view);
	}

	public void addPageView(Context context, int resId) {
		this.pageViews.add(LayoutInflater.from(context).inflate(resId, null));
	}
}
