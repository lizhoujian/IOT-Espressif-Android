package com.espressif.iot.db.greenrobot.daos;

import java.util.List;
import com.espressif.iot.db.greenrobot.daos.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import com.espressif.iot.object.db.IRegisterDB;

// KEEP INCLUDES END
/**
 * Entity mapped to table REGISTER_DB.
 */
public class RegisterDB implements IRegisterDB {

	private long id;

	/** Not-null value. */
	private int regType;

	/** Not-null value. */
	private int regAddr;

	/** Not-null value. */
	private String regName;

	private boolean isSpinned;

	/** Used to resolve relations */
	private transient DaoSession daoSession;

	/** Used for active entity operations. */
	private transient RegisterDBDao myDao;

	private List<DeviceDB> devices;

	// KEEP FIELDS - put your custom fields here
	// KEEP FIELDS END

	public RegisterDB() {
	}

	public RegisterDB(long id) {
		this.id = id;
	}

	public RegisterDB(long id, int regType, int regAddr, String regName,
			boolean isSpinned) {
		this.id = id;
		this.regType = regType;
		this.regAddr = regAddr;
		this.regName = regName;
		this.isSpinned = isSpinned;
	}

	/** called by internal mechanisms, do not call yourself. */
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getRegisterDBDao() : null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Convenient call for {@link AbstractDao#delete(Object)}. Entity must
	 * attached to an entity context.
	 */
	public void delete() {
		if (myDao == null) {
			throw new DaoException("Entity is detached from DAO context");
		}
		myDao.delete(this);
	}

	/**
	 * Convenient call for {@link AbstractDao#update(Object)}. Entity must
	 * attached to an entity context.
	 */
	public void update() {
		if (myDao == null) {
			throw new DaoException("Entity is detached from DAO context");
		}
		myDao.update(this);
	}

	/**
	 * Convenient call for {@link AbstractDao#refresh(Object)}. Entity must
	 * attached to an entity context.
	 */
	public void refresh() {
		if (myDao == null) {
			throw new DaoException("Entity is detached from DAO context");
		}
		myDao.refresh(this);
	}

	@Override
	public int getRegType() {
		return this.regType;
	}

	@Override
	public void setRegType(int regType) {
		this.regType = regType;
	}

	@Override
	public int getRegAddr() {
		return this.regAddr;
	}

	@Override
	public void setRegAddr(int regAddr) {
		this.regAddr = regAddr;
	}

	@Override
	public String getRegName() {
		return this.regName;
	}

	@Override
	public void setRegName(String regName) {
		this.regName = regName;
	}

	@Override
	public boolean getIsSpinned() {
		return this.isSpinned;
	}

	@Override
	public void setIsSpinned(boolean isSpinned) {
		this.isSpinned = isSpinned;
	}

	// KEEP METHODS - put your custom methods here
	// KEEP METHODS END

}
