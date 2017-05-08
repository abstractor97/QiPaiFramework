package com.yaowan.tools.dbsync;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.db.DatabaseFactory;
import com.yaowan.framework.util.FileUtil;
import com.yaowan.server.game.model.data.entity.Role;


public class OneTableHandler {
	
	public static List<Class<?>> getDefineTableClz(String classPath) {
		Set<Class<?>> classSet = FileUtil.getClasses(classPath);
		
		List<Class<?>> clazzList = new ArrayList<>();
		for(Class<?> clazz : classSet) {
			if(clazz.getAnnotation(Table.class) != null) {
				clazzList.add(clazz);
			}
		}
		return clazzList;
	}
	
	public static void main(String[] args) {
//		if (args.length != 2) {
//			System.out.println("Please set the parameters 'serverConfigDir' and 'configDir'.");
//			return;
//		}
		long startTime = System.currentTimeMillis();
		// 服务器配置初始化
//		Config.init(args[0], args[1]);
		
		
		String ipHost = "127.0.0.1:3306";
		String user = "root";
		String password = "123456";
		String dataBase = "dfh3_data";
		String logDataBase = "dfh3_log";
		
//		String ipHost = "113.107.101.114:27516";
//		String user = "root";
//		String password = "ryzc!@yaowan2014";
//		String dataBase = "dfh3_data";
//		String logDataBase = "dfh3_log";
//		
/*		String ipHost = "manage.mini.yaowan.com:27516";
		String user = "root";
		String password = "7.L7,796,3.k_dsMrv_365^GeYT^fdsjOs_Lx2R_3~b";
		String dataBase = "dfh3_data";
		String logDataBase = "dfh3_log";*/
//		
//		String ipHost = "113.107.95.248:27516";
//		String user = "root";
//		String password = "7.L7,796,3.k_dsMrv_365^GeYT^fdsjOs_Lx2R_3~b";
//		String dataBase = "dfh3_9998_data";
//		String logDataBase = "dfh3_9998_log";
				
		initDataBase(ipHost, dataBase, user, password, "data.game");
		//initDataBase(ipHost, logDataBase, user, password, "data.gamelog");
		
		System.out.println("exe times:" + (System.currentTimeMillis() - startTime) + " ms");
	}
	
	public static void initDataBase(String ipHost, String dataBase, String user, String password, String classPath) {
		try {
			DataSource data = DatabaseFactory.create(ipHost, dataBase, user, password, 1, 2);
			Statement statement = data.getConnection().createStatement();
			
			
			Class<?> clazz = Role.class;
			Table table = clazz.getAnnotation(Table.class);
			
			table.catalog();
			String tableName = table.name();
			if("".equals(tableName)) {
				tableName = clazz.getSimpleName();
			}
			
			if(table.catalog() > 0) {
				for(int i = table.catalog_(); i < table.catalog(); i++) {
					checkTable(statement, dataBase, clazz, tableName + "_" + i);
				}
			} else {
				checkTable(statement, dataBase, clazz, tableName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void checkTable(Statement statement, String dataBase, Class<?> clazz, String tableName) throws SQLException {
		String tableSql = "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + dataBase + "' and TABLE_NAME = '"
				+ tableName + "'";
		
		ResultSet rs = statement.executeQuery(tableSql);
		//表存在
		if(rs.next()) {
			rs.close();
			TableRebuilder.changeTable(statement, clazz, dataBase, tableName);
		} else {
			//表不存在
			rs.close();
			TableCreator.createTable(statement, clazz, dataBase, tableName);
		}
	}
	
	public static boolean isTableHasPrimary(Statement statement, String dataBase, String tableName) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE");
		sql.append(" WHERE TABLE_SCHEMA = '").append(dataBase).append("'");
		sql.append(" AND TABLE_NAME = '").append(tableName).append("'");
		ResultSet rs = statement.executeQuery(sql.toString());
		boolean has = false;
		if(rs.next()) {
			has = true;
		}
		rs.close();
		return has;
	}
}
