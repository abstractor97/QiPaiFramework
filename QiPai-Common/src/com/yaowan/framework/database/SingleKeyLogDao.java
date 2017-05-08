/**
 * 
 */
package com.yaowan.framework.database;

import org.springframework.beans.factory.annotation.Autowired;

import com.yaowan.framework.database.db.BaseDB;
import com.yaowan.framework.database.db.LogDB;

/**
 * @author huangyuyuan
 *
 */
public abstract class SingleKeyLogDao<T, PK> extends SingleKeyBaseDao<T, PK> {
	
	@Autowired
	private LogDB logDB;

	@Override
	protected BaseDB getDB() {
		return logDB;
	}
}
