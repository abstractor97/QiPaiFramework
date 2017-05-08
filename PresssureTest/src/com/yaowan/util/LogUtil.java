package com.yaowan.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LogUtil {
	
	private static Logger logger = Logger.getLogger(LogUtil.class);
	
	/**
	 * 
	 * 
	 * @param log
	 */
	public static void info(String log) {
		logger.log(LogUtil.class.getName(), Level.INFO, log, null);
	}

	public static void error(String log, Throwable e) {
		logger.log(LogUtil.class.getName(), Level.ERROR, log, e);
	}

	public static void error(Throwable e) {
		logger.log(LogUtil.class.getName(), Level.ERROR, "", e);
	}

	public static void error(String log) {
		logger.log(LogUtil.class.getName(), Level.ERROR, log, null);
	}
	
	public static void debug(String log) {
		logger.log(LogUtil.class.getName(), Level.INFO, log, null);
	}
	
	public static void warn(String log) {
		logger.log(LogUtil.class.getName(), Level.WARN, log, null);
	}
}
