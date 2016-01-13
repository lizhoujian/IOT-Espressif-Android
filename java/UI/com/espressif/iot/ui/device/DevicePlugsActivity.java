package com.espressif.iot.ui.device;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.type.device.EspPlugsAperture;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class DevicePlugsActivity extends DeviceActivityAbs implements
		OnItemClickListener {
	private IEspDevicePlugs mDevicePlugs;

	protected ListView mApertureListView;
	private ApertureAdapter mApertureAdapter;
	private List<IAperture> mApertureList;

	private final String[] addrTypeitems = { "X", "Y", "S", "T", "C", "D",
			"D*", "M", "M*", "TV16", "CV16", "CV32" };
	private final int[] addrTypeValues = { Fx2nControl.REG_X,
			Fx2nControl.REG_Y, Fx2nControl.REG_S, Fx2nControl.REG_T,
			Fx2nControl.REG_C, Fx2nControl.REG_D, Fx2nControl.REG_DS,
			Fx2nControl.REG_M, Fx2nControl.REG_MS, Fx2nControl.REG_TV16,
			Fx2nControl.REG_CV16, Fx2nControl.REG_CV32 };
	private final int[] addrTypeValueLen = { 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 4 };

	private int bitAddrTypeValue = 0;
	private int bitSetValue = 0;
	private TextView txtBitAddrType;
	private Button btnBitAddrType;

	private RadioGroup radioSet;

	private Button btnBitExec;

	private int byteAddrTypeValue = 0;
	private TextView txtByteAddrType;
	private Button btnByteAddrType;

	private TextView txtByteWriteValue;

	private TextView txtByteReadValue;

	private Button btnByteReadExec;

	private Button btnByteWriteExec;

	private EditText txtBitAddr;
	private EditText txtByteAddr;

	private int byteLen;

	private Handler myHandler;

	private Button btnSerialSwitch;
	private Button btnRunStop;
	private Button btnLanIP;
	private TextView txtMsg;

	private boolean serialSwitchStatus = false;
	private boolean runStopStatus = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean compatibility = isDeviceCompatibility();
		checkHelpModePlugs(compatibility);
		if (compatibility) {
			executeGet();
		}
	}

	@Override
	protected View initControlView() {
		View view = View.inflate(this, R.layout.device_activity_plugs, null);

		mDevicePlugs = (IEspDevicePlugs) mIEspDevice;
		mApertureList = mDevicePlugs.getApertureList();
		mApertureListView = (ListView) view.findViewById(R.id.aperture_list);
		mApertureAdapter = new ApertureAdapter(this);
		mApertureListView.setAdapter(mApertureAdapter);
		mApertureListView.setOnItemClickListener(this);

		txtBitAddrType = (TextView) view.findViewById(R.id.txtBitAddrType);
		btnBitAddrType = (Button) view.findViewById(R.id.btnBitAddrType);
		btnBitAddrType.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						DevicePlugsActivity.this);
				builder.setIcon(R.drawable.app_icon);
				builder.setTitle("选择寄存器类型");
				builder.setItems(addrTypeitems,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								bitAddrTypeValue = addrTypeValues[which];
								txtBitAddrType.setText(addrTypeitems[which]);
							}
						});
				builder.show();
			}
		});
		txtBitAddr = (EditText) view.findViewById(R.id.txtBitAddr);
		radioSet = (RadioGroup) view.findViewById(R.id.radioBitSet);
		radioSet.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioBitSetValue) {
					bitSetValue = 1;
				} else if (checkedId == R.id.radioBitResetValue) {
					bitSetValue = 0;
				}
			}
		});
		btnBitExec = (Button) view.findViewById(R.id.btnBitExec);
		btnBitExec.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				executeBitControl();
			}
		});

		txtByteAddrType = (TextView) view.findViewById(R.id.txtByteAddrType);
		btnByteAddrType = (Button) view.findViewById(R.id.btnByteAddrType);
		btnByteAddrType.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						DevicePlugsActivity.this);
				builder.setIcon(R.drawable.app_icon);
				builder.setTitle("选择寄存器类型");
				builder.setItems(addrTypeitems,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								byteAddrTypeValue = addrTypeValues[which];
								byteLen = addrTypeValueLen[which];
								txtByteAddrType.setText(addrTypeitems[which]);
							}
						});
				builder.show();
			}
		});

		txtByteAddr = (EditText) view.findViewById(R.id.txtByteAddr);
		txtByteWriteValue = (TextView) view
				.findViewById(R.id.txtByteWriteValue);
		txtByteReadValue = (TextView) view.findViewById(R.id.txtByteReadValue);
		btnByteReadExec = (Button) view.findViewById(R.id.btnByteReadExec);
		btnByteReadExec.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				executeByteControl(0);
			}
		});
		btnByteWriteExec = (Button) view.findViewById(R.id.btnByteWriteExec);
		btnByteWriteExec.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				executeByteControl(1);
			}
		});

		btnSerialSwitch = (Button) view.findViewById(R.id.btnSerialSwitch);
		btnSerialSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				executeSerialSwitch(serialSwitchStatus ? 0 : 1);
			}
		});
		btnRunStop = (Button) view.findViewById(R.id.btnRunStop);
		btnRunStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				executeRunStop(runStopStatus ? 0 : 1);
			}
		});
		btnLanIP = (Button) view.findViewById(R.id.btnLanIP);
		btnLanIP.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exeucteGetLanIP();
			}
		});

		txtMsg = (TextView) view.findViewById(R.id.txtMsg);
		txtMsg.setText("");

		myHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Fx2nControl.REQUEST_CONTROL:
					txtByteReadValue.setText((String) msg.obj);
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
						btnRunStop.setText("PLC停步");
					} else {
						txtMsg.setText("PLC停步运行");
						btnRunStop.setText("PLC运行");
					}
					break;
				}
				super.handleMessage(msg);
			}
		};

		Fx2nControl.setHandler(myHandler);
		myHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				executeSerialSwitchGet();
				executeRunStopGet();
			}
		}, 200);

		return view;
	}

	private void executeBitControl() {
		int bitAddrType = bitAddrTypeValue;
		int bitAddr = 0;
		int bitValue = bitSetValue;
		IEspStatusPlugs status = new EspStatusPlugs();
		txtMsg.setText("");
		try {
			if (!txtBitAddr.getText().toString().trim().isEmpty()) {
				bitAddr = Integer.parseInt(txtBitAddr.getText().toString());
			}
		} catch (Exception e) {
		}
		status.setControlParam("control",
				bitValue > 0 ? Fx2nControl.CMD_FORCE_ON
						: Fx2nControl.CMD_FORCE_OFF, bitAddrType, bitAddr, "",
				0);
		executePost(status);
	}

	private void executeByteControl(int readWrite) {
		int byteAddrType = byteAddrTypeValue;
		int byteAddr = 0;
		int byteWriteValue = 0;

		txtMsg.setText("");
		try {
			if (!txtByteAddr.getText().toString().trim().isEmpty()) {
				byteAddr = Integer.parseInt(txtByteAddr.getText().toString());
			} else {
				byteAddr = 0;
			}
			if (!txtByteWriteValue.getText().toString().trim().isEmpty()) {
				byteWriteValue = Integer.parseInt(txtByteWriteValue.getText()
						.toString());
			} else {
				byteWriteValue = 0;
			}
		} catch (Exception e) {
		}
		String hexString = "";
		if (readWrite > 0) {
			hexString = Fx2nControl.toHexString(byteWriteValue, byteLen);
		}

		IEspStatusPlugs status = new EspStatusPlugs();
		status.setControlParam("control", readWrite > 0 ? Fx2nControl.CMD_WRITE
				: Fx2nControl.CMD_READ, byteAddrType, byteAddr, hexString,
				byteLen);
		executePost(status);
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
	protected void executePrepare() {
	}

	@Override
	protected void executeFinish(int command, boolean result) {
		mApertureAdapter.notifyDataSetChanged();

		if (command == COMMAND_GET && !result) {
			Toast.makeText(this, R.string.esp_device_plugs_get_status_failed,
					Toast.LENGTH_LONG).show();
		} else if (result && Fx2nControl.getLastStatus().getResult() > 0) {

		}

		checkHelpExecuteFinish(command, result);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		boolean isOn = !mApertureList.get(position).isOn();

		IEspStatusPlugs status = new EspStatusPlugs();
		List<IAperture> statusApertureList = new ArrayList<IAperture>();
		for (int i = 0; i < mApertureList.size(); i++) {
			IAperture aperture = mApertureList.get(i);
			IAperture statusAperture = new EspPlugsAperture(aperture.getId());
			statusAperture.setOn(i == position ? isOn : aperture.isOn());

			statusApertureList.add(statusAperture);
		}
		status.setStatusApertureList(statusApertureList);

		executePost(status);
	}

	private class ViewHolder {
		ImageView icon;

		TextView title;

		TextView notes;

		ImageView status;
	}

	private class ApertureAdapter extends BaseAdapter {
		private Activity mActivity;

		public ApertureAdapter(Activity activity) {
			mActivity = activity;
		}

		@Override
		public int getCount() {
			return mApertureList.size();
		}

		@Override
		public IAperture getItem(int position) {
			return mApertureList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			if (convertView == null) {
				view = mActivity.getLayoutInflater().inflate(
						R.layout.device_plugs_aperture, parent, false);
				holder = new ViewHolder();
				holder.icon = (ImageView) view.findViewById(R.id.aperture_icon);
				holder.title = (TextView) view
						.findViewById(R.id.aperture_title);
				holder.notes = (TextView) view
						.findViewById(R.id.aperture_notes);
				holder.status = (ImageView) view
						.findViewById(R.id.aperture_status);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}

			IAperture aperture = getItem(position);
			holder.icon
					.setBackgroundResource(R.drawable.esp_icon_plugs_aperture);
			holder.title.setText(aperture.getTitle());
			int statusIcon = aperture.isOn() ? R.drawable.esp_plug_small_on
					: R.drawable.esp_plug_small_off;
			holder.status.setBackgroundResource(statusIcon);
			holder.notes.setVisibility(View.GONE);

			return view;
		}

	}

	protected void checkHelpModePlugs(boolean compatibility) {
	}

	protected void checkHelpExecuteFinish(int command, boolean result) {
	}
}
