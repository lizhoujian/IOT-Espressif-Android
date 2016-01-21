package com.espressif.iot.ui.device;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.espressif.iot.R;
import com.shizhefei.view.indicator.FragmentListPageAdapter;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.IndicatorViewPager.IndicatorFragmentPagerAdapter;
import com.shizhefei.view.indicator.IndicatorViewPager.OnIndicatorPageChangeListener;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;

public class DevicePlugsActivityTabs extends DeviceActivityAbs {
	protected static final String TAG = "DevicePlugsActivityTabs";
	private IndicatorViewPager indicatorViewPager;
	private LayoutInflater inflate;
	private String[] names = { "œµÕ≥…Ë÷√", "X", "Y", "M", "D" };
	private int[] namesType = { 0, Fx2nControl.REG_X, Fx2nControl.REG_Y,
			Fx2nControl.REG_M, Fx2nControl.REG_D };
	private ScrollIndicatorView indicator;

	private DevicePlugsActivityTabsFragmentBase currFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean compatibility = isDeviceCompatibility();
		checkHelpModePlugs(compatibility);
		if (compatibility) {
			executeGet();
		}
	}

	protected void checkHelpModePlugs(boolean compatibility) {
	}

	@Override
	protected View initControlView() {
		return inflatView();
	}

	@Override
	protected void executePrepare() {
	}

	@Override
	protected void executeFinish(int command, boolean result) {
		if (currFragment != null) {
			currFragment.executeFinish(command, result);
		}
	}

	public void setCurrentFragment(DevicePlugsActivityTabsFragmentBase f) {
		currFragment = f;
	}

	private View inflatView() {
		View view = View
				.inflate(this, R.layout.device_activity_plugs_tab, null);
		ViewPager viewPager = (ViewPager) view
				.findViewById(R.id.moretab_viewPager);
		indicator = (ScrollIndicatorView) view
				.findViewById(R.id.moretab_indicator);
		indicator.setScrollBar(new ColorBar(this, Color.RED, 5));

		int selectColorId = R.color.tab_top_text_2;
		int unSelectColorId = R.color.tab_top_text_1;
		indicator.setOnTransitionListener(new OnTransitionTextListener()
				.setColorId(this, selectColorId, unSelectColorId));

		viewPager.setOffscreenPageLimit(2);
		indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
		inflate = LayoutInflater.from(getApplicationContext());
		indicatorViewPager
				.setAdapter(new MyAdapter(getSupportFragmentManager()));
		indicatorViewPager
				.setOnIndicatorPageChangeListener(new OnIndicatorPageChangeListener() {
					@Override
					public void onIndicatorPageChange(int preItem,
							int currentItem) {
						Log.d(TAG, "onIndicatorPageChange, currentItem = "
								+ currentItem);
					}
				});
		indicator.setSplitAuto(true);
		return view;
	}

	public DevicePlugsActivityTabs getMainActivity() {
		return this;
	}

	private int size = names.length;

	class MyAdapter extends IndicatorFragmentPagerAdapter {

		public MyAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return size;
		}

		@Override
		public View getViewForTab(int position, View convertView,
				ViewGroup container) {
			if (convertView == null) {
				convertView = inflate.inflate(R.layout.tab_top, container,
						false);
			}
			TextView textView = (TextView) convertView;
			textView.setText(names[position % names.length]);
			textView.setPadding(20, 0, 20, 0);
			return convertView;
		}

		@Override
		public Fragment getFragmentForPage(int position) {
			DevicePlugsActivityTabsFragmentBase fragment;
			Bundle bundle = new Bundle();
			bundle.putInt(DevicePlugsActivityTabsFragmentBase.INTENT_INT_INDEX,
					position);
			if (position == 0) {
				fragment = new DevicePlugsActivityTabsFragmentSetting();
			} else {
				fragment = new DevicePlugsActivityTabsFragmentRegister();
				bundle.putInt(
						DevicePlugsActivityTabsFragmentBase.INTENT_REGISTER_TYPE,
						namesType[position]);
			}
			fragment.setArguments(bundle);
			fragment.setParentActivity(getMainActivity());
			return fragment;
		}

		@Override
		public int getItemPosition(Object object) {
			return FragmentListPageAdapter.POSITION_NONE;
		}

	};
}
