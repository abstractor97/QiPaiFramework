package com.yaowan.framework.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.yaowan.server.game.model.data.entity.Role;

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

	public static void info(Role role, String log) {
		logger.log(LogUtil.class.getName(), Level.INFO, "[rid=" + role.getRid() + "]" + log, null);
	}
	public static void error(String log, Throwable e) {
		logger.log(LogUtil.class.getName(), Level.ERROR, log, e);
	}

	public static void error(Throwable e) {
		logger.log(LogUtil.class.getName(), Level.ERROR, "", e);
	}
	
	public static void error(String[] strings){
		StringBuilder builder = new StringBuilder();
		for (String string : strings) {
			builder.append(string).append("\r\n");
		}
		error(builder.toString());
	}

	public static void error(String log) {
		logger.log(LogUtil.class.getName(), Level.ERROR, log, null);
	}

	public static void debug(String log) {
		logger.log(LogUtil.class.getName(), Level.DEBUG, log, null);
	}

	public static void debug(Role role, String log) {
		logger.log(LogUtil.class.getName(), Level.DEBUG, "[rid=" + role.getRid() + "]" + log, null);
	}

	public static void warn(String log) {
		logger.log(LogUtil.class.getName(), Level.WARN, log, null);
	}
}
