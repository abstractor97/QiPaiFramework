/**
 * 
 */
package com.yaowan.server.game.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.yaowan.ServerConfig;
import com.yaowan.core.base.Spring;
import com.yaowan.core.function.FunctionManager;
import com.yaowan.csv.CheckConfig;
import com.yaowan.csv.ConfigLoader;
import com.yaowan.excel.ExcelCacheLoader;
import com.yaowan.framework.core.handler.ProtobufCenter;
import com.yaowan.framework.druid.DruidControllerSystemConfig;
import com.yaowan.framework.druid.JettyServer;
import com.yaowan.httpserver.NettyHttp;
import com.yaowan.server.game.quartz.QueueManager;
import com.yaowan.tools.dbsync.DatabaseHelper;

/**
 * @author zane
 *
 */
public class RunGameServer {

	public static void main(String[] args) {
		// 加载系统配置
		ServerConfig.init(args);
		// 系统配置路径
		System.setProperty("gameserver.dir", ServerConfig.configPath0);
		// 加载Spring配置
		Spring.init();
		// 加载配置表
		ExcelCacheLoader.load();

		// 检查数值配置文件类型
		Spring.getBean(CheckConfig.class).check();
		// 加载配置
		Spring.getBean(ConfigLoader.class).load();
		
//		数据结构手动进行校验
//		try {
//			initDatabase();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		// 初始化协议中心
		ProtobufCenter.init();
		// 启动定时器
		Spring.getBean(QueueManager.class).start();
		
		NettyClient.init();
		// 服务器启动
		NettyServer.start();
		
		// 执行开服处理
		FunctionManager.handleOnServerStart();
		
		// 连接中心服
		NettyClient.connectToCenter();
		
		// http服务，和html5 WebSocket 服务
		if (DruidControllerSystemConfig.isStart == 1) {
			JettyServer.start();
		}
		NettyHttp.start();
		
	}
	
	
	private static void initDatabase() throws IOException{
		Properties properties = new Properties();
		properties.load(new FileInputStream(ServerConfig.configPath0+"/jdbc.properties"));
		String dbUser = properties.getProperty("gameserver.data.username");
		String dbPassword = properties.getProperty("gameserver.data.password");
		//gameserver.data.url=jdbc:mysql://127.0.0.1:3306/qipai_data
		String dbURL = properties.getProperty("gameserver.data.url");
		String  string = dbURL.substring(dbURL.indexOf("//")+2);
		String dbHost = string.substring(0,string.indexOf('/'));
		int last = string.indexOf('?');
		if(last == -1) last = string.length();
		String dbname = string.substring(string.indexOf('/')+1,last);
		//数据库结构同步
		DatabaseHelper.initDataBase(dbHost, dbname, dbUser, dbPassword, "com.yaowan.server.game.model.data");
		
		
//		gameserver.log.url=jdbc:mysql://127.0.0.1:3306/qipai_log
//		gameserver.log.username=root
//		gameserver.log.password=123456
		dbUser = properties.getProperty("gameserver.log.username");
		dbPassword = properties.getProperty("gameserver.log.password");
		//gameserver.data.url=jdbc:mysql://127.0.0.1:3306/qipai_data
		dbURL = properties.getProperty("gameserver.log.url");
		string = dbURL.substring(dbURL.indexOf("//")+2);
		dbHost = string.substring(0,string.indexOf('/'));
		last = string.indexOf('?');
		if(last == -1) last = string.length();
		dbname = string.substring(string.indexOf('/')+1,last);
		//数据库结构同步
		DatabaseHelper.initDataBase(dbHost, dbname, dbUser, dbPassword, "com.yaowan.server.game.model.data");
	}
	
	
}
