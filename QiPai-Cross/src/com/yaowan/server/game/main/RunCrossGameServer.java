/**
 * 
 */
package com.yaowan.server.game.main;

import com.yaowan.ServerConfig;
import com.yaowan.core.base.Spring;
import com.yaowan.core.function.FunctionManager;
import com.yaowan.cross.CrossGameNettyServer;
import com.yaowan.csv.CheckConfig;
import com.yaowan.csv.ConfigLoader;
import com.yaowan.excel.ExcelCacheLoader;
import com.yaowan.framework.core.handler.ProtobufCenter;
import com.yaowan.framework.druid.DruidControllerSystemConfig;
import com.yaowan.framework.druid.JettyServer;
import com.yaowan.httpserver.NettyHttp;

/**
 * @author zane
 *
 */
public class RunCrossGameServer {

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

		// 初始化协议中心
		ProtobufCenter.init();
//		// 启动定时器
//		Spring.getBean(QueueManager.class).start();
		// 服务器启动
		CrossGameNettyServer.start();
		
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
	
	
	
	
	
}
