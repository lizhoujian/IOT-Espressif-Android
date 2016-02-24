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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
	void setItemId(int itemId);

	int getItemId();

	int getId();

	void setTitle(String title);

	String getTitle();

	void setValue(int v);

	int getValue();

	void setSpinned(boolean spinned);

	boolean isSpinned();
}

class RegListItem implements IListItem {
	int itemId;
	int id;
	String title;
	int v;
	boolean spinned;

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

	@Override
	public boolean isSpinned() {
		return this.spinned;
	}

	@Override
	public void setSpinned(boolean spinned) {
		this.spinned = spinned;
	}

	@Override
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	@Override
	public int getItemId() {
		return this.itemId;
	}
}

public class DevicePlugsActivityTabsFragmentRegister extends
		DevicePlugsActivityTabsFragmentBase {
	private static final String TAG = "DevicePlugsActivityTabsFragmentRegister";

	private TextView txtMsg;

	protected ListView mListView;
	private ListAdapter mAdapter;
	private List<IListItem> mList = new Vector<IListItem>();
	private List<IListItem> mSpinnedList = new Vector<IListItem>();
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

	private int getCurrentPage() {
		return (currentPage - spinnedPages());
	}

	private int refreshPaging() {
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
		totalPage += mSpinnedList.size() / countPerPage;
		if ((mSpinnedList.size() % countPerPage) > 0) {
			totalPage++;
		}
		return totalPage;
	}

	private int getCurrentStartAddr() {
		if (mBitMode) {
			return (getCurrentPage() * countPerPage) / 8;
		} else {
			return getCurrentPage() * countPerPage;
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
		IRegisterDB r = IOTRegisterDBManager.getInstance().find(addrType, addr,
				false);
		if (r != null) {
			return r.getRegName();
		} else {
			return "";
		}
	}

	private boolean getIsSpinned(int addr, int addrType) {
		return IOTRegisterDBManager.getInstance().find(addrType, addr, true) != null;
	}

	private void parseRegValuesBit(String regValues) {
		boolean[] bits = Fx2nControl.bytesToBits(Fx2nControl
				.hexStringToBytes(regValues));
		int rows = regBitCount;
		int leftRows = 0;
		int addr = 0;
		int i;
		int startLoc;

		if (bits == null) {
			Log.e(TAG, "parseRegValuesBit bits is null.");
			return;
		}

		if (!isAppendList) {
			mList.clear();
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
		if (currentPage == spinnedPages() + 1) { // need fill list
			leftRows = countPerPage - spinnedModCount();
			startLoc = 0;
		} else {
			startLoc = spinnedModCount();
		}

		if (mList.size() + leftRows > rows) {
			leftRows = rows - mList.size();
		}

		for (i = startLoc; i < leftRows; i++) {
			IListItem item = new RegListItem(addr);
			item.setItemId(addr);
			item.setTitle(getItemName(addr, addrType));
			item.setValue(bits[startLoc] ? 1 : 0);
			item.setSpinned(getIsSpinned(addr, addrType));
			mList.add(item);
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
		int startLoc;

		if (values == null) {
			Log.e(TAG, "parseRegValuesByte values is null.");
			return;
		}

		if (!isAppendList) {
			mList.clear();
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
		if (currentPage == spinnedPages() + 1) { // need fill list
			leftRows = countPerPage - spinnedModCount();
			startLoc = 0;
		} else {
			startLoc = spinnedModCount();
		}

		if (mList.size() + leftRows > rows) {
			leftRows = rows - mList.size();
		}

		for (i = startLoc; i < leftRows; i++) {
			IListItem item = new RegListItem(addr);
			item.setItemId(addr);
			item.setTitle(getItemName(addr, addrType));
			item.setValue(values[i]);
			item.setSpinned(getIsSpinned(addr, addrType));
			mList.add(item);
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
								r.setIsSpinned(false);
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

	private IListItem findItem(int position) {
		if (position < mSpinnedList.size()) {
			return mSpinnedList.get(position);
		} else {
			return mList.get(position - mSpinnedList.size());
		}
	}

	private IListItem findItemInSpinned(int position) {
		if (position < mSpinnedList.size()) {
			return mSpinnedList.get(position);
		} else {
			return null;
		}
	}

	private IListItem findItemInList(int position) {
		if (position < mSpinnedList.size()) {
			return null;
		} else {
			return mList.get(position - mSpinnedList.size());
		}
	}

	private void onListItemLongClick(AdapterView<?> parent, View view,
			int _position, long id) {
		if (_position > 0) {
			_position--;
		}
		Log.d(TAG, "current long click item = " + _position);
		final int position = _position;
		final IListItem item = findItem(position);
		if (item != null) {
			showRenameDialog(item);
			mAdapter.notifyDataSetChanged();
		}
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
		final IListItem item = findItem(position);
		if (item != null) {
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
	}

	private int spinnedPages() {
		return mSpinnedList.size() / countPerPage;
	}

	private int spinnedModCount() {
		return mSpinnedList.size() % countPerPage;
	}

	private void loadSpinnedList() {
		mSpinnedList.clear();
		int i = 0;
		List<RegisterDB> list = IOTRegisterDBManager.getInstance()
				.findSpinnedBy(addrType);
		if (list != null) {
			for (RegisterDB r : list) {
				if (r != null) {
					int addr = r.getRegAddr();
					IListItem item = new RegListItem(addr);
					item.setItemId(i++);
					item.setTitle(getItemName(addr, addrType));
					item.setValue(0);
					item.setSpinned(getIsSpinned(addr, addrType));
					mSpinnedList.add(item);
				}
			}
		}
		refreshPaging();
	}

	private void removeFromSpinnedList(IListItem item) {
		if (mSpinnedList.isEmpty())
			return;
		if (true) {
			for (IListItem i : mSpinnedList) {
				if (i.getId() == item.getId()
						&& i.getTitle().equalsIgnoreCase(item.getTitle())) {
					mSpinnedList.remove(i);
					break;
				}
			}
		} else {
			mSpinnedList.remove(item);
		}
	}

	private void appendToSpinnedList(IListItem item) {
		mSpinnedList.add(item);
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
						mList.clear();
						if (mSpinnedList.size() < countPerPage) {
							executeByteControlRead(0, countPerPage);
							isAppendList = false;
						} else {
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									mPullRefreshListView.onRefreshComplete();
								}
							}, 100);
						}
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
						} else {
							if (currentPage < totalPage) {
								currentPage++;
								if (currentPage > spinnedPages()) {
									executeByteControlRead(
											getCurrentStartAddr(),
											countPerPage * 2);
								}
							}
						}
					}
				});

		mListView = (ListView) mPullRefreshListView.getRefreshableView();
		// mListView.setDivider(null);
		mListView.setDividerHeight(1);
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				loadSpinnedList();
			}
		}).start();
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

	private boolean viewCanVisible(int position) {
		if (position >= mSpinnedList.size()) {
			IListItem item = findItemInList(position);
			if (item != null) {
				for (IListItem i : mSpinnedList) {
					if (i.getId() == item.getId()
							&& i.getTitle().equalsIgnoreCase(item.getTitle())) {
						Log.d(TAG, "hide list item " + position + "");
						return false;
					}
				}
			}
		}
		return true;
	}

	private class ViewHolder {
		ImageView icon;
		TextView title;
		TextView notes;
		ImageView status;
		TextView statusText;
		Button spin;
		int viewHeight;
	}

	private class ListAdapter extends BaseAdapter {
		private Activity mActivity;

		public ListAdapter(Activity activity) {
			mActivity = activity;
		}

		@Override
		public int getCount() {
			int size = mList.size() + mSpinnedList.size();
			return size;
		}

		@Override
		public IListItem getItem(int position) {
			return findItem(position);
		}

		@Override
		public long getItemId(int position) {
			if (position < mSpinnedList.size()) {
				return mSpinnedList.get(position).getItemId();
			} else {
				return mList.get(position - mSpinnedList.size()).getItemId()
						+ mSpinnedList.size();
			}
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
				holder.icon.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = Integer.parseInt(v.getTag().toString());
						onListItemClick(null, v, pos + 1, 0);
					}
				});
				holder.title = (TextView) view
						.findViewById(R.id.aperture_title);
				holder.title.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = Integer.parseInt(v.getTag().toString());
						onListItemClick(null, v, pos + 1, 0);
					}
				});
				holder.notes = (TextView) view
						.findViewById(R.id.aperture_notes);
				holder.status = (ImageView) view
						.findViewById(R.id.aperture_status);
				holder.status.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = Integer.parseInt(v.getTag().toString());
						onListItemClick(null, v, pos + 1, 0);
					}
				});
				holder.statusText = (TextView) view
						.findViewById(R.id.aperture_status_text);
				holder.spin = (Button) view.findViewById(R.id.btn_spin);
				holder.spin.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Button btn = (Button) v;
						int pos = Integer.parseInt(v.getTag().toString());
						IListItem item = getItem(pos);
						if (item.isSpinned()) {
							IOTRegisterDBManager.getInstance().delete(addrType,
									item.getId(), true);
							btn.setText("固定");
							item.setSpinned(false);
							removeFromSpinnedList(item);
						} else {
							RegisterDB r = new RegisterDB();
							r.setId(0);
							r.setRegType(addrType);
							r.setRegAddr(item.getId());
							r.setRegName(item.getTitle());
							r.setIsSpinned(true);
							IOTRegisterDBManager.getInstance().insertOrReplace(
									r);
							btn.setText("取消");
							item.setSpinned(true);
							appendToSpinnedList(item);
						}
						mAdapter.notifyDataSetChanged();
					}
				});
				holder.viewHeight = view.getLayoutParams().height;
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}

			IListItem item = getItem(position);
			holder.icon
					.setBackgroundResource(R.drawable.esp_icon_plugs_aperture);
			holder.title.setText(item.getTitle().isEmpty() ? addrTypeName
					+ item.getId() : item.getTitle() + "(" + addrTypeName
					+ item.getId() + ")");
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
			if (item.isSpinned()) {
				holder.spin.setText("取消");
			} else {
				holder.spin.setText("固定");
			}
			holder.icon.setTag(position);
			holder.title.setTag(position);
			holder.status.setTag(position);
			holder.spin.setVisibility(View.VISIBLE);
			holder.spin.setTag(position);
			if (!viewCanVisible(position)) {
				LayoutParams linearParams = view.getLayoutParams();
				linearParams.height = 1;
				view.setLayoutParams(linearParams);
				view.setVisibility(View.GONE);
			} else {
				LayoutParams linearParams = view.getLayoutParams();
				linearParams.height = holder.viewHeight;
				view.setLayoutParams(linearParams);
				view.setVisibility(View.VISIBLE);
			}
			return view;
		}
	}
}
