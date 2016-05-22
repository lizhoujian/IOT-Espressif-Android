package com.espressif.iot.ui.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
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

	void setSynced(boolean synced);

	boolean synced();
}

class RegListItem implements IListItem {
	int itemId;
	int id;
	String title;
	int v;
	boolean spinned;
	boolean synced;

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

	@Override
	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	@Override
	public boolean synced() {
		return this.synced;
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

	private TextView txtAddrType;
	private EditText editTextAddrType;
	private Button btnAddrTypeAddr;

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

	private int listSize = 0;

	private int RefreshListSleepTime = 10;

	private boolean autoRefreshCompleteList = true;
	private boolean autoRefreshCompleteSpinnedList = true;

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
		if (mBitMode && Fx2nControl.REG_GPIO != addrType) {
			return (getCurrentPage() * countPerPage) / 8;
		} else {
			return getCurrentPage() * countPerPage;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String v;
			boolean isSpinned, refresh;
			Map<String, Object> o;
			int addr;
			switch (msg.what) {
			case Fx2nControl.REQUEST_CONTROL:
				o = (Map<String, Object>) msg.obj;
				v = o.get("value").toString();
				isSpinned = Boolean.valueOf(o.get("isSpinned").toString());
				refresh = Boolean.valueOf(o.get("forRefresh").toString());
				if (!v.isEmpty()) {
					if (!isSpinned) {
						if (refresh) {
							addr = Integer.parseInt(o.get("addr").toString());
						} else {
							addr = 0;
						}
						parseRegValues(v, addr, refresh);
					} else {
						addr = Integer.parseInt(o.get("addr").toString());
						parseRegValuesForSpinned(addr, v, refresh);
					}
				}
				break;
			case Fx2nControl.REQUEST_PLC_REGISTER_COUNT:
				v = (String) msg.obj;
				if (!v.isEmpty()) {
					regBitCount = Integer.parseInt(v);
					refreshPaging();
				}
				break;
			case Fx2nControl.REQUEST_GPIO_COUNT:
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

	private Button btnRefreshTimeSub;

	private TextView txtRefreshTime;

	private Button btnRefreshTimeAdd;

	private SharedPreferences settings;
	private int thisPageId = 0;

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
		if (command == 1/* COMMAND_POST */) {
			if (!result) {
				/*
				 * Toast.makeText(daa,
				 * R.string.esp_device_plugs_get_status_failed,
				 * Toast.LENGTH_LONG).show();
				 */
				Log.w(TAG, "execute COMMAND_POST failed.\n");
			}
			autoRefreshCompleteList = true;
			autoRefreshCompleteSpinnedList = true;
		}
		mPullRefreshListView.onRefreshComplete();
	}

	private void parseRegValues(String regValues, int addr_start,
			boolean refresh) {
		try {
			if (mBitMode) {
				parseRegValuesBit(regValues, addr_start, refresh);
			} else {
				parseRegValuesByte(regValues, addr_start, refresh);
			}
			if (refresh) {
				refreshListValues(refresh);
			}
		} catch (Exception e) {
			Log.e(TAG, "parseRegValues error! msg: " + e.getMessage());
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

	private IListItem findItemInSpinnedByAddr(int addr) {
		for (IListItem i : mSpinnedList) {
			if (i.getId() == addr) {
				return i;
			}
		}
		return null;
	}

	private void removeOutItemFromList() {
		int id;
		int index;
		if (mList.isEmpty()) {
			return;
		}
		for (;;) {
			index = mList.size() - 1;
			id = mList.get(index).getId();
			if (id > listSize) {
				Log.d(TAG, "remove out item " + id);
				mList.remove(index);
			} else {
				break;
			}
		}
	}

	private void parseRegValuesBit(String regValues, int addr_start,
			boolean refresh) {
		boolean[] bits = Fx2nControl.bytesToBits(Fx2nControl
				.hexStringToBytes(regValues));
		int rows = regBitCount;
		int leftRows = 0;
		int addr = 0;
		int i;
		int startLoc;
		IListItem spinnedItem;

		if (bits == null) {
			Log.e(TAG, "parseRegValuesBit bits is null.");
			return;
		}

		if (refresh) {
			boolean changed = false;
			int addrStart = addr_start - addr_start % 8;
			int addrEnd = addrStart + bits.length;
			Log.d(TAG, "refresh, syncBit start addr_start=" + addr_start
					+ ", v=" + regValues);
			for (IListItem item : mList) {
				if (item.getId() >= addrStart && item.getId() < addrEnd) {
					item.setValue(bits[item.getId() - addrStart] ? 1 : 0);
					item.setSynced(true);
					changed = true;
				}
			}
			if (changed) {
				mAdapter.notifyDataSetChanged();
			}
			return;
		}

		if (!isAppendList) {
			mList.clear();
			listSize = 0;
		} else {
			removeOutItemFromList();
		}
		// addr = mList.size();
		addr = listSize;

		// if (false) {
		// if (listSize + countPerPage > rows) {
		// leftRows = rows - listSize;
		// } else {
		// leftRows = countPerPage;
		// }
		// if (leftRows > bits.length) {
		// leftRows = bits.length;
		// }
		// startLoc = 0;
		// if (currentPage == spinnedPages() + 1) { // need fill list
		// leftRows = countPerPage - spinnedModCount();
		// } else if (currentPage > 0) {
		// startLoc = spinnedModCount();
		// }
		// } else
		{
			leftRows = countPerPage;
			if (leftRows > bits.length) {
				leftRows = bits.length;
			}
			startLoc = 0;
		}

		if (listSize + leftRows > rows) {
			leftRows = rows - listSize;
		}

		Log.d(TAG, "bitValues addr=" + addr + ", startloc=" + startLoc
				+ ", leftRows=" + leftRows);

		for (i = startLoc; i < leftRows; i++) {
			spinnedItem = findItemInSpinnedByAddr(addr);
			if (spinnedItem == null) {
				IListItem item = new RegListItem(addr);
				item.setItemId(addr);
				item.setTitle(getItemName(addr, addrType));
				item.setValue(bits[i] ? 1 : 0);
				item.setSpinned(getIsSpinned(addr, addrType));
				item.setSynced(true);
				mList.add(item);
			} else {
				spinnedItem.setValue(bits[i] ? 1 : 0);
			}
			addr++;
		}

		listSize = addr;
		mAdapter.notifyDataSetChanged();
	}

	private void parseRegValuesByte(String regValues, int addr_start,
			boolean refresh) {
		int rows = regBitCount / (mByteLen * 8);
		int leftRows = 0;
		int i;
		int addr = 0;
		int[] values = Fx2nControl.hexStringToInt(regValues, mByteLen);
		int startLoc;
		IListItem spinnedItem;

		if (values == null) {
			Log.e(TAG, "parseRegValuesByte values is null.");
			return;
		}

		if (refresh) {
			boolean changed = false;
			int addrStart = addr_start;
			int addrEnd = addrStart + values.length;
			Log.d(TAG, "refresh, syncByte start addr=" + addr_start + ", v="
					+ regValues);
			for (IListItem item : mList) {
				if (item.getId() >= addrStart && item.getId() < addrEnd) {
					item.setValue(values[item.getId() - addrStart]);
					item.setSynced(true);
					changed = true;
				}
			}
			if (changed) {
				mAdapter.notifyDataSetChanged();
			}
			return;
		}

		if (!isAppendList) {
			mList.clear();
			listSize = 0;
		} else {
			removeOutItemFromList();
		}
		// addr = mList.size();
		addr = listSize;
		// if (false) {
		// if (listSize + countPerPage > rows) {
		// leftRows = rows - listSize;
		// } else {
		// leftRows = countPerPage;
		// }
		// if (leftRows > values.length) {
		// leftRows = values.length;
		// }
		// startLoc = 0;
		// if (currentPage == spinnedPages() + 1) { // need fill list
		// leftRows = countPerPage - spinnedModCount();
		// } else if (currentPage > 0) {
		// startLoc = spinnedModCount();
		// }
		// } else
		{
			leftRows = countPerPage;
			if (leftRows > values.length) {
				leftRows = values.length;
			}
			startLoc = 0;
		}

		if (listSize + leftRows > rows) {
			leftRows = rows - listSize;
		}

		Log.d(TAG, "byteValues addr=" + addr + ", startloc=" + startLoc
				+ ", leftRows=" + leftRows);

		for (i = startLoc; i < leftRows; i++) {
			spinnedItem = findItemInSpinnedByAddr(addr);
			if (spinnedItem == null) {
				IListItem item = new RegListItem(addr);
				item.setItemId(addr);
				item.setTitle(getItemName(addr, addrType));
				item.setValue(values[i]);
				item.setSpinned(getIsSpinned(addr, addrType));
				item.setSynced(true);
				mList.add(item);
			} else {
				spinnedItem.setValue(values[i]);
			}
			addr++;
		}

		listSize = addr;
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
		String name;
		nameEdit.setSingleLine();
		nameEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
		if (item.getValue() > 0) {
			nameEdit.setText(Integer.toString(item.getValue()));
		}
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		nameEdit.setLayoutParams(lp);
		nameEdit.selectAll();
		name = "修改寄存器";
		new AlertDialog.Builder(daa)
				.setView(nameEdit)
				.setTitle(
						name
								+ (!item.getTitle().trim().isEmpty() ? "("
										+ item.getTitle() + ")" : ""))
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String editValue = nameEdit.getText()
										.toString();
								int iValue = 0;
								try {
									iValue = Integer.parseInt(editValue);
								} catch (Exception e) {
								}
								executeByteControlWrite(item.getId(), iValue);
								item.setValue(iValue);
							}

						}).show();
	}

	private IListItem findItemByPosition(int position) {
		if (position < mSpinnedList.size()) {
			return mSpinnedList.get(position);
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
		final IListItem item = findItemByPosition(position);
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
		final IListItem item = findItemByPosition(position);
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

	private void loadSpinnedListFromDB() {
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
					item.setSynced(false);
					mSpinnedList.add(item);
				}
			}
		}
		refreshPaging();
	}

	private int findFirstNeedAddr(List<IListItem> list) {
		if (list.isEmpty())
			return -1;
		for (IListItem i : list) {
			if (!i.synced()) {
				return i.getId();
			}
		}
		return -1;
	}

	private int calcStartInByte(int addr) {
		int start;
		if (mBitMode) {
			start = addr / 8;
		} else {
			start = addr;
		}
		return start;
	}

	private void refreshSpinnedListValues(boolean forRefresh) {
		if (DevicePlugsActivityTabs.currentPageId == thisPageId) {
			int addr = findFirstNeedAddr(mSpinnedList);
			if (addr >= 0) {
				int byteStart = calcStartInByte(addr);
				executeByteControlReadForSpinned(addr, byteStart,
						Math.min(countPerPage, regBitCount), forRefresh);
			} else {
				if (forRefresh) {
					autoRefreshCompleteSpinnedList = true;
				}
			}
		} else {
			// force complete for select tab changed
			if (forRefresh) {
				autoRefreshCompleteSpinnedList = true;
			}
		}
	}

	private void refreshListValues(boolean forRefresh) {
		if (DevicePlugsActivityTabs.currentPageId == thisPageId) {
			int addr = findFirstNeedAddr(mList);
			if (addr >= 0) {
				int byteStart = calcStartInByte(addr);
				executeByteControlReadForRefresh(addr, byteStart,
						Math.min(countPerPage, regBitCount), forRefresh);
			} else {
				if (forRefresh) {
					autoRefreshCompleteList = true;
				}
			}
		} else {
			// force complete for select tab changed
			if (forRefresh) {
				autoRefreshCompleteList = true;
			}
		}
	}

	private void parseRegBitForSpinned(int addr, String v, boolean refresh) {
		try {
			boolean changed = false;
			boolean[] bits = Fx2nControl.bytesToBits(Fx2nControl
					.hexStringToBytes(v));
			int addrStart = addr - addr % 8;
			int addrEnd = addrStart + bits.length;

			Log.d(TAG, "syncBit start addr=" + addr + ", v=" + v);

			for (IListItem item : mSpinnedList) {
				if ((refresh || !item.synced())
						&& (item.getId() >= addrStart && item.getId() < addrEnd)) {
					item.setValue(bits[item.getId() - addrStart] ? 1 : 0);
					item.setSynced(true);
					changed = true;
					Log.d(TAG, "syncBit addr=" + item.getId() + ", value="
							+ item.getValue());
				}
			}
			if (changed) {
				mAdapter.notifyDataSetChanged();
			}
		} catch (Exception e) {
			Log.e(TAG, "parseRegBitForSpinned error! msg: " + e.getMessage());
		}
	}

	private void parseRegByteForSpinned(int addr, String v, boolean refresh) {
		boolean changed = false;
		int[] values = Fx2nControl.hexStringToInt(v, mByteLen);
		int addrStart = addr;
		int addrEnd = addrStart + values.length;

		Log.d(TAG, "syncByte start addr=" + addr + ", v=" + v);

		for (IListItem item : mSpinnedList) {
			if ((refresh || !item.synced())
					&& (item.getId() >= addrStart && item.getId() < addrEnd)) {
				item.setValue(values[item.getId() - addrStart]);
				item.setSynced(true);
				changed = true;
				Log.d(TAG,
						"syncByte addr=" + item.getId() + ", value="
								+ item.getValue());
			}
		}
		if (changed) {
			mAdapter.notifyDataSetChanged();
		}
	}

	private void parseRegValuesForSpinned(int addr, String v, boolean refresh) {
		try {
			if (mBitMode) {
				parseRegBitForSpinned(addr, v, refresh);
			} else {
				parseRegByteForSpinned(addr, v, refresh);
			}
			refreshSpinnedListValues(refresh);
		} catch (Exception e) {
			Log.e(TAG, "parseRegValuesForSpinned error! msg: " + e.getMessage());
		}
	}

	private void appendToList(IListItem item, List<IListItem> list) {
		if (list.isEmpty()) {
			list.add(item);
		} else if (list.get(0).getId() > item.getId()) {
			list.add(0, item);
		} else if (list.get(list.size() - 1).getId() < item.getId()) {
			list.add(item);
		} else {
			int i;
			for (i = 0; i < list.size(); i++) {
				if (list.get(i).getId() > item.getId()) {
					list.add(i, item);
					break;
				}
			}
		}
	}

	private void removeFromList(IListItem item, List<IListItem> list) {
		if (list.isEmpty())
			return;
		for (IListItem i : list) {
			if (i.getId() == item.getId()
					&& i.getTitle().equalsIgnoreCase(item.getTitle())) {
				list.remove(i);
				break;
			}
		}
	}

	private void removeFromSpinnedList(IListItem item) {
		removeFromList(item, mSpinnedList);
	}

	private void appendToSpinnedList(IListItem item) {
		appendToList(item, mSpinnedList);
	}

	private void removeFromList(IListItem item) {
		removeFromList(item, mList);
	}

	private void appendToList(IListItem item) {
		appendToList(item, mList);
	}

	private void addItemToList(int id, List<IListItem> list, int value,
			boolean isSpinned) {
		int addr = id;
		IListItem item = new RegListItem(addr);
		item.setItemId(addr);
		item.setTitle(getItemName(addr, addrType));
		item.setValue(mBitMode ? (value != 0 ? 1 : 0) : value);
		item.setSpinned(isSpinned);
		list.add(item);
	}

	private void saveItemToDB(int id, boolean isSpinned) {
		RegisterDB r = new RegisterDB();
		r.setId(0);
		r.setRegType(addrType);
		r.setRegAddr(id);
		r.setRegName("");
		r.setIsSpinned(isSpinned);
		IOTRegisterDBManager.getInstance().insertOrReplace(r);
	}

	private void appendToSpinnedListByManual(int id) {
		int i;
		boolean exist = false;
		for (i = 0; i < mSpinnedList.size(); i++) {
			if (mSpinnedList.get(i).getId() == id) {
				exist = true;
				break;
			}
		}
		if (!exist) {
			saveItemToDB(id, true);
			addItemToList(id, mSpinnedList, 0, true);
			for (i = 0; i < mList.size(); i++) {
				if (mList.get(i).getId() == id) {
					mList.remove(i);
					break;
				}
			}
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_tabmain_item_register);
		tabIndex = getArguments().getInt(INTENT_INT_INDEX);
		regIndex = findRegIndexByType(getArguments().getInt(
				INTENT_REGISTER_TYPE));
		thisPageId = getArguments().getInt(THIS_PAGE_ID);
		addrType = Fx2nControl.addrTypeValues[regIndex];
		addrTypeName = Fx2nControl.addrTypeitems[regIndex];
		mByteLen = Fx2nControl.addrTypeValueLen[regIndex];

		if (addrType == Fx2nControl.REG_D) {
			mBitMode = false;
		} else {
			mBitMode = true;
		}

		txtAddrType = (TextView) findViewById(R.id.txtAddrType);
		if (addrType == Fx2nControl.REG_GPIO) {
			txtAddrType.setText("IO");
		} else {
			txtAddrType.setText(addrTypeName);
		}
		editTextAddrType = (EditText) findViewById(R.id.editTextAddrType);
		editTextAddrType.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					editTextAddrType.setHint("");
				} else {
					editTextAddrType.setHint("输入编号");
				}
			}
		});

		btnRefreshTimeSub = (Button) findViewById(R.id.btnRefreshTimeSub);
		btnRefreshTimeSub.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (RefreshListSleepTime > 0) {
					RefreshListSleepTime--;
					Editor editor = settings.edit();
					editor.putInt("RefreshListSleepTime" + addrType,
							RefreshListSleepTime);
					editor.commit();
					txtRefreshTime.setText(RefreshListSleepTime + "秒刷新");
				}
			}
		});
		txtRefreshTime = (TextView) findViewById(R.id.txtRefreshTime);
		btnRefreshTimeAdd = (Button) findViewById(R.id.btnRefreshTimeAdd);
		btnRefreshTimeAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (RefreshListSleepTime < 60) {
					RefreshListSleepTime++;
					Editor editor = settings.edit();
					editor.putInt("RefreshListSleepTime" + addrType,
							RefreshListSleepTime);
					editor.commit();
					txtRefreshTime.setText(RefreshListSleepTime + "秒刷新");
				}
			}
		});

		settings = this.getApplicationContext().getSharedPreferences(
				"settings", Activity.MODE_PRIVATE);
		RefreshListSleepTime = settings.getInt("RefreshListSleepTime"
				+ addrType, 10);
		txtRefreshTime.setText(RefreshListSleepTime + "秒刷新");

		btnAddrTypeAddr = (Button) findViewById(R.id.btnAddrTypeAddr);
		btnAddrTypeAddr.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String value = editTextAddrType.getText().toString().trim();
				if (!value.isEmpty()) {
					int i = Integer.parseInt(value);
					appendToSpinnedListByManual(i);
				}
			}
		});

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
				// onListItemClick(parent, view, position, id);
			}
		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// onListItemLongClick(parent, view, position, id);
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
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				loadSpinnedListFromDB();
				refreshSpinnedListValues(false);
				mAdapter.notifyDataSetChanged();
			}
		}, 20);

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					if (DevicePlugsActivityTabs.currentPageId == thisPageId
							&& RefreshListSleepTime > 0) {
						try {
							Thread.sleep(RefreshListSleepTime * 1000);
						} catch (InterruptedException e) {
						}
						autoRefreshStart();
					} else {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}).start();
	}

	private void autoRefreshStart() {
		if (mSpinnedList.size() > 0 && autoRefreshCompleteSpinnedList) {
			Log.d(TAG, "mSpinnedList refresh....<" + addrType + ">");
			for (IListItem item : mSpinnedList) {
				item.setSynced(false);
			}
			autoRefreshCompleteSpinnedList = false;
			refreshSpinnedListValues(true);
		}
		if (mList.size() > 0 && autoRefreshCompleteList) {
			Log.d(TAG, "mList refresh....<" + addrType + ">");
			for (IListItem item : mList) {
				item.setSynced(false);
			}
			autoRefreshCompleteList = false;
			refreshListValues(true);
		}
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
		Map<String, Object> tag = new HashMap<String, Object>();
		tag.put("addr", addr);
		status.setTag(tag);
		executePost(status);
	}

	private void executeByteControlReadForSpinned(int addr, int byteStart,
			int rows) {
		executeByteControlReadForSpinned(addr, byteStart, rows, false);
	}

	private void executeByteControlReadForRefresh(int addr, int byteStart,
			int rows, boolean forRefresh) {
		Map<String, Object> tag = new HashMap<String, Object>();
		tag.put("addr", addr);
		tag.put("byteStart", byteStart);
		tag.put("rows", rows);
		tag.put("isWrite", false);
		tag.put("isSpinned", false);
		tag.put("forRefresh", forRefresh);
		executeByteControl(byteStart, rows, false, 0, tag, false);
	}

	private void executeByteControlReadForSpinned(int addr, int byteStart,
			int rows, boolean forRefresh) {
		Map<String, Object> tag = new HashMap<String, Object>();
		tag.put("addr", addr);
		tag.put("byteStart", byteStart);
		tag.put("rows", rows);
		tag.put("isWrite", false);
		tag.put("isSpinned", true);
		tag.put("forRefresh", forRefresh);
		executeByteControl(byteStart, rows, false, 0, tag, false);
	}

	private void executeByteControlRead(int addr, int rows) {
		executeByteControlRead(addr, rows, false);
	}

	private void executeByteControlRead(int addr, int rows, boolean forRefresh) {
		Map<String, Object> tag = new HashMap<String, Object>();
		tag.put("addr", addr);
		tag.put("rows", rows);
		tag.put("isWrite", false);
		tag.put("isSpinned", false);
		tag.put("forRefresh", forRefresh);
		executeByteControl(addr, rows, false, 0, tag);
	}

	private void executeByteControlWrite(int addr, int writeValue) {
		Map<String, Object> tag = new HashMap<String, Object>();
		tag.put("addr", addr);
		tag.put("rows", 1);
		tag.put("isWrite", true);
		tag.put("isSpinned", false);
		tag.put("forRefresh", false);
		executeByteControl(addr, 1, true, writeValue, tag);
	}

	private void executeByteControl(int addr, int count, boolean isWrite,
			int writeValue, Map tag) {
		executeByteControl(addr, count, isWrite, writeValue, tag,
				isWrite ? true : false);
	}

	private long lastTime = 0;
	private int delayMS = 300;
	private int seq = 0;

	private void executeByteControl(int addr, int count, boolean isWrite,
			int writeValue, Map tag, boolean showWaitingDialog) {
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

		final boolean swd = showWaitingDialog;
		final IEspStatusPlugs status = new EspStatusPlugs();
		status.setControlParam("control", isWrite ? Fx2nControl.CMD_WRITE
				: Fx2nControl.CMD_READ, byteAddrType, byteAddr, hexString,
				byteLen);
		status.setTag(tag);

		Log.d(TAG,
				"post param, forRefresh = "
						+ Boolean.valueOf(tag.get("forRefresh").toString())
						+ ", divTime = "
						+ (System.currentTimeMillis() - lastTime));
		if (Boolean.valueOf(tag.get("forRefresh").toString())
				&& (System.currentTimeMillis() - lastTime < delayMS)) {
			final int iseq = seq++;
			long delay = delayMS - (System.currentTimeMillis() - lastTime);
			lastTime = System.currentTimeMillis();
			Log.d(TAG, "<" + iseq + ">delayed " + delay + " post at "
					+ (lastTime + delay));
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					executePost(status, swd);
					Log.d(TAG,
							"<" + iseq + ">posted at "
									+ System.currentTimeMillis());
				}
			}, delay);
		} else {
			lastTime = System.currentTimeMillis();
			executePost(status, showWaitingDialog);
		}
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
		ImageView spin;
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
			return findItemByPosition(position);
		}

		@Override
		public long getItemId(int position) {
			if (position < mSpinnedList.size()) {
				return mSpinnedList.get(position).getItemId();
			} else {
				return mList.get(position - mSpinnedList.size()).getItemId();
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
						onListItemLongClick(null, v, pos + 1, 0);
					}
				});
				holder.title = (TextView) view
						.findViewById(R.id.aperture_title);
				holder.title.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = Integer.parseInt(v.getTag().toString());
						onListItemLongClick(null, v, pos + 1, 0);
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
				holder.spin = (ImageView) view.findViewById(R.id.btn_spin);
				holder.spin.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						ImageView btn = (ImageView) v;
						int pos = Integer.parseInt(v.getTag().toString());
						IListItem item = getItem(pos);
						if (item.isSpinned()) {
							IOTRegisterDBManager.getInstance().delete(addrType,
									item.getId(), true);
							btn.setBackgroundResource(R.drawable.spin);
							item.setSpinned(false);
							removeFromSpinnedList(item);
							appendToList(item);
						} else {
							RegisterDB r = new RegisterDB();
							r.setId(0);
							r.setRegType(addrType);
							r.setRegAddr(item.getId());
							r.setRegName(item.getTitle());
							r.setIsSpinned(true);
							IOTRegisterDBManager.getInstance().insertOrReplace(
									r);
							btn.setBackgroundResource(R.drawable.unspin);
							item.setSpinned(true);
							removeFromList(item);
							appendToSpinnedList(item);
						}
						mAdapter.notifyDataSetChanged();
					}
				});
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}

			IListItem item = getItem(position);
			if (holder.icon != null) {
				holder.icon
						.setBackgroundResource(R.drawable.esp_icon_plugs_aperture);
			}
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
				holder.statusText.setText(String.format("        %-6d", ul));
				holder.statusText.setVisibility(View.VISIBLE);
				holder.statusText.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = Integer.parseInt(v.getTag().toString());
						onListItemClick(null, v, pos + 1, 0);
					}
				});
			}
			holder.notes.setVisibility(View.GONE);
			if (item.isSpinned()) {
				holder.spin.setBackgroundResource(R.drawable.spin);
				view.setBackgroundColor(Color.LTGRAY);
			} else {
				holder.spin.setBackgroundResource(R.drawable.unspin);
				view.setBackgroundColor(Color.WHITE);

			}
			holder.icon.setTag(position);
			holder.title.setTag(position);
			holder.status.setTag(position);
			holder.statusText.setTag(position);
			if (addrType == Fx2nControl.REG_GPIO) {
				holder.spin.setVisibility(View.GONE);
			} else {
				holder.spin.setVisibility(View.VISIBLE);
			}
			holder.spin.setTag(position);
			return view;
		}
	}
}
