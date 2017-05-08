package com.yaowan.game.qipai.test;


import org.apache.log4j.PropertyConfigurator;

import com.yaowan.core.handler.ProtobufCenter;
import com.yaowan.util.LogUtil;

/**
 * 测试工具
 * 
 */
public class Runner {
	//是否只是登录
	public static final boolean OnlyLogin = false;
	public static void main(String[] args) {
		
		String log4jFile = "log4j.properties";
		PropertyConfigurator.configure(log4jFile);
		//118.89.36.187:11002
		String ip = "127.0.0.1";
		int port = 11401;
		int playerNumber = 1;
		int loginSplit = 300;
		int playerGroup = 1;
	
		if(args.length>0){
			if (args.length != 4) {
				printHelp();
				return;
			}
			if (!args[0].trim().startsWith("-s")) {
				printHelp();
				return;
			}
	
			if (!args[1].trim().startsWith("-n")) {
				printHelp();
				return;
			}
			
			if (!args[2].trim().startsWith("-p")) {
				printHelp();
				return;
			}
			
			if (!args[3].trim().startsWith("-g")) {
				printHelp();
				return;
			}
			
			String[] server = args[0].substring(2).split(":");
			ip = server[0];
			port = Integer.valueOf(server[1]);
			playerNumber = Integer.valueOf(args[1].substring(2));
			loginSplit = Integer.valueOf(args[2].substring(2));
			playerGroup = Integer.valueOf(args[3].substring(2));
		}
		
		// 初始化协议中心
		ProtobufCenter.init();
		int count = 0;
		while (count< playerNumber) {
			try {
				Thread.sleep((int)Math.floor(1000D/loginSplit));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			count++;
			NettyClient.connectToCenter(ip,port,count,playerGroup);
		}
	}

	private static void printHelp() {
		LogUtil.error("-s地址:端口 -n总人数 -p每秒登录的人数 -g玩家分组");
	}
}
