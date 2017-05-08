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
public abstract class LogDao<T> extends BaseDao<T> {

	@Autowired
	private LogDB logDB;

	@Override
	protected BaseDB getDB() {
		return logDB;
	}
}
