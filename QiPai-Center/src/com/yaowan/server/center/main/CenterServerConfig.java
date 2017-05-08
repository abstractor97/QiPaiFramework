package com.yaowan.server.center.main;


/**
 * 中心服配置
 * @author YW0824
 *
 */
public class CenterServerConfig {
	
	public static int centerPort = 11042;
	
	public static void init(String[] args)
	{
		
	}
	
	/*public static Properties loadConfig(String path) {
		Properties properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(path);
			properties.load(in);
		} catch (Exception e) {
			throw new RuntimeException("can't load config.properties:" + path);
		}
		return properties;
	}*/
	
}
