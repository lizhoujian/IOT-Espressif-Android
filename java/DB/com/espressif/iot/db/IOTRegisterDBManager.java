package com.espressif.iot.db;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.db.greenrobot.daos.DeviceDB;
import com.espressif.iot.db.greenrobot.daos.RegisterDBDao.Properties;
import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.db.greenrobot.daos.RegisterDB;
import com.espressif.iot.db.greenrobot.daos.RegisterDBDao;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.object.db.IRegisterDB;
import com.espressif.iot.object.db.IRegisterDBManager;

import de.greenrobot.dao.query.Query;

public class IOTRegisterDBManager implements IRegisterDBManager,
		IEspSingletonObject {
	private static final Logger log = Logger
			.getLogger(IOTRegisterDBManager.class);

	private RegisterDBDao registerDao;

	// Singleton Pattern
	private static IOTRegisterDBManager instance = null;

	private IOTRegisterDBManager(DaoSession daoSession) {
		this.registerDao = daoSession.getRegisterDBDao();
	}

	public static void init(DaoSession daoSession) {
		instance = new IOTRegisterDBManager(daoSession);
	}

	public static IOTRegisterDBManager getInstance() {
		return instance;
	}

	private IRegisterDB findByRRS(int regType, int regAddr, boolean isSpinned) {
		Query<RegisterDB> query = registerDao
				.queryBuilder()
				.where(Properties.RegType.eq(regType),
						Properties.RegAddr.eq(regAddr),
						Properties.IsSpinned.eq(isSpinned)).build();
		List<RegisterDB> results = query.list();
		if (results != null && results.size() > 0) {
			log.debug(Thread.currentThread().toString()
					+ "##findRegister(regType=[" + regType + "],regAddr=["
					+ regAddr + "]): " + results);
			return results.get(0);
		} else {
			return null;
		}
	}

	@Override
	public IRegisterDB find(int regType, int regAddr, boolean isSpinned) {
		return findByRRS(regType, regAddr, isSpinned);
	}

	public long getLastId() {
		Query<RegisterDB> query = registerDao.queryBuilder()
				.orderDesc(Properties.Id).limit(1).build();
		List<RegisterDB> results = query.list();
		if (results != null && results.size() > 0) {
			return results.get(0).getId();
		} else {
			return 0;
		}
	}

	@Override
	public synchronized void insertOrReplace(IRegisterDB r) {
		IRegisterDB ir;
		RegisterDB re = new RegisterDB();
		re.setId(r.getId());
		re.setRegType(r.getRegType());
		re.setRegAddr(r.getRegAddr());
		re.setRegName(r.getRegName());
		re.setIsSpinned(r.getIsSpinned());
		if ((ir = find(r.getRegType(), r.getRegAddr(), r.getIsSpinned())) != null) {
			re.setId(ir.getId());
			registerDao.update(re);
		} else {
			re.setId(getLastId() + 1);
			registerDao.insertOrReplace(re);
		}
	}

	@Override
	public List<RegisterDB> findSpinnedBy(int regType) {
		Query<RegisterDB> query = registerDao
				.queryBuilder()
				.where(Properties.RegType.eq(regType),
						Properties.IsSpinned.eq(true))
				.orderAsc(Properties.RegAddr).build();
		List<RegisterDB> results = query.list();
		if (results != null && results.size() > 0) {
			log.debug(Thread.currentThread().toString()
					+ "##findRegister(regType=[" + regType
					+ ", isSpinned=true]): " + results);
			return results;
		} else {
			return null;
		}
	}

	@Override
	public boolean isSpinned(int regType, int regAddr) {
		return findByRRS(regType, regAddr, true) != null;
	}

	@Override
	public void delete(int regType, int regAddr, boolean isSpinned) {
		IRegisterDB r = findByRRS(regType, regAddr, isSpinned);
		if (r != null) {
			registerDao.deleteByKey(r.getId());
		}
	}
}
