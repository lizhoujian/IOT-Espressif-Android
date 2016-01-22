package com.espressif.iot.ui.device;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.espressif.iot.R;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.shizhefei.fragment.LazyFragment;

public class DevicePlugsActivityTabsFragmentSetting extends
		DevicePlugsActivityTabsFragmentBase {

	private Button btnSerialSwitch;
	private Button btnRunStop;
	private Button btnLanIP;
	private boolean serialSwitchStatus = false;
	private boolean runStopStatus = false;

	private TextView txtMsg;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Fx2nControl.REQUEST_CONTROL:
				break;
			case Fx2nControl.REQUEST_LAN_IP:
				txtMsg.setText((String) msg.obj);
				break;
			case Fx2nControl.REQUEST_SERIAL_SWITCH:
				serialSwitchStatus = msg.arg1 == 1;
				if (serialSwitchStatus) {
					txtMsg.setText("PLC串口已连接到WIFI");
					btnSerialSwitch.setText("PLC连接PC");
				} else {
					txtMsg.setText("PLC串口已连接到PC");
					btnSerialSwitch.setText("PLC连接WIFI");
				}
				break;
			case Fx2nControl.REQUEST_PLC_RUN_STOP:
				runStopStatus = msg.arg1 == 1;
				if (runStopStatus) {
					txtMsg.setText("PLC正常运行");
					btnRunStop.setText("PLC停止");
				} else {
					txtMsg.setText("PLC停止运行");
					btnRunStop.setText("PLC运行");
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void executeFinish(int command, boolean result) {

	}

	@Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_tabmain_item_setting);
		tabIndex = getArguments().getInt(INTENT_INT_INDEX);
		txtMsg = (TextView) findViewById(R.id.txtDesc);

		btnSerialSwitch = (Button) findViewById(R.id.btnSerialSwitch);
		btnSerialSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				executeSerialSwitch(serialSwitchStatus ? 0 : 1);
			}
		});
		btnRunStop = (Button) findViewById(R.id.btnRunStop);
		btnRunStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				executeRunStop(runStopStatus ? 0 : 1);
			}
		});
		btnLanIP = (Button) findViewById(R.id.btnLanIP);
		btnLanIP.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exeucteGetLanIP();
			}
		});

		txtMsg.setText("setting " + tabIndex);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				executeSerialSwitchGet();
				executeRunStopGet();
			}
		}, 10);

		setHandler(handler);
	}

	private void executeRunStopGet() {
		txtMsg.setText("");
		IEspStatusPlugs status = new EspStatusPlugs();
		status.setAction("plc_run_stop_get");
		executePost(status);
	}

	private void executeRunStop(int onoff) {
		txtMsg.setText("");
		IEspStatusPlugs status = new EspStatusPlugs();
		status.setAction("plc_run_stop_set");
		status.setCmd(onoff);
		executePost(status);
	}

	private void executeSerialSwitch(int onoff) {
		txtMsg.setText("");
		IEspStatusPlugs status = new EspStatusPlugs();
		status.setAction("serial_switch_set");
		status.setCmd(onoff);
		executePost(status);
	}

	private void executeSerialSwitchGet() {
		txtMsg.setText("");
		IEspStatusPlugs status = new EspStatusPlugs();
		status.setAction("serial_switch_get");
		executePost(status);
	}

	private void exeucteGetLanIP() {
		txtMsg.setText("");
		IEspStatusPlugs status = new EspStatusPlugs();
		status.setAction("lan_ip");
		executePost(status);
	}

	@Override
	public void onDestroyViewLazy() {
		super.onDestroyViewLazy();
	}

}
