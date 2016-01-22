package com.espressif.iot.type.device.status;

import java.util.List;
import java.util.Vector;

import android.os.Handler;

public class EspStatusPlugs implements IEspStatusPlugs, Cloneable {
	private List<IAperture> mApertureList;

	private boolean listControl = true;

	private String action = "";
	private int cmd = 0;
	private int addrType = 0;
	private int addr = 0;
	private String value = "";
	private int len = 0;

	private int result;

	private Handler handler;

	public EspStatusPlugs() {
		mApertureList = new Vector<IAperture>();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public void setStatusApertureList(List<IAperture> list) {
		mApertureList.clear();
		mApertureList.addAll(list);
		listControl = true;
	}

	@Override
	public List<IAperture> getStatusApertureList() {
		return mApertureList;
	}

	@Override
	public void updateOrAddAperture(IAperture newAperture) {
		for (IAperture aperture : mApertureList) {
			if (aperture.getId() == newAperture.getId()) {
				aperture.setTitle(newAperture.getTitle());
				aperture.setOn(newAperture.isOn());
				return;
			}
		}

		mApertureList.add(newAperture);
	}

	@Override
	public boolean updateApertureOnOff(IAperture newAperture) {
		for (IAperture aperture : mApertureList) {
			if (aperture.getId() == newAperture.getId()) {
				aperture.setOn(newAperture.isOn());
				return true;
			}
		}

		return false;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void setControlParam(String action, int cmd, int addrType, int addr,
			String value, int len) {
		listControl = false;
		this.action = action;
		this.cmd = cmd;
		this.addrType = addrType;
		this.addr = addr;
		this.value = value;
		this.len = len;
	}

	public String getAction() {
		return this.action;
	}

	public int getCmd() {
		return this.cmd;
	}

	public int getAddrType() {
		return this.addrType;
	}

	public void setAddrType(int addrType) {
		this.addrType = addrType;
	}

	public int getAddr() {
		return this.addr;
	}

	public String getValue() {
		return this.value;
	}

	public int getLen() {
		return this.len;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public int getResult() {
		return this.result;
	}

	@Override
	public void setAction(String v) {
		this.action = v;
	}

	@Override
	public void setCmd(int i) {
		this.cmd = i;
	}

	@Override
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	@Override
	public Handler getHandler() {
		return this.handler;
	}

}
