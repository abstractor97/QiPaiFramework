/**
 * 
 */
package com.yaowan.framework.database;

import org.springframework.beans.factory.annotation.Autowired;

import com.yaowan.framework.database.db.BaseDB;
import com.yaowan.framework.database.db.DataDB;


/**
 * @author huangyuyuan
 *
 */
public abstract class DataDao<T> extends BaseDao<T>{

	@Autowired
	private DataDB dataDB;

	@Override
	protected BaseDB getDB() {
		return dataDB;
	}
}
