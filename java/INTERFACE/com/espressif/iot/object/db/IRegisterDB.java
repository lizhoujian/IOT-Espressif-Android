package com.espressif.iot.object.db;

import com.espressif.iot.object.IEspDBObject;

/**
 * the User db should save such properties as follows
 * 
 * @author afunx
 * 
 */
public interface IRegisterDB extends IEspDBObject {
	/**
	 * get the user's id
	 * 
	 * @return the user's id
	 */
	long getId();

	/**
	 * set the user's id
	 * 
	 * @param id
	 *            the user's id
	 */
	void setId(long id);

	/**
	 * get the register's regType
	 * 
	 * @return the register's regType
	 */
	int getRegType();

	void setRegType(int regType);

	/**
	 * get the register's regAddr
	 * 
	 * @return the register's regAddr
	 */
	int getRegAddr();

	void setRegAddr(int regAddr);

	/**
	 * get the register's regName
	 * 
	 * @return the register's regName
	 */
	String getRegName();

	void setRegName(String regName);

	boolean getIsSpinned();

	void setIsSpinned(boolean isSpinned);

}
