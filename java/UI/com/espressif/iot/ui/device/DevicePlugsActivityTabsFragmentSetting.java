package com.espressif.iot.ui.device;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.espressif.iot.R;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.shizhefei.fragment.LazyFragment;

public class DevicePlugsActivityTabsFragmentSetting extends
		DevicePlugsActivityTabsFragmentBase {

	private TextView v;

	@Override
	public void executeFinish(int command, boolean result) {

	}

	@Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_tabmain_item);
		tabIndex = getArguments().getInt(INTENT_INT_INDEX);
		v = (TextView) findViewById(R.id.fragment_mainTab_item_textView);
		v.setText("setting " + tabIndex);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				IEspStatusPlugs status = new EspStatusPlugs();
				status.setAction("lan_ip");
				Fx2nControl.setHandler(handler);
				executePost(status);
			}
		}, 200);
	}

	@Override
	public void onDestroyViewLazy() {
		super.onDestroyViewLazy();
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Fx2nControl.REQUEST_LAN_IP:
				v.setText((String) msg.obj);
				break;
			}
			super.handleMessage(msg);
		}
	};
}
