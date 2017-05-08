package com.yaowan.framework.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yaowan.framework.database.annotation.Transient;

/**
 * 正则表达式操作(增删改查等)工具类
 * 
 * @author Kyle
 * 
 */
public class RegexUtil {
	/**
	 * int基本类型
	 */
	private static final String intBase = "int";
	/**
	 * byte基本类型
	 */
	private static final String byteBase = "byte";
	/**
	 * short基本类型
	 */
	private static final String shortBase = "short";
	/**
	 * boolean基本类型
	 */
	private static final String booleanBase = "boolean";
	/**
	 * long基本类型
	 */
	private static final String longBase = "long";

	/**
	 * float基本类型
	 */
	private static final String floatBase = "float";

	/**
	 * 字符串String
	 */
	private static final String stringWrap = "java.lang.String";
	/**
	 * Long包装类型
	 */
	private static final String longWrap = "java.lang.Long";
	/**
	 * Integer包装类型
	 */
	private static final String intWrap = "java.lang.Integer";
	/**
	 * Byte包装类型
	 */
	private static final String byteWrap = "java.lang.Byte";
	/**
	 * Short包装类型
	 */
	private static final String shortWrap = "java.lang.Short";
	/**
	 * Float包装类型
	 */
	private static final String floatWrap = "java.lang.Float";

	/**
	 * 对象之前标志符
	 */
	private static final String OBJ_SYMBOL = ",";

	/**
	 * 字符串每一条记录之间的分隔符
	 */
	private static final String SPLIT_SYMBOL = "_";

	private static Map<Class<?>, Field[]> classFileds = new HashMap<Class<?>, Field[]>(
			60);

	private static Set<Field> ingornFields = new HashSet<Field>(50);

	@SuppressWarnings("rawtypes")
	private static Field[] getClassFileds(Class clz) {
		Field[] fields = classFileds.get(clz);
		if (fields == null) {
			fields = clz.getDeclaredFields();
			for (Field field : fields) {
				if (field.getAnnotation(Transient.class) != null) {
					ingornFields.add(field);
				}
			}
			classFileds.put(clz, fields);
		}
		return fields;
	}

	/**
	 * 是否忽略的属性
	 * 
	 * @param field
	 * @return
	 */
	private static boolean isIngornField(Field field) {
		return ingornFields.contains(field);
	}

	/**
	 * 根据传入对象转换为固定格式值字符串
	 * 
	 * @param obj
	 *            对象
	 * @return 转换后的字符串
	 */
	public static String objToString(Object obj) {
		StringBuilder sb = new StringBuilder();
		try {
			boolean isFirst = true;
			for (Field field : getClassFileds(obj.getClass())) {
				if (isIngornField(field)) {
					continue;
				}
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				if (isFirst) {
					isFirst = false;
				} else {
					sb.append(SPLIT_SYMBOL);
				}
				sb.append(field.get(obj));
			}
		} catch (IllegalArgumentException e) {
			LogUtil.error(e);
		} catch (IllegalAccessException e) {
			LogUtil.error(e);
		}
		return sb.toString();
	}

	/**
	 * 根据传入对象转换为固定格式值字符串
	 * 
	 * @param obj
	 *            对象
	 * @return 转换后的字符串
	 */
	public static String objToString(Object obj, String itemsRegex) {
		StringBuilder sb = new StringBuilder();
		try {
			boolean isFirst = true;
			for (Field field : getClassFileds(obj.getClass())) {
				if (isIngornField(field)) {
					continue;
				}
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				if (isFirst) {
					isFirst = false;
				} else {
					sb.append(itemsRegex);
				}
				sb.append(field.get(obj));
			}
		} catch (IllegalArgumentException e) {
			LogUtil.error(e);
		} catch (IllegalAccessException e) {
			LogUtil.error(e);
		}
		return sb.toString();
	}

	/**
	 * @param <T>
	 * @param valStr
	 * @param clazz
	 * @return
	 */
	public static <T> T stringToObj(String valStr, Class<T> clazz) {
		return stringToObj(valStr, clazz, SPLIT_SYMBOL);
	}

	/**
	 * @param <T>
	 * @param valStr
	 * @param clazz
	 * @param split
	 * @return
	 */
	public static <T> T stringToObj(String valStr, Class<T> clazz, String split) {
		if (CommonUtils.isNull(valStr)) {
			try {
				return clazz.newInstance();
			} catch (Exception e) {
				LogUtil.error(e);
				return null;
			}
		}
		T obj = null;
		int secs = 0;
		String[] valArr = StringUtil.split(valStr, split);
		try {
			obj = clazz.newInstance();
			for (Field field : getClassFileds(clazz)) {
				if (isIngornField(field)) {
					continue;
				}
				if (secs >= valArr.length) {
					continue;
				}
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				Class<?> cl = field.getType();
				String clName = cl.getName();
				// 判断对象中属性的类型
				if (stringWrap.equals(clName)) {
					if (!valArr[secs].equals("null")) {
						field.set(obj, valArr[secs]);
					}
				} else if (intBase.equals(clName) || intWrap.equals(clName)) {
					field.set(obj, Integer.parseInt(valArr[secs]));
				} else if (booleanBase.equals(clName)) {
					field.set(obj, Boolean.parseBoolean(valArr[secs]));
				} else if (longBase.equals(clName) || longWrap.equals(clName)) {
					field.set(obj, Long.parseLong(valArr[secs]));
				} else if (floatBase.equals(clName) || floatWrap.equals(clName)) {
					field.set(obj, Float.parseFloat(valArr[secs]));
				} else if(byteBase.equals(clName) || byteWrap.equals(clName)) {
					field.set(obj, Byte.parseByte(valArr[secs]));
				} else if(shortBase.equals(clName) || shortWrap.equals(clName)) {
					field.set(obj, Short.parseShort(valArr[secs]));
				}
				secs++;
			}
		} catch (Exception e) {
			LogUtil.error(e);
			LogUtil.info("错误的字符串:"+valStr);
		}
		return obj;
	}

	/**
	 * @param <T>
	 * @param data
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> stringToList(String data, Class<T> clazz) {
		return stringToList(data, clazz, OBJ_SYMBOL, SPLIT_SYMBOL);
	}

	/**
	 * @param <T>
	 * @param data
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> stringToList(String data, Class<T> clazz,
			String itemsRegex, String valueRegex) {
		if (CommonUtils.isNull(data)) {
			return new ArrayList<T>();
		}
		String[] array = StringUtil.split(data, itemsRegex);
		List<T> result = new ArrayList<T>(array.length);
		for (String str : array) {
			result.add(stringToObj(str, clazz, valueRegex));
		}
		return result;
	}

	/**
	 * @param <K>
	 * @param <T>
	 * @param data
	 * @param clazz
	 * @return
	 */
//	public static <K, T extends RegexMapObj<K>> Map<K, T> stringToMap(
//			String data, Class<T> clazz) {
//		if (CommonUtils.isNull(data)) {
//			return new HashMap<K, T>();
//		}
//		String[] array = StringUtil.split(data, OBJ_SYMBOL);
//		Map<K, T> result = new HashMap<K, T>(array.length);
//		for (String str : array) {
//			T obj = stringToObj(str, clazz);
//			result.put(obj.getId(), obj);
//		}
//		return result;
//	}
//
//	/**
//	 * @param <K>
//	 * @param <T>
//	 * @param data
//	 * @param clazz
//	 * @return
//	 */
//	public static <K, T extends RegexMapObj<K>> Map<K, T> stringToConcurrentMap(
//			String data, Class<T> clazz) {
//		if (CommonUtils.isNull(data)) {
//			return new ConcurrentHashMap<K, T>();
//		}
//		String[] array = StringUtil.split(data, OBJ_SYMBOL);
//		Map<K, T> result = new ConcurrentHashMap<K, T>(array.length);
//		for (String str : array) {
//			T obj = stringToObj(str, clazz);
//			result.put(obj.getId(), obj);
//		}
//		return result;
//	}
//
//	/**
//	 * 
//	 * @param <K>
//	 * @param <T>
//	 * @param data
//	 * @param clazz
//	 * @param itemsRegex
//	 * @param valueRegex
//	 * @return
//	 */
//	public static <K, T extends RegexMapObj<K>> Map<K, T> stringToMap(
//			String data, Class<T> clazz, String itemsRegex, String valueRegex) {
//		if (CommonUtils.isNull(data)) {
//			return new HashMap<K, T>();
//		}
//		String[] array = StringUtil.split(data, itemsRegex);
//		Map<K, T> result = new HashMap<K, T>(array.length);
//		for (String str : array) {
//			T obj = stringToObj(str, clazz, valueRegex);
//			result.put(obj.getId(), obj);
//		}
//		return result;
//	}

	/**
	 * Map转为字符串
	 * 		T为Long、Integer时候使用StringUtil.map2String
	 * 
	 * @param <K>
	 * @param <T>
	 * @param map
	 * @return
	 */
	public static <K, T> String mapToString(Map<K, T> map) {
		if (map == null) {
			return null;
		}
		return listToString(map.values());
	}

	/**
	 * Map转为字符串
	 * 		T为Long、Integer时候使用StringUtil.map2String
	 * 
	 * @param <K>
	 * @param <T>
	 * @param map
	 * @param itemsRegex
	 * @param valueRegex
	 * @return
	 */
	public static <K, T> String mapToString(Map<K, T> map, String itemsRegex,
			String valueRegex) {
		if (map == null) {
			return null;
		}
		return listToString(map.values(), itemsRegex, valueRegex);
	}

	/**
	 * List转为字符串
	 * 		T为Long、Integer时候使用StringUtil.listToString
	 * 
	 * @param <T>
	 * @param list
	 * @return
	 */
	public static <T> String listToString(Collection<T> list,
			String itemsRegex, String valueRegex) {
		if (CommonUtils.isCollectionEmpty(list)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		Field[] fields = null;
		boolean isFirstObj = true;
		for (T obj : list) {
			if (fields == null) {
				fields = getClassFileds(obj.getClass());
			}
			try {
				if (isFirstObj) {
					isFirstObj = false;
				} else {
					sb.append(itemsRegex);
				}
				int length = fields.length;
				boolean isFirst = true;
				for (int j = 0; j < length; j++) {
					Field field = fields[j];
					if (isIngornField(field)) {
						continue;
					}
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					if (isFirst) {
						isFirst = false;
					} else {
						sb.append(valueRegex);
					}
					sb.append(field.get(obj));
				}
			} catch (Exception e) {
				LogUtil.error(e);
			}
		}
		return sb.toString();
	}

	/**
	 * List转为字符串
	 * 		T为Long、Integer时候使用StringUtil.listToString
	 * 
	 * @param <T>
	 * @param list
	 * @return
	 */
	public static <T> String listToString(Collection<T> list) {
		return listToString(list, OBJ_SYMBOL, SPLIT_SYMBOL);
	}

}
