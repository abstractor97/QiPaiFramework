/**
 * 
 */
package com.yaowan;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.yaowan.core.base.GlobalConfig;
import com.yaowan.framework.druid.DruidControllerSystemConfig;
import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 * 
 *         游戏服配置
 */
public class ServerConfig {

	public static String configPath0 = "E:/WorkSpace/ServerQiPai/config";

	// 命令调用
	public static boolean cmdEnable = false;
	// 服务器号
	public static int serverId = 1;
	// 服务器端口号
	public static int nettyPort = 11041;
	// 服务器http服务的端口
	public static int httpPort = 9801;
	// 平台服务器网址
	public static String platformURI = "http://manage.mini.yaowan.com:90/";
	// 服务器的语言版本 1:cn 2:tw 3:en 4:kr 5:fr 6:th
	public static int lang = 1;
	// 中心服务器IP
	public static String centerHost = "127.0.0.1";
	// 中心服务器端口
	public static int centerPort = 11042;
	// 跨区游戏类型列表
	public static List<Integer> crossGameTypes = new ArrayList<Integer>();

	/**
	 * 配置数据map
	 */
	private static Properties properties;

	public static void main(String[] args) {
		System.out.println(new File("").getAbsolutePath() + File.separator + "config");
		;
	}

	public static void init(String[] args) {
		String configPath = null;
		if (args.length == 0) {
			configPath = new File("").getAbsolutePath() + File.separator + "config";
		} else if (args.length == 1) {
			configPath = args[0];
		} else if (args.length == 2) {
			configPath = args[0];
			GlobalConfig.csvConfigDir = args[1];
		}

		PropertyConfigurator.configure(configPath + File.separator + "log4j.properties");

		loadLogBack(configPath + File.separator + "logback.xml");

		properties = loadConfig(configPath + File.separator + "global.properties");
		configPath0 = configPath;
		GlobalConfig.excelPath = new File("").getAbsolutePath() + File.separator + "excel" + File.separator;
		nettyPort = Integer.parseInt(properties.getProperty("nettyPort"));
		httpPort = Integer.parseInt(properties.getProperty("httpPort"));

		centerHost = properties.getProperty("centerHost");
		centerPort = Integer.parseInt(properties.getProperty("centerPort"));

		if (properties.containsKey("serverId")) {
			serverId = Integer.parseInt(properties.getProperty("serverId"));
		}
		if (properties.containsKey("crossGameTypes")) {
			String[] gts = properties.get("crossGameTypes").toString().split(",");
			for (String gameType : gts) {
				crossGameTypes.add(Integer.valueOf(gameType));
			}
		}

		GlobalConfig.platformKey = properties.getProperty("platformKey");

		GlobalConfig.quartzThreadSize = Integer.parseInt(properties.getProperty("quartzThreadSize"));
		if (args.length < 2) {
			GlobalConfig.csvConfigDir = properties.getProperty("csvConfigDir");
		}
		if (properties.containsKey("chargeBase")) {
			GlobalConfig.chargeBase = properties.getProperty("chargeBase");
		}
		if (properties.containsKey("U8ServerURL")) {
			GlobalConfig.U8ServerURL = properties.getProperty("U8ServerURL");
		}
		
		if (properties.containsKey("HttpValidCheck")) {
			GlobalConfig.HttpValidCheck = properties.getProperty("HttpValidCheck");
		}
		
		if (properties.containsKey("initIncrementPrefix")) {
			GlobalConfig.initDBIncrementId = Long.parseLong(properties.getProperty("initIncrementPrefix") + "00000000");
		}

		if (properties.containsKey("test")) {
			GlobalConfig.isTest = true;
		}

		if (properties.containsKey("jetty.isStart")) {
			// 初始化阿里连接池的监控系统端口
			DruidControllerSystemConfig.jettyPort = Integer.parseInt(properties.getProperty("jetty.port"));
			DruidControllerSystemConfig.descriptor = properties.getProperty("jetty.descriptor");
			DruidControllerSystemConfig.resourceBase = properties.getProperty("jetty.resourceBase");
			DruidControllerSystemConfig.contextPath = properties.getProperty("jetty.contextPath");
			DruidControllerSystemConfig.isStart = Integer.parseInt(properties.getProperty("jetty.isStart"));

			// 初始化阿里连接池的监控系统参数
			DruidControllerSystemConfig.allow = properties.getProperty("druid.allow");
			DruidControllerSystemConfig.deny = properties.getProperty("druid.deny");
			DruidControllerSystemConfig.loginUsername = properties.getProperty("druid.loginUsername");
			DruidControllerSystemConfig.loginPassword = properties.getProperty("druid.loginPassword");
			LogUtil.info("初始化阿里连接池的监控系统参数完毕！");
		}

	}

	public static Properties loadConfig(String path) {
		Properties properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(path);
			properties.load(in);
		} catch (Exception e) {
			throw new RuntimeException("can't load config.properties:" + path);
		}
		return properties;
	}

	private static void loadLogBack(String pathname) {
	    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            context.reset();
            configurator.doConfigure(pathname);
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}
}
