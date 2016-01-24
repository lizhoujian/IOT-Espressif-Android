package com.espressif.iot.object.db;

import java.util.List;

import com.espressif.iot.db.greenrobot.daos.DeviceDB;
import com.espressif.iot.object.IEspDBManager;

public interface IRegisterDBManager extends IEspDBManager {
	IRegisterDB find(int regType, int regAddr);

	void insertOrReplace(IRegisterDB r);
}
