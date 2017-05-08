package com.yaowan.framework.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 排序类，通用于获取的对象的方法值 都是int类型
 * 
 * @author chenjy
 * 
 */
public final class SortUtil {
	/**
	 * 装载已经用过的规则 实现类似单例模式
	 */
	private static ConcurrentHashMap<String, SortUtil> sortMap = new ConcurrentHashMap<String, SortUtil>();

	private Method[] methodArr = null;
	private int[] typeArr = null;
	private Order order = null;

	/**
	 * 构造函数 并保存该规则
	 * 
	 * @param clazz
	 * @param args
	 */
	private <T> SortUtil(Class<T> clazz, String... args) {
		methodArr = new Method[args.length];
		typeArr = new int[args.length];
		for (int i = 0; i < args.length; i++) {
			String key = args[i].split("#")[0];
			try {
				methodArr[i] = clazz.getMethod(key, new Class[] {});
				typeArr[i] = Integer.parseInt((args[i].split("#")[1]));
			} catch (Exception e) {
				LogUtil.error(e);
			}
		}
	}
	
	/**
	 * 构造函数 ，设置排序规则
	 * @param order
	 */
	private <T> SortUtil(Order order) {
		this.order = order;
	}

	/**
	 * 对象排序规则
	 * 
	 * @author Rain 2012-03-13
	 */
	private final Comparator<Object> comparatorObject = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			for (int i = 0; i < methodArr.length; i++) {
				try {
					Object value1 = methodArr[i].invoke(o1);
					Object value2 = methodArr[i].invoke(o2);
					double double1 = 0;
					double double2 = 0;

					if (value1 instanceof Integer) {
						double1 = (Integer) value1;
						double2 = (Integer) value2;
					} else if (value1 instanceof Boolean) {
						double1 = (Boolean) value1 ? 1 : -1;
						double2 = (Boolean) value2 ? 1 : -1;
					} else if (value1 instanceof Double) {
						double1 = (Double) value1;
						double2 = (Double) value2;
					} else if (value1 instanceof Float) {
						double1 = (Float) value1;
						double2 = (Float) value2;
					} else if (value1 instanceof Long) {
						double1 = (Long) value1;
						double2 = (Long) value2;
					} else if (value1 instanceof Short) {
						double1 = (Short) value1;
						double2 = (Short) value2;
					} else if (value1 instanceof Byte) {
						double1 = (Byte) value1;
						double2 = (Byte) value2;
					} else {
						double1 = value1.toString().compareToIgnoreCase(value2.toString());
						double2 = -double1;
					}
					if (double1 == double2) {
						continue;
					}
					if (typeArr[i] == 1) {
						return (double1 > double2) ? 1 : -1;
					} else {
						return (double1 > double2) ? -1 : 1;
					}
				} catch (Exception e) {
					LogUtil.error(e);
				}
			}
			return 0;
		}
	};
	
	/**
	 * 非对象排序规则
	 */
	private final Comparator<Object> comparatorValue = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			int result = 0;
			if (o1 instanceof Integer) {
				result = Integer.parseInt(o1.toString()) - Integer.parseInt(o2.toString());
			} else if (o1 instanceof Boolean) {
				result = o1.toString().compareToIgnoreCase(o2.toString());
			} else if (o1 instanceof Double) {
				result = Double.parseDouble(o1.toString()) > Double.parseDouble(o2.toString()) ? 1 : -1;
			} else if (o1 instanceof Float) {
				result = Float.parseFloat(o1.toString()) > Float.parseFloat(o2.toString()) ? 1 : -1;
			} else if (o1 instanceof Long) {
				result = Long.parseLong(o1.toString()) > Long.parseLong(o2.toString()) ? 1 : -1;
			} else if (o1 instanceof Short) {
				result = Short.parseShort(o1.toString()) - Short.parseShort(o2.toString());
			} else if (o1 instanceof Byte) {
				result = Byte.parseByte(o1.toString()) - Byte.parseByte(o2.toString());
			} else {
				result = Double.parseDouble(o1.toString()) > Double.parseDouble(o2.toString()) ? 1 : -1;
			}
			return order.equals(Order.ASC) ? result : -result;
		}
	};

	/**
	 * 获取排序规则
	 * 
	 * @return SortUtil
	 */
	private static <T> SortUtil getSort(Class<T> clazz, String... args) {
		String key = clazz.getName() + Arrays.toString(args);
		SortUtil sort = sortMap.get(key);
		if(sort == null){
			sort = new SortUtil(clazz, args);
			sortMap.put(key, sort);
		}
		return sort;
	}
	
	/**
	 * 获取排序规则
	 * @author ruan
	 * @param clazz
	 * @param order
	 * @return
	 */
	private static <T> SortUtil getSort(Class<T> clazz, Order order) {
		String key = clazz.getName() + order;
		SortUtil sort = sortMap.get(key);
		if(sort == null){
			sort = new SortUtil(order);
			sortMap.put(key, sort);
		}
		return sort;
	}

	/**
	 * 对对象数组进行排序
	 * <pre>
	 * 首先会在容器中，根据class+规则去找。如果没有见则new 
	 * 调用方式 SortUtil.sort(list,"方法名#升序(1)/降序(-1)","..","..")
	 * 后面字符串参数：比如："getMark#1","getAge#-1"
	 * 表示先按照getMark的值按照升序排，如果相等再按照getAge的降序排
	 * 如果返回值是true类型，若按照true先排："isOnline#1" ,若按照false先排："isOnline#-1"
	 * </pre>
	 * 
	 * @param list
	 * @param args
	 */
	public final static <T> void sort(List<T> list, String... args) {
		if (list == null || list.size() == 0 || args.length == 0) {
			return;
		}
		SortUtil sort = getSort(list.get(0).getClass(), args);
		Collections.sort(list, sort.comparatorObject);
	}
	
	/**
	 * 对非对象数组进行排序(多用于数值型)
	 * @author ruan
	 * @param list
	 * @param order
	 */
	public static <T> void sort(List<T> list, Order order) {
		if (list == null || list.size() == 0 || order == null) {
			return;
		}
		SortUtil sort = getSort(list.get(0).getClass(), order);
		Collections.sort(list, sort.comparatorValue);
	}

	/**
	 * 给Map进行排序 对map的value进行排序(对象)
	 * 
	 * @param map 被排序的map
	 * @param args 排序方法条件：方法名x#1升序-1倒序, 方法名y#-1倒序
	 * @return List<T>
	 */
	public static <T, F> List<F> sortMap(Map<T, F> map, String... args) {
		List<F> list = new ArrayList<F>();
		if (map == null || map.isEmpty()) {
			return list;
		}
		list.addAll(map.values());
		sort(list, args);
		return list;
	}
	
	/**
	 * 给Map进行排序 对map的value进行排序(非对象)
	 * 
	 * @param map 被排序的map
	 * @param args 排序方法条件：方法名x#1升序-1倒序, 方法名y#-1倒序
	 * @return List<T>
	 */
	public static <T, F> List<F> sortMap(Map<T, F> map, Order order) {
		List<F> list = new ArrayList<F>();
		if (map == null || map.isEmpty()) {
			return list;
		}
		list.addAll(map.values());
		sort(list, order);
		return list;
	}

	/**
	 * 排序方式
	 * 
	 * @author ruan
	 * 
	 */
	public enum Order {
		/**
		 * 升序
		 */
		ASC,
		/**
		 * 反序
		 */
		DESC;
	}
}