package com.espressif.iot.ui.device;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.espressif.iot.R;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.shizhefei.fragment.LazyFragment;

public abstract class DevicePlugsActivityTabsFragmentBase extends LazyFragment {
	protected int tabIndex;
	protected DevicePlugsActivityTabs daa;
	public static final String INTENT_INT_INDEX = "intent_int_index";
	public static final String INTENT_REGISTER_TYPE = "intent_register_type";

	public void setParentActivity(DevicePlugsActivityTabs daa) {
		this.daa = daa;
	}

	public void executePost(IEspStatusPlugs status) {
		daa.setCurrentFragment(this);
		daa.executePost(status);
	}

	public abstract void executeFinish(int command, boolean result);

}
