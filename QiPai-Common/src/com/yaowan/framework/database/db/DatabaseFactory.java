/**
 * 
 */
package com.yaowan.framework.database.db;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 *
 */
public class DatabaseFactory {
	
	public static DataSource create(String hostAndPort, String dbName, String username, String password, int minConnections, int maxConnections) {
		DruidDataSource dataSource = new DruidDataSource();
		String jdbc = "jdbc:mysql://" + hostAndPort + "/" + dbName + "?useUnicode=true&characterEncoding=UTF-8";
		dataSource.setUrl(jdbc);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		// 初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时
		dataSource.setInitialSize(minConnections);
		// 最小连接池数量
		dataSource.setMinIdle(minConnections);
		// 最大连接池数量
		dataSource.setMaxActive(maxConnections);
		// 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
		dataSource.setTestOnBorrow(false);
		// 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
		dataSource.setTestWhileIdle(true);
		// 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
		dataSource.setTestOnReturn(false);
		// 用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
		dataSource.setValidationQuery("select 1");
		// 连接保持空闲而不被驱逐的最长时间
		dataSource.setMinEvictableIdleTimeMillis(30000);
		// 获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁。
		dataSource.setMaxWait(60000);
		// 有两个含义：
		//1) Destroy线程会检测连接的间隔时间，如果连接空闲时间大于等于minEvictableIdleTimeMillis则关闭物理连接
		//2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
		dataSource.setTimeBetweenEvictionRunsMillis(60000);
//		dataSource.setFilters("log4j");
		dataSource.setConnectionProperties("druid.stat.mergeSql=true");
		dataSource.setConnectionProperties("druid.stat.slowSqlMillis=5000");

		//String sql_mode = "SET @@sql_mode = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'";

//		ArrayList<Object> list = new ArrayList<Object>();
//		list.add(sql_mode);
//		dataSource.setConnectionInitSqls(list);


		// initSql(dataSource, sql_mode);
		// initSql(dataSource, sql_mode, "set names utf8");
		LogUtil.info("DataSource init:" + dataSource.getUrl());
		return dataSource;
	}
}
