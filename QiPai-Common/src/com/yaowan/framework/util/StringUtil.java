package com.yaowan.framework.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StringUtil {

	/**
	 * 分割符"_"
	 */
	public static final String DELIMITER_INNER_ITEM = "_";
	/**
	 * 分割符","
	 */
	private static final String REGEX_DOU_HAO = "\\|";
	/**
	 * 分割符","
	 */
	public static final String DELIMITER_BETWEEN_ITEMS = "|";
	/**
	 * 分隔符逗号
	 */
	public static final String DELIMITER_COMMA = ",";
	
	/**
	 * 首字母小写
	 *
	 * @author ruan
	 * @param str
	 * @return
	 */
	public final static String firstToLowerCase(String str) {
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}

	/**
	 * 首字母大写
	 *
	 * @author ruan
	 * @param str
	 * @return
	 */
	public final static String firstToUpperCase(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	/**
	 * 字符串是否为null或空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isStringEmpty(String str) {
		if (str == null || str.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 把数组Object[]组装成以_分割的字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String arrayToString(Object[] objs) {
		StringBuilder bf = null;
		if (objs != null && objs.length > 0) {
			for (Object obj : objs) {
				if (obj != null && !obj.equals("")) {
					if (bf == null) {
						bf = new StringBuilder();
					} else {
						bf.append(DELIMITER_INNER_ITEM);
					}
					bf.append(obj);
				}
			}
		}
		return bf == null ? null : bf.toString();
	}

	/**
	 * 把数组Object[]组装成以指定分割的字符串
	 * 
	 * @param obj
	 * @param regex
	 * @return
	 */
	public static String arrayToString(Object[] objs, String regex) {
		StringBuilder bf = null;
		if (objs != null && objs.length > 0) {
			for (Object obj : objs) {
				if (obj != null && !obj.equals("")) {
					if (bf == null) {
						bf = new StringBuilder();
					} else {
						bf.append(regex);
					}
					bf.append(obj);
				}
			}
		}
		return bf == null ? null : bf.toString();
	}

	/**
	 * 把List<T>组装成以指定分割的字符串
	 * 		T为Long、Integer、String时使用
	 * @param obj
	 * @param regex
	 * @return
	 */
	public static <T> String listToString(List<T> list, String regex) {
		if (CommonUtils.isCollectionEmpty(list)) {
			return "";
		}
		StringBuilder bf = null;
		for (T value : list) {
			if (bf == null) {
				bf = new StringBuilder();
			} else {
				bf.append(regex);
			}
			bf.append(value);
		}
		return bf == null ? "" : bf.toString();
	}

	/**
	 * 将list解释成a_b_c,d_e_f格式
	 * 
	 * @param <T>
	 * @param list
	 * @return
	 */
	public static <T> String listArrayToString(List<T[]> list) {
		return listArrayToString(list, ",", "_");
	}

	/**
	 * 把List<T[]>组装成以指定分割的字符串
	 * 
	 * @param obj
	 * @param itemsRegex
	 *            项与项之间的分割符,例如上面的|
	 * @param valueRegex
	 *            项值之间的分割符,例如上面的_
	 * @return
	 */
	public static <T> String listArrayToString(List<T[]> list,
			String itemsRegex, String valueRegex) {
		if (CommonUtils.isCollectionEmpty(list)) {
			return null;
		}
		StringBuilder bf = null;
		for (T[] valueArray : list) {
			if (bf == null) {
				bf = new StringBuilder();
			} else {
				bf.append(itemsRegex);
			}
			int i = 0;
			for (T value : valueArray) {
				if (i > 0) {
					bf.append(valueRegex);
				}
				i++;
				bf.append(value);
			}
		}
		return bf == null ? null : bf.toString();
	}

	/**
	 * 把以_分割的字符串分解成数组
	 * 
	 * @param str
	 * @return
	 */
	public static <T> T[] stringToArray(String str, Class<T> clz) {
		return stringToArray(str, DELIMITER_INNER_ITEM, clz);
	}

	/**
	 * 把以1_2_3,4_5_6分割的字符串分解成List<T[]>
	 * 
	 * @param str
	 * @return
	 */
	public static <T> List<T[]> stringToListArray(String str, Class<T> clz) {
		return stringToListArray(str, clz, REGEX_DOU_HAO, DELIMITER_INNER_ITEM);
	}

	/**
	 * 把类似字符串a_b_c|e_f_g分割成List<T[]>
	 * 
	 * @param <T>
	 * @param str
	 * @param clz
	 *            最终值的类型
	 * @param itemsRegex
	 *            项与项之间的分割符,例如上面的|
	 * @param valueRegex
	 *            项值之间的分割符,例如上面的_
	 * @return 没有数据返回空元素的集合
	 */
	public static <T> List<T[]> stringToListArray(String str, Class<T> clz,
			String itemsRegex, String valueRegex) {
		if (CommonUtils.isNull(str)) {
			return new ArrayList<T[]>();
		}
		String[] arr = split(str, itemsRegex);
		List<T[]> result = new ArrayList<T[]>(arr.length);
		for (String value : arr) {
			result.add((T[]) stringToArray(value, valueRegex, clz));
		}
		return result;
	}

	/**
	 * 把类似字符串a|b|c分割成List<T>
	 * 
	 * @param <T>
	 * @param str
	 * @param clz
	 *            最终值的类型
	 * @param itemsRegex
	 *            项与项之间的分割符,例如上面的|
	 * @return 没有数据返回空元素的集合
	 */
	public static <T> List<T> stringToList(String str, String itemsRegex,
			Class<T> clz) {
		if (CommonUtils.isNull(str)) {
			return new ArrayList<T>();
		}
		String[] arr = split(str, itemsRegex);
		List<T> result = new ArrayList<T>(arr.length);
		for (String value : arr) {
			result.add(getValueByStr(value, clz));
		}
		return result;
	}

	/**
	 * 把以regex分割的字符串分解成数组
	 * 
	 * @param str
	 * @return
	 */
	public static <T> T[] stringToArray(String str, String regex, Class<T> cls) {
		if (str == null || str.length() == 0) {
			return null;
		}
		String[] arr = split(str, regex);
		return stringToArray(arr, cls, 0);
	}

	/**
	 * 把以regex分割的字符串分解成数组
	 * 
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> T[] stringToArray(String[] scrValueArray, Class<T> cls,
			int startIndex) {
		if (scrValueArray == null || scrValueArray.length == 0) {
			return null;
		}
		int j = 0;
		if (cls == Integer.class) {
			Integer[] result = new Integer[scrValueArray.length - startIndex];
			for (int i = startIndex; i < scrValueArray.length; i++) {
				result[j] = Integer.parseInt(scrValueArray[i]);
				j++;
			}
			return (T[]) result;
		} else if (cls == Float.class) {
			Float[] result = new Float[scrValueArray.length - startIndex];
			for (int i = startIndex; i < scrValueArray.length; i++) {
				result[j] = Float.parseFloat(scrValueArray[i]);
				j++;
			}
			return (T[]) result;
		} else if (cls == Long.class) {
			Long[] result = new Long[scrValueArray.length - startIndex];
			for (int i = startIndex; i < scrValueArray.length; i++) {
				result[j] = Long.parseLong(scrValueArray[i]);
				j++;
			}
			return (T[]) result;
		} else if (cls == String.class) {
			if (startIndex == 0) {
				return (T[]) scrValueArray;
			} else {
				String[] result = new String[scrValueArray.length - startIndex];
				for (int i = startIndex; i < scrValueArray.length; i++) {
					result[j] = scrValueArray[i];
					j++;
				}
			}
		}
		return null;
	}

	/**
	 * 把以1_2|1_3类似格式的字符串分解成HashMap<K,V>
	 * 
	 * @param str
	 * @param itemsRegex
	 *            项与项之间的分割符,例如上面的|
	 * @param valueRegex
	 *            项值之间的分割符,例如上面的_
	 * 
	 * @param keyClz
	 *            key类型
	 * @param valueClz
	 *            value类型
	 * @return 没有数据返回空元素的集合
	 */
	public static <K, V> Map<K, V> stringToMap(String str, String itemsRegex,
			String valueRegex, Class<K> keyClz, Class<V> valueClz) {
		if (CommonUtils.isNull(str)) {
			return new HashMap<K, V>();
		}
		String[] arr = split(str, itemsRegex);
		Map<K, V> map = new HashMap<K, V>(arr.length);
		for (String s : arr) {
			String[] subArr = split(s, valueRegex);
			map.put(getValueByStr(subArr[0], keyClz),
					getValueByStr(subArr[1], valueClz));
		}
		return map;
	}

	/**
	 * 把以1_2|1_3类似格式的字符串分解成HashMap<K,V>
	 * 
	 * @param str
	 * @param itemsRegex
	 *            项与项之间的分割符,例如上面的|
	 * @param valueRegex
	 *            项值之间的分割符,例如上面的_
	 * 
	 * @param keyClz
	 *            key类型
	 * @param valueClz
	 *            value类型
	 * @return 没有数据返回空元素的集合
	 */
	public static <K, V> LinkedHashMap<K, V> stringToLinkedMap(String str,
			String itemsRegex, String valueRegex, Class<K> keyClz,
			Class<V> valueClz) {
		if (CommonUtils.isNull(str)) {
			return new LinkedHashMap<K, V>();
		}
		String[] arr = split(str, itemsRegex);
		LinkedHashMap<K, V> map = new LinkedHashMap<K, V>(arr.length);
		for (String s : arr) {
			String[] subArr = split(s, valueRegex);
			if (subArr.length == 2) {
				map.put(getValueByStr(subArr[0], keyClz),
						getValueByStr(subArr[1], valueClz));
			}
		}
		return map;
	}

	/**
	 * 把以1_2_3|2_3_4类似格式的字符串分解成HashMap<K,List<V>>
	 * 
	 * @param str
	 * @param itemsRegex
	 *            项与项之间的分割符,例如上面的|
	 * @param valueRegex
	 *            项值之间的分割符,例如上面的_
	 * 
	 * @param keyClz
	 *            key类型
	 * @param valueClz
	 *            value类型
	 * @return 没有数据返回空元素的集合
	 */
	public static <K, V> Map<K, List<V>> stringToMapList(String str,
			String itemsRegex, String valueRegex, Class<K> keyClz,
			Class<V> valueClz) {
		if (CommonUtils.isNull(str)) {
			return new HashMap<K, List<V>>();
		}
		String[] arr = split(str, itemsRegex);
		Map<K, List<V>> map = new HashMap<K, List<V>>(arr.length);
		for (String s : arr) {
			String[] subArr = split(s, valueRegex);
			List<V> list = new ArrayList<V>(subArr.length - 1);
			for (int i = 1; i < subArr.length; i++) {
				list.add(getValueByStr(subArr[i], valueClz));
			}
			map.put(getValueByStr(subArr[0], keyClz), list);
		}
		return map;
	}

	/**
	 * 把以1_2_3|2_3_4类似格式的字符串分解成HashMap<K,List<V>>
	 * 
	 * @param str
	 * @param itemsRegex
	 *            项与项之间的分割符,例如上面的|
	 * @param valueRegex
	 *            项值之间的分割符,例如上面的_
	 * 
	 * @param keyClz
	 *            key类型
	 * @param valueClz
	 *            value类型
	 * @return 没有数据返回空元素的集合
	 */
	public static <K, V> Map<K, V[]> stringToMapArray(String str,
			String itemsRegex, String valueRegex, Class<K> keyClz,
			Class<V> valueClz) {
		if (CommonUtils.isNull(str)) {
			return new HashMap<K, V[]>();
		}
		String[] arr = split(str, itemsRegex);
		Map<K, V[]> map = new HashMap<K, V[]>(arr.length);
		for (String s : arr) {
			String[] subArr = split(s, valueRegex);
			map.put(getValueByStr(subArr[0], keyClz),
					stringToArray(subArr, valueClz, 1));
		}
		return map;
	}

	/**
	 * 把HashMap分解成以1_a|1_b格式的字符串
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V> String mapArrayToString(Map<K, V[]> map,
			String itemsRegex, String valueRegex) {
		StringBuilder bf = null;
		if (map != null && map.size() > 0) {
			for (K key : map.keySet()) {
				V[] array = map.get(key);
				if (array == null || array.length == 0) {
					continue;
				}
				if (bf == null) {
					bf = new StringBuilder();
				} else {
					bf.append(itemsRegex);
				}
				bf.append(key);
				for (V obj : array) {
					bf.append(valueRegex).append(obj);
				}
			}
		}
		return bf == null ? null : bf.toString();
	}

	/**
	 * 把HashMap分解成以1_a|1_b格式的字符串
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V> String mapListToString(Map<K, List<V>> map,
			String itemsRegex, String valueRegex) {
		StringBuilder bf = null;
		if (map != null && map.size() > 0) {
			for (K key : map.keySet()) {
				List<V> list = map.get(key);
				if (CommonUtils.isCollectionEmpty(list)) {
					continue;
				}
				if (bf == null) {
					bf = new StringBuilder();
				} else {
					bf.append(itemsRegex);
				}
				bf.append(key);
				for (V obj : list) {
					bf.append(valueRegex).append(obj);
				}
			}
		}
		return bf == null ? null : bf.toString();
	}

	/**
	 * 分割字符串
	 * 
	 * @param str
	 * @param regex
	 * @return
	 */
	public static String[] split(String str, String regex) {
		if (regex.equals("|")) {
			regex = "\\|";
		} else if (regex.equals(",")) {
			regex = REGEX_DOU_HAO;
		} else if (regex.equals("，")) {
			regex = REGEX_DOU_HAO;
		} else if (regex.equals("^")) {
			regex = "\\^";
		}
		return str.split(regex);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getValueByStr(String str, Class<T> clz) {
		if (clz == Integer.class) {
			return (T) Integer.valueOf(str);
		} else if (clz == Byte.class) {
			return (T) Byte.valueOf(str);
		} else if (clz == Float.class) {
			return (T) Float.valueOf(str);
		} else if (clz == Double.class) {
			return (T) Double.valueOf(str);
		} else if (clz == Boolean.class) {
			return (T) Boolean.valueOf(str);
		} else if (clz == Short.class) {
			return (T) Short.valueOf(str);
		} else if (clz == Long.class) {
			return (T) Long.valueOf(str);
		} else if (clz == String.class) {
			return (T) str;
		}
		return null;
	}

	/**
	 * 把以1_2,1_3类似格式的字符串分解成HashMap<K,V>
	 * 		V为Long、Integer、String时使用
	 * @param str
	 * @param keyClz
	 *            key类型
	 * @param valueClz
	 *            value类型
	 * @return
	 */
	public static <K, V> Map<K, V> stringToMap(String str, Class<K> keyClz,
			Class<V> valueClz) {
		return stringToMap(str, REGEX_DOU_HAO, DELIMITER_INNER_ITEM, keyClz,
				valueClz);
	}

	/**
	 * 把HashMap分解成以1_a|1_b格式的字符串
	 * 		V为Long、Integer、String时使用
	 * @param map
	 * @return
	 */
	public static <K, V> String mapToString(Map<K, V> map) {
		return map2String(map, DELIMITER_BETWEEN_ITEMS, DELIMITER_INNER_ITEM);
	}

	/**
	 * 把HashMap分解成以1_a|1_b格式的字符串
	 * 		V为Long、Integer、String时使用
	 * @param map
	 * @return
	 */
	public static <K, V> String map2String(Map<K, V> map, String itemsRegex,
			String valueRegex) {
		StringBuilder bf = null;
		if (map != null && map.size() > 0) {
			for (K key : map.keySet()) {
				V obj = map.get(key);
				if (obj != null) {
					if (bf == null) {
						bf = new StringBuilder();
					} else {
						bf.append(itemsRegex);
					}
					bf.append(key).append(valueRegex).append(obj);
				}
			}
		}
		return bf == null ? null : bf.toString();
	}

	/**
	 * 替换字符串变量
	 * 
	 * @param src
	 *            源字符串
	 * @param param
	 *            变量
	 * @param paramValue
	 *            变量值数值
	 * @return
	 */
	public static String replaceString(String src, String param,
			Object... paramValue) {
		if (paramValue == null || paramValue.length == 0) {
			return src;
		}
		StringBuilder sb = new StringBuilder(src);
		// 变量参数
		int paramLength = param.length();
		int index = 0;
		for (Object value : paramValue) {
			index = sb.indexOf(param);
			if (index < 0) {
				break;
			}
			sb.replace(index, index + paramLength, value.toString());
		}
		return sb.toString();
	}
	
	/**
	 * 把类似字符串a|b|c分割成Set<T>
	 * 
	 * @param <T>
	 * @param str
	 * @param itemsRegex
	 * @param clz
	 * @return
	 */
	public static <T> Set<T> stringToSet(String str, String itemsRegex, Class<T> clz) {
		if (CommonUtils.isNull(str)) {
			return new HashSet<T>();
		}
		String[] arr = split(str, itemsRegex);
		Set<T> result = new HashSet<T>(arr.length);
		for (String value : arr) {
			result.add(getValueByStr(value, clz));
		}
		return result;
	}
	
	/**
	 * 把Set<T>组装成以指定分割的字符串
	 * 		T为Long、Integer、String时使用
	 * @param <T>
	 * @param set
	 * @param regex
	 * @return
	 */
	public static <T> String setToString(Set<T> set, String regex) {
		if (CommonUtils.isCollectionEmpty(set)) {
			return null;
		}
		StringBuilder bf = null;
		for (T value : set) {
			if (bf == null) {
				bf = new StringBuilder();
			} else {
				bf.append(regex);
			}
			bf.append(value);
		}
		return bf == null ? null : bf.toString();
	}
	
	
	/**
	 *
	 * @param obj
	 * @return
	 */
	public final static int getInt(Object obj) {
		return (int) getDouble(obj);
	}

	/**
	 *
	 * @param obj
	 * @return
	 */
	public final static byte getByte(Object obj) {
		return (byte) getDouble(obj);
	}

	/**
	 *
	 * @param obj
	 * @return
	 */
	public final static char getChar(Object obj) {
		return (char) getDouble(obj);
	}

	/**
	 * getShort
	 *
	 * @author ruan
	 * @param str
	 * @return
	 */
	public final static short getShort(Object obj) {
		return (short) getDouble(obj);
	}

	/**
	 * getLong
	 *
	 * @param str
	 * @return
	 */
	public final static long getLong(Object obj) {
		if (obj == null) {
			return 0;
		}

		if (obj instanceof Long) {
			return (long) obj;
		}

		try {
			return new BigDecimal(obj.toString()).longValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (long) getDouble(obj);
	}

	/**
	 * getDouble
	 *
	 * @param str
	 * @return
	 */
	public final static double getDouble(Object obj) {
		if (obj == null) {
			return 0;
		}
		try {
			return Double.parseDouble(obj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * getFloat
	 *
	 * @param str
	 * @return
	 */
	public final static float getFloat(Object obj) {
		return (float) getDouble(obj);
	}

	/**
	 *
	 * @param obj
	 * @return
	 */
	public final static boolean getBoolean(Object obj) {
		if (obj != null) {
			try {
				return Boolean.parseBoolean(obj.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
