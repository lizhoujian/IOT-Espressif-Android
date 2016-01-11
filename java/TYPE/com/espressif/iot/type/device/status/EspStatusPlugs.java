package com.espressif.iot.type.device.status;

import java.util.List;
import java.util.Vector;

public class EspStatusPlugs implements IEspStatusPlugs, Cloneable {
	private List<IAperture> mApertureList;

	private boolean listControl = true;

	private int cmd = 0;
	private int addrType = 0;
	private int addr = 0;
	private String value = "";
	private int len = 0;

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
	public void setControlParam(int cmd, int addrType, int addr, String value,
			int len) {
		listControl = false;
		this.cmd = cmd;
		this.addrType = addrType;
		this.addr = addr;
		this.value = value;
		this.len = len;
	}
}
