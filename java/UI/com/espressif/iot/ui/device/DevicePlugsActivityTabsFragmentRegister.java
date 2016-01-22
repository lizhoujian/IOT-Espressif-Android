package com.espressif.iot.ui.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.espressif.iot.R;
import com.espressif.iot.type.device.EspPlugsAperture;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

interface IListItem {
	int getId();

	void setTitle(String title);

	String getTitle();

	void setValue(int v);

	int getValue();
}

class RegListItem implements IListItem {
	int id;
	String title;
	int v;

	RegListItem() {
		setId(0);
	}

	RegListItem(int id) {
		setId(id);
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public void setValue(int v) {
		this.v = v;
	}

	@Override
	public int getValue() {
		return this.v;
	}
}

public class DevicePlugsActivityTabsFragmentRegister extends
		DevicePlugsActivityTabsFragmentBase implements
		OnRefreshListener<ScrollView> {
	private static final String TAG = "DevicePlugsActivityTabsFragmentRegister";

	private TextView txtMsg;

	protected ListView mListView;
	private ListAdapter mAdapter;
	private List<IListItem> mList = new Vector<IListItem>();
	private PullToRefreshScrollView mPullRereshScorllView;

	private String addrTypeName = "";
	private int addrType = 0;
	private int regIndex = 0;
	private int regCount = 2;
	private int mByteLen = 0;
	private boolean mBitMode = true;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String v;
			switch (msg.what) {
			case Fx2nControl.REQUEST_CONTROL:
				v = (String) msg.obj;
				parseRegValues(v);
				break;
			case Fx2nControl.REQUEST_PLC_REGISTER_COUNT:
				v = (String) msg.obj;
				regCount = Integer.parseInt(v);
				executeByteControlRead(0); // reload
				break;
			}
			super.handleMessage(msg);
		}
	};

	private int findRegIndexByType(int type) {
		for (int i = 0; i < Fx2nControl.addrTypeValues.length; i++) {
			if (Fx2nControl.addrTypeValues[i] == type)
				return i;
		}
		return 0;
	}

	@Override
	public void executeFinish(int command, boolean result) {
		mAdapter.notifyDataSetChanged();
		if (command == 1/* COMMAND_POST */&& !result) {
			Toast.makeText(daa, R.string.esp_device_plugs_get_status_failed,
					Toast.LENGTH_LONG).show();
		}
		mPullRereshScorllView.onRefreshComplete();
	}

	private void parseRegValues(String regValues) {
		if (mBitMode) {
			parseRegValuesBit(regValues);
		} else {
			parseRegValuesByte(regValues);
		}
	}

	private void parseRegValuesBit(String regValues) {
		boolean[] bits = Fx2nControl.bytesToBits(Fx2nControl
				.hexStringToBytes(regValues));
		int bitsLen = regCount * mByteLen * 8;
		int i;
		if (bits == null) {
			Log.e(TAG, "parseRegValuesBit bits is null.");
			return;
		}
		if (bits.length != bitsLen) {
			Log.w(TAG, "parseRegValues bits.length=" + bits.length
					+ ",bitsLen=" + bitsLen);
		}

		mList.clear();
		for (i = 0; i < bits.length; i++) {
			IListItem aperture = new RegListItem(i);
			aperture.setTitle(addrTypeName + i);
			aperture.setValue(bits[i] ? 1 : 0);
			mList.add(aperture);
		}

		mAdapter.notifyDataSetChanged();
	}

	private void parseRegValuesByte(String regValues) {
		int i;
		int[] values = Fx2nControl.hexStringToInt(regValues, mByteLen);
		if (values == null) {
			Log.e(TAG, "parseRegValuesByte values is null.");
			return;
		}
		if (values.length != regCount) {
			Log.w(TAG, "parseRegValues values.length=" + values.length
					+ ",regCount=" + regCount);
		}

		mList.clear();
		for (i = 0; i < values.length; i++) {
			IListItem aperture = new RegListItem(i);
			aperture.setTitle(addrTypeName + i);
			aperture.setValue(values[i]);
			mList.add(aperture);
		}

		mAdapter.notifyDataSetChanged();
	}

	private void showEditDialog(final IListItem item) {
		final EditText nameEdit = new EditText(daa);
		nameEdit.setSingleLine();
		nameEdit.setText(Integer.toString(item.getValue()));
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		nameEdit.setLayoutParams(lp);
		new AlertDialog.Builder(daa)
				.setView(nameEdit)
				.setTitle(item.getTitle())
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String editValue = nameEdit.getText()
										.toString();
								int iValue = Integer.parseInt(editValue);
								executeByteControlWrite(item.getId(), iValue);
								item.setValue(iValue);
							}

						}).show();
	}

	private void onListItemClick(AdapterView<?> parent, View view,
			int _position, long id) {
		final int position = _position;
		final IListItem item = mList.get(position);
		final int v = item.getValue();
		final int addr = item.getId();
		if (mBitMode) {
			int newValue = v > 0 ? 0 : 1;
			executeBitControl(addr, newValue > 0);
			item.setValue(newValue);
		} else {
			showEditDialog(item);
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_tabmain_item_register);
		tabIndex = getArguments().getInt(INTENT_INT_INDEX);
		regIndex = findRegIndexByType(getArguments().getInt(
				INTENT_REGISTER_TYPE));
		addrType = Fx2nControl.addrTypeValues[regIndex];
		addrTypeName = Fx2nControl.addrTypeitems[regIndex];
		mByteLen = Fx2nControl.addrTypeValueLen[regIndex];

		if (addrType == Fx2nControl.REG_D) {
			mBitMode = false;
		} else {
			mBitMode = true;
		}

		txtMsg = (TextView) findViewById(R.id.txtDesc);

		mListView = (ListView) findViewById(R.id.aperture_list);
		mAdapter = new ListAdapter(daa);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onListItemClick(parent, view, position, id);
			}
		});

		mPullRereshScorllView = (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_scrollview);
		mPullRereshScorllView.setOnRefreshListener(this);
		mPullRereshScorllView.setScrollingWhileRefreshingEnabled(true);
		txtMsg.setText("");
		setHandler(handler);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				executeRegisterCountGet();
				executeByteControlRead(0);
			}
		}, 10);
	}

	private void executeRegisterCountGet() {
		int bitAddrType = addrType;
		IEspStatusPlugs status = new EspStatusPlugs();
		status.setControlParam("register_count", 0, bitAddrType, 0, "", 0);
		executePost(status);
	}

	private void executeBitControl(int addr, boolean onoff) {
		int bitAddrType = addrType;
		int bitAddr = addr;
		IEspStatusPlugs status = new EspStatusPlugs();
		status.setControlParam("control", onoff ? Fx2nControl.CMD_FORCE_ON
				: Fx2nControl.CMD_FORCE_OFF, bitAddrType, bitAddr, "", 0);
		executePost(status);
	}

	private void executeByteControlRead(int addr) {
		executeByteControl(addr, regCount, false, 0);
	}

	private void executeByteControlWrite(int addr, int writeValue) {
		executeByteControl(addr, 1, true, writeValue);
	}

	private void executeByteControl(int addr, int count, boolean isWrite,
			int writeValue) {
		int byteAddrType = addrType;
		int byteAddr = addr;
		int byteWriteValue = writeValue;
		int byteLen = mByteLen * count;
		String hexString = "";

		if (isWrite) {
			hexString = Fx2nControl.toHexString(byteWriteValue, byteLen);
		}

		IEspStatusPlugs status = new EspStatusPlugs();
		status.setControlParam("control", isWrite ? Fx2nControl.CMD_WRITE
				: Fx2nControl.CMD_READ, byteAddrType, byteAddr, hexString,
				byteLen);
		executePost(status);
	}

	@Override
	public void onDestroyViewLazy() {
		super.onDestroyViewLazy();
	}

	private class ViewHolder {
		ImageView icon;
		TextView title;
		TextView notes;
		ImageView status;
		TextView statusText;
	}

	private class ListAdapter extends BaseAdapter {
		private Activity mActivity;

		public ListAdapter(Activity activity) {
			mActivity = activity;
		}

		@Override
		public int getCount() {
			int size = mList.size();
			return size;
		}

		@Override
		public IListItem getItem(int position) {
			return mList.get(position);
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
						R.layout.device_plugs_aperture_tab, parent, false);
				holder = new ViewHolder();
				holder.icon = (ImageView) view.findViewById(R.id.aperture_icon);
				holder.title = (TextView) view
						.findViewById(R.id.aperture_title);
				holder.notes = (TextView) view
						.findViewById(R.id.aperture_notes);
				holder.status = (ImageView) view
						.findViewById(R.id.aperture_status);
				holder.statusText = (TextView) view
						.findViewById(R.id.aperture_status_text);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}

			IListItem aperture = getItem(position);
			holder.icon
					.setBackgroundResource(R.drawable.esp_icon_plugs_aperture);
			holder.title.setText(aperture.getTitle());
			if (mBitMode) {
				int statusIcon = aperture.getValue() > 0 ? R.drawable.esp_plug_small_on
						: R.drawable.esp_plug_small_off;
				holder.status.setBackgroundResource(statusIcon);
				holder.status.setVisibility(View.VISIBLE);
			} else {
				holder.statusText
						.setText(Integer.toString(aperture.getValue()));
				holder.statusText.setVisibility(View.VISIBLE);
			}
			holder.notes.setVisibility(View.GONE);
			return view;
		}
	}

	@Override
	public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
		executeRegisterCountGet();
		executeByteControlRead(0);
	}
}
