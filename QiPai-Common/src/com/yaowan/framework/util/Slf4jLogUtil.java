package com.yaowan.framework.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * 基于性能考虑, 在warn error exception debug 模式下,程序会讲object自动转成json格式
 * 
 * (注意: info不会将object转成json格式)
 * 
 * @author JeffieChan
 * @version 2017年3月1日 下午4:55:08
 */
public class Slf4jLogUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jLogUtil.class);

	/**
	 * 
	 * @param logger
	 * @param ex
	 * @param tag
	 */
	public static void exception(Exception ex, String tag, String text, Object... params) {
		if (params == null || params.length == 0) {
			LOGGER.error(tag + " - t:`" + text + "`");
			return;
		}
		String[] keys = getKeys(params);
		Object[] values = getValues(params);
		String paraName = generateParaName(keys);
		LOGGER.error(tag + " - t:`" + text + "` - p:{" + paraName + "}", toJson(values), ex);
	}

	/**
	 * 
	 * @param logger
	 * @param ex
	 * @param tag
	 */
	public static void error(String tag, String text, Object... params) {
		if (params == null || params.length == 0) {
			LOGGER.error(tag + " - t:`" + text + "`");
			return;
		}
		String[] keys = getKeys(params);
		Object[] values = getValues(params);
		String paraName = generateParaName(keys);
		LOGGER.error(tag + " - t:`" + text + "` - p:{" + paraName + "}", toJson(values));
	}

	public static void warn(String tag, String text, Object... params) {
		if (params == null || params.length == 0) {
			LOGGER.warn(tag + " - t:`" + text + "`");
			return;
		}
		String[] keys = getKeys(params);
		Object[] values = getValues(params);
		String paraName = generateParaName(keys);
		LOGGER.warn(tag + " - t:`" + text + "` - p:{" + paraName + "}", toJson(values));
	}

	public static void info2in(String tag, Object... params) {
		info(tag, "IN", params);
	}

	public static void info2out(String tag, Object... params) {
		info(tag, "OUT", params);
	}

	public static void success(String tag, String text, Object... params) {
		info(tag, text, params, "result", "SUCCESS");
	}

	public static void fail(String tag, String text, Object... params) {
		warn(tag, text, params, "result", "FAIL");
	}

	public static void info(String tag, String text, Object... params) {

		if (params == null || params.length == 0) {
			LOGGER.info(tag + " - t:`" + text + "`");
			return;
		}
		if (params.length % 2 != 0) {
			try {
				LOGGER.info(tag + " - t:`" + text + "` - p:{" + toJson(params) + "}");
			} catch (Exception ex) {
				exception(ex, tag, "Parameter mismatch , Convert  to JSON exception., params");
			}
			return;
		}

		String[] keys = getKeys(params);
		Object[] values = getValues(params);
		String paraName = generateParaName(keys);
		LOGGER.info(tag + " - t:`" + text + "` - p:{" + paraName + "}", values);
	}

	public static void debug(String tag, String text, Object... params) {
		if (params == null || params.length == 0) {
			LOGGER.info(tag + " - t:`" + text + "`");
			return;
		}

		String[] keys = getKeys(params);
		Object[] values = getValues(params);

		String paraName = generateParaName(keys);
		LOGGER.debug(tag + " - t:`" + text + "` - p:{" + paraName + "}", toJson(values));
	}

	public static <V> Map<String, V> getMap(Object[] objects) {
		String[] keys = getKeys(objects);
		V[] values = getValues(objects);
		int minl = keys.length;

		if (keys.length != values.length) {
			LOGGER.error("getMap(...) exception,keys.length != values.length");
			/*
			 * key 和val 的 length 不相等时,取 leng最小值作为循环的max.
			 */
			minl = keys.length <= values.length ? keys.length : values.length;
		}

		Map<String, V> map = new HashMap<>();

		for (int i = 0; i < minl; i++) {
			map.put(keys[i], values[i]);
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	private static <V> V[] getValues(Object[] objects) {
		if (objects.length <= 1) {
			return (V[]) objects;
		}
		Object[] values = new Object[objects.length / 2];
		int j = 0;
		for (int i = 0; i < objects.length; i++) {
			if ((i + 1) % 2 == 0) {
				values[j] = objects[i];
				j++;
			}
		}
		return (V[]) values;
	}

	private static String[] getKeys(Object[] objects) {
		if (objects.length <= 1) {
			String key = objects.length == 1 ? objects[0].toString() : "key";
			return new String[] { key };
		}
		String[] keys = new String[objects.length / 2];
		int j = 0;
		for (int i = 0; i < objects.length; i++) {
			if ((i + 1) % 2 != 0) {
				keys[j] = objects[i].toString();
				j++;
			}
		}
		return keys;
	}

	private static String generateParaName(String[] keys) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < keys.length; i++) {
			sb.append(keys[i] + "={}");
			if (i != keys.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	private static Object[] toJson(Object... obj) {
		Object[] strs = new Object[obj.length];
		for (int i = 0; i < obj.length; i++) {
			if (obj[i] instanceof String || NumberUtils.isNumber(obj[i].toString())) {
				strs[i] = obj[i];
			} else {
				strs[i] = JSON.toJSONString(obj[i]);
			}
		}
		return strs;
	}

	public static class SimpleLogUtil {

		private static String convertStr(Object tag) {
			return tag.getClass().toString();
		}

		public static void exception(Exception ex, Object tag, String text) {
			Slf4jLogUtil.exception(ex, convertStr(tag), text);
		}

		public static void exception(Exception ex, Object tag, String text, Object... params) {
			Slf4jLogUtil.exception(ex, convertStr(tag), text, params);
		}

		public static void warn(Object tag, String text, Object... params) {
			Slf4jLogUtil.warn(convertStr(tag), text, params);
		}

		public static void error(Object tag, String text, Object... params) {
			Slf4jLogUtil.error(convertStr(tag), text, params);
		}

		public static void info2in(Object tag, Object... params) {
			Slf4jLogUtil.info2in(convertStr(tag), params);
		}

		public static void info2out(Object tag, Object... params) {
			Slf4jLogUtil.info2out(convertStr(tag), params);
		}

		public static void success(Object tag, String text, Object... params) {
			Slf4jLogUtil.success(convertStr(tag), text, params);
		}

		public static void fail(Object tag, String text, Object... params) {
			Slf4jLogUtil.fail(convertStr(tag), text, params);
		}

		public static void info(Object tag, String text, Object... params) {
			Slf4jLogUtil.info(convertStr(tag), text, params);
		}

		public static void debug(Object tag, String text, Object... params) {
			Slf4jLogUtil.debug(convertStr(tag), text, params);
		}
	}

}
