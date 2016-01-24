package com.espressif.iot.ui.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.espressif.iot.R;
import com.espressif.iot.db.IOTRegisterDBManager;
import com.espressif.iot.db.greenrobot.daos.RegisterDB;
import com.espressif.iot.object.db.IRegisterDB;
import com.espressif.iot.type.device.EspPlugsAperture;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

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
		DevicePlugsActivityTabsFragmentBase {
	private static final String TAG = "DevicePlugsActivityTabsFragmentRegister";

	private TextView txtMsg;

	protected ListView mListView;
	private ListAdapter mAdapter;
	private List<IListItem> mList = new Vector<IListItem>();
	private PullToRefreshListView mPullRefreshListView;

	private String addrTypeName = "";
	private int addrType = 0;
	private int regIndex = 0;
	private int regBitCount = 16;
	private int mByteLen = 0;
	private boolean mBitMode = true;

	private boolean isAppendList = false;

	private int totalPage = 2;
	private int currentPage = 0;
	private int countPerPage = 16;

	private void refreshPaging() {
		int lines;
		if (mBitMode) {
			lines = regBitCount / 1;
		} else {
			lines = regBitCount / (mByteLen * 8);
		}
		totalPage = lines / countPerPage;
		if ((lines % countPerPage) > 0) {
			totalPage++;
		}
	}

	private int getCurrentStartAddr() {
		if (mBitMode) {
			return (currentPage * countPerPage) / 8;
		} else {
			return currentPage * countPerPage;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String v;
			switch (msg.what) {
			case Fx2nControl.REQUEST_CONTROL:
				v = (String) msg.obj;
				if (!v.isEmpty()) {
					parseRegValues(v);
				}
				break;
			case Fx2nControl.REQUEST_PLC_REGISTER_COUNT:
				v = (String) msg.obj;
				if (!v.isEmpty()) {
					regBitCount = Integer.parseInt(v);
					refreshPaging();
				}
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
		mPullRefreshListView.onRefreshComplete();
	}

	private void parseRegValues(String regValues) {
		if (mBitMode) {
			parseRegValuesBit(regValues);
		} else {
			parseRegValuesByte(regValues);
		}
	}

	private String getItemName(int addr, int addrType) {
		String origName = addrTypeName + addr;
		IRegisterDB r = IOTRegisterDBManager.getInstance().find(addrType, addr);
		if (r != null) {
			return r.getRegName();
		} else {
			return "";
		}
	}

	private void parseRegValuesBit(String regValues) {
		boolean[] bits = Fx2nControl.bytesToBits(Fx2nControl
				.hexStringToBytes(regValues));
		int rows = regBitCount;
		int leftRows = 0;
		int addr = 0;
		int i;
		if (bits == null) {
			Log.e(TAG, "parseRegValuesBit bits is null.");
			return;
		}
		if (bits.length > countPerPage) {
			Log.w(TAG, "parseRegValues bits.length=" + bits.length
					+ ",countPerPage=" + countPerPage);
		}

		if (!isAppendList) {
			mList.clear();
			isAppendList = false;
		}
		addr = mList.size();
		if (mList.size() + countPerPage > rows) {
			leftRows = rows - mList.size();
		} else {
			leftRows = countPerPage;
		}
		if (leftRows > bits.length) {
			leftRows = bits.length;
		}
		for (i = 0; i < leftRows; i++) {
			IListItem aperture = new RegListItem(addr);
			aperture.setTitle(getItemName(addr, addrType));
			aperture.setValue(bits[i] ? 1 : 0);
			mList.add(aperture);
			addr++;
		}

		mAdapter.notifyDataSetChanged();
	}

	private void parseRegValuesByte(String regValues) {
		int rows = regBitCount / (mByteLen * 8);
		int leftRows = 0;
		int i;
		int addr = 0;
		int[] values = Fx2nControl.hexStringToInt(regValues, mByteLen);
		if (values == null) {
			Log.e(TAG, "parseRegValuesByte values is null.");
			return;
		}
		if (values.length > countPerPage) {
			Log.w(TAG, "parseRegValues values.length=" + values.length
					+ ",countPerPage=" + countPerPage);
		}

		if (!isAppendList) {
			mList.clear();
			isAppendList = false;
		}
		addr = mList.size();
		if (mList.size() + countPerPage > rows) {
			leftRows = rows - mList.size();
		} else {
			leftRows = countPerPage;
		}
		if (leftRows > values.length) {
			leftRows = values.length;
		}
		for (i = 0; i < leftRows; i++) {
			IListItem aperture = new RegListItem(addr);
			aperture.setTitle(getItemName(addr, addrType));
			aperture.setValue(values[i]);
			mList.add(aperture);
			addr++;
		}

		mAdapter.notifyDataSetChanged();
	}

	private void showRenameDialog(final IListItem item) {
		final EditText title = new EditText(daa);
		title.setSingleLine();
		title.setText(item.getTitle());
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		title.setLayoutParams(lp);
		new AlertDialog.Builder(daa)
				.setView(title)
				.setTitle("重命名")
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								RegisterDB r = new RegisterDB();
								r.setId(0);
								r.setRegType(addrType);
								r.setRegAddr(item.getId());
								r.setRegName(title.getText().toString());
								IOTRegisterDBManager.getInstance()
										.insertOrReplace(r);
								item.setTitle(r.getRegName());
							}

						}).show();
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

	private void onListItemLongClick(AdapterView<?> parent, View view,
			int _position, long id) {
		if (_position > 0) {
			_position--;
		}
		Log.d(TAG, "current long click item = " + _position);
		final int position = _position;
		final IListItem item = mList.get(position);
		showRenameDialog(item);
		mAdapter.notifyDataSetChanged();
	}

	private void onListItemClick(AdapterView<?> parent, View view,
			int _position, long id) {
		if (addrType == Fx2nControl.REG_X) {
			return;
		}
		if (_position > 0) {
			_position--;
		}
		Log.d(TAG, "current click item = " + _position);
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

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// String label = DateUtils.formatDateTime(
						// getApplicationContext(),
						// System.currentTimeMillis(),
						// DateUtils.FORMAT_SHOW_TIME
						// | DateUtils.FORMAT_SHOW_DATE
						// | DateUtils.FORMAT_ABBREV_ALL);
						// // Update the LastUpdatedLabel
						// refreshView.getLoadingLayoutProxy()
						// .setLastUpdatedLabel(label);
						currentPage = 0;
						executeByteControlRead(0, countPerPage);
						isAppendList = false;
					}
				});
		mPullRefreshListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
					@Override
					public void onLastItemVisible() {
						if (!isAppendList) {
							Toast.makeText(daa, "上拉刷新", Toast.LENGTH_SHORT)
									.show();
							isAppendList = true;
							return;
						}
						if (currentPage < totalPage) {
							currentPage++;
							executeByteControlRead(getCurrentStartAddr(),
									countPerPage);
						}
					}
				});

		mListView = (ListView) mPullRefreshListView.getRefreshableView();
		// Need to use the Actual ListView when registering for Context Menu
		registerForContextMenu(mListView);

		mAdapter = new ListAdapter(daa);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onListItemClick(parent, view, position, id);
			}
		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				onListItemLongClick(parent, view, position, id);
				return true;
			}
		});

		txtMsg.setText("");
		setHandler(handler);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				executeRegisterCountGet();
				executeByteControlRead(0, countPerPage);
			}
		}, 10);
	}

	private void executeRegisterCountGet() {
		int bitAddrType = addrType;
		IEspStatusPlugs status = new EspStatusPlugs();
		status.setAction("reg_bits");
		status.setCmd(bitAddrType);
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

	private void executeByteControlRead(int addr, int rows) {
		executeByteControl(addr, rows, false, 0);
	}

	private void executeByteControlWrite(int addr, int writeValue) {
		executeByteControl(addr, 1, true, writeValue);
	}

	private void executeByteControl(int addr, int count, boolean isWrite,
			int writeValue) {
		int byteAddrType = addrType;
		int byteAddr = addr;
		int byteWriteValue = writeValue;
		int byteLen;
		String hexString = "";

		if (mBitMode) {
			byteLen = count / 8;
			if (count % 8 > 0) {
				byteLen++;
			}
		} else {
			byteLen = count * mByteLen;
		}

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

			IListItem item = getItem(position);
			holder.icon
					.setBackgroundResource(R.drawable.esp_icon_plugs_aperture);
			holder.title.setText(item.getTitle().isEmpty() ? addrTypeName
					+ item.getId() : item.getTitle() + "("
					+ addrTypeName + item.getId() + ")");
			if (mBitMode) {
				int statusIcon = item.getValue() > 0 ? R.drawable.esp_plug_small_on
						: R.drawable.esp_plug_small_off;
				holder.status.setBackgroundResource(statusIcon);
				holder.status.setVisibility(View.VISIBLE);
			} else {
				long ul = item.getValue();
				holder.statusText.setText(Long.toString(ul));
				holder.statusText.setVisibility(View.VISIBLE);
			}
			holder.notes.setVisibility(View.GONE);
			return view;
		}
	}
}
