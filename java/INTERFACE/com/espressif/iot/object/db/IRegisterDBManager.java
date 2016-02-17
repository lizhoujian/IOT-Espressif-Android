package com.espressif.iot.object.db;

import java.util.List;

import com.espressif.iot.db.greenrobot.daos.DeviceDB;
import com.espressif.iot.db.greenrobot.daos.RegisterDB;
import com.espressif.iot.object.IEspDBManager;

public interface IRegisterDBManager extends IEspDBManager {
	IRegisterDB find(int regType, int regAddr, boolean isSpinned);

	void insertOrReplace(IRegisterDB r);

	List<RegisterDB> findSpinnedBy(int regType);
	boolean isSpinned(int regType, int regAddr);
	
	void delete(int regType, int regAddr, boolean isSpinned);
}
