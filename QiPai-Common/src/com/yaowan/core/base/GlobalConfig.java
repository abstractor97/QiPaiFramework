/**
 * 
 */
package com.yaowan.core.base;

/**
 * @author huangyuyuan
 *
 */
public class GlobalConfig {

	public static boolean debug = false;
	
	public static int quartzThreadSize = 2;
	
	public static String platformKey = "^_^dfh3:start@2015-09-24!";
	
	public static String excelPath = "excel/";
	
	public static String csvConfigDir = "csv/";
	
	public static String chargeBase = "http://qipai-platform.allrace.com";
	
	public static long initDBIncrementId = 1;
	
	public static boolean isTest = false;
	//U8平台地址
	public static String U8ServerURL = "http://localhost:9001/U8Server";
	
	//Http请求有效性检验
	public static String HttpValidCheck = "127.0.0.1";
}
