/**
 * 
 */
package com.yaowan.framework.database.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * @author huangyuyuan
 *
 */
public abstract class BaseDB {
	
	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * 线程内的数据库会话
	 */
	private ThreadLocal<Statement> dbSession = new ThreadLocal<Statement>();
	/**
	 * 获取数据库连接
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		return getDataSource().getConnection();
	}
	/**
	 * 获取当前线程的数据库会话
	 * @return
	 * @throws SQLException
	 */
	public Statement getSession() {
		Statement statement = null;
		try {
			statement = dbSession.get();
			if(statement == null || statement.isClosed()) {
				statement = this.getConnection().createStatement();
				dbSession.set(statement);
			}
			return statement;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return statement;
	}
	/**
	 * 关闭当前线程的数据库会话
	 */
	public void closeSession() {
		try {
			Statement statement = dbSession.get();
			if(statement != null && !statement.isClosed()) {
				Connection connection = statement.getConnection();
				statement.close();
				if(connection != null) {
					connection.close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
