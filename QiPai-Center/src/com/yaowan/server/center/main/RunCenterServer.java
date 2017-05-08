package com.yaowan.server.center.main;

import java.io.File;
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
import com.yaowan.server.center.quartz.CenterQueueManager;

public class RunCenterServer {

	public static String configPath0 = "E:/WorkSpace/ServerQiPai/config";

	public static void main(String[] args) {
		if(args.length==0){
			args = new String[]{new File("").getAbsolutePath() + File.separator
					+ "config"};
		}
		// 加载系统配置
		ServerConfig.init(args);
		// 系统配置路径
		System.setProperty("gameserver.dir", ServerConfig.configPath0);
		// 加载Spring配置
		Spring.init();
//		// 加载配置表
//		ExcelCacheLoader.load();
//		正式服是不能校验中心服的数据库， 中心服数据库与平台共用，由平台维护结构
//		try {
//			initDatabase();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		// 检查数值配置文件类型
		Spring.getBean(CheckConfig.class).check();
		// 加载配置
		Spring.getBean(ConfigLoader.class).load();

		// 执行开服处理
		FunctionManager.handleOnServerStart();
		// 初始化协议中心
		ProtobufCenter.init();
		// 启动定时器
		Spring.getBean(CenterQueueManager.class).start();
		// 服务器启动
		CenterNettyServer.start();

	}
	
	
}
