package com.kakao.example.android.activity;

import com.kakao.example.android.R;
import com.kakao.example.android.fragment.FriendListFragment;
import com.kakao.example.android.fragment.FriendsListFragment;
import com.kakao.example.android.fragment.SuperAwesomeCardFragment;
import com.kakao.example.android.widget.PagerSlidingTabStrip;
import com.kakao.example.android.widget.PagerSlidingTabStrip.IconTabProvider;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.widget.LinearLayout;

public class KakaoActivity extends FragmentActivity {

    private static final int[] ICONS = new int[] {
        R.drawable.tab_icon_friend,
        R.drawable.tab_icon_chatting,
        R.drawable.tab_icon_search,
        R.drawable.tab_icon_more,
    };

    private LinearLayout title;
	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		title = (LinearLayout) findViewById(R.id.ll_title);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager = (ViewPager) findViewById(R.id.pager);
		
		tabs.setTitleLayout(title);
		
		adapter = new MyPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);

		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
		pager.setPageMargin(pageMargin);
		pager.setPageMarginDrawable(R.color.page_margin_bg_color);
		
		tabs.setViewPager(pager);
	}

	public class MyPagerAdapter extends FragmentPagerAdapter implements IconTabProvider {
		static final int NUM_COUNT = 4;
		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUM_COUNT;
		}

		@Override
		public Fragment getItem(int position) {
			switch(position) {
			case 0:
				return new FriendListFragment();
//				return new FriendsListFragment();
			default:
				return SuperAwesomeCardFragment.newInstance(position);
			}
		}

		@Override
		public int getPageIconResId(int position) {
			return ICONS[position];
		}
	}
}