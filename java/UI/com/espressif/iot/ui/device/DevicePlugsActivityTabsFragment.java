package com.espressif.iot.ui.device;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.espressif.iot.R;
import com.shizhefei.fragment.LazyFragment;

public class DevicePlugsActivityTabsFragment extends LazyFragment {
	private int tabIndex;
	public static final String INTENT_INT_INDEX = "intent_int_index";

	@Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_tabmain_item);
		tabIndex = getArguments().getInt(INTENT_INT_INDEX);
		TextView v = (TextView) findViewById(R.id.fragment_mainTab_item_textView);
		v.setText("load " + tabIndex);
	}

	@Override
	public void onDestroyViewLazy() {
		super.onDestroyViewLazy();
		handler.removeMessages(1);
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
		}
	};
}
