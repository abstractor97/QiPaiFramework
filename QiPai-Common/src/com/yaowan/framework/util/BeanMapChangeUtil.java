package com.yaowan.framework.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sun.misc.FloatConsts;

import com.google.gson.internal.bind.MapTypeAdapterFactory;

/**
 * bean和map相互转换
 * 
 * @author G_T_C
 */
public class BeanMapChangeUtil {

	/**
	 * map 转成 bean
	 * 
	 * @author G_T_C
	 * @param map
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static <T> T toBean(Map map, Class<T> clazz) throws Exception {
		T bean = clazz.newInstance();
		try {
			for (Object object : map.keySet()) {
				String s = object + "";
				Field field = clazz.getDeclaredField(s);
				Class c = field.getType();
				if (field != null) {
					field.setAccessible(true);
					if (String.class.equals(c)) {
						field.set(bean, map.get(object));
					} else if (Integer.class.equals(c) || int.class == c) {
						field.set(bean, Integer.parseInt(map.get(object) + ""));
					} else if (Long.class.equals(c) || long.class == c) {
						field.set(bean, Long.parseLong(map.get(object) + ""));
					} else if (Double.class.equals(c) || double.class == c) {
						field.set(bean,
								Double.parseDouble(map.get(object) + ""));
					} else if (Float.class.equals(c) || float.class == c) {
						field.set(bean, Float.parseFloat(map.get(object) + ""));
					} else if (Short.class.equals(c) || short.class == c) {
						field.set(bean, Short.parseShort(map.get(object) + ""));
					}

				}
			}
			return bean;
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return bean;
	}

	/**
	 * 通过私有变量，javaBean转成map
	 * 
	 * @author G_T_C
	 * @param domain
	 * @return
	 */
	public static Map<String, String> toMap(Object domain) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			Class<?> clazz = Class.forName(domain.getClass().getName());
			Field[] fileds = clazz.getDeclaredFields(); // 得到catClass类所有的属性（包括私有属性）
			for (Field field : fileds) {
				// 取消java语言访问检查,允许获取私有变量
				field.setAccessible(true);
				// 获取变量的类型名称
				// String returnType = field.getType().getName();
				// 获取变量的名称
				String fieldName = field.getName();
				// 获取当前对象的对应字段的值
				Object value = field.get(domain);
				/*
				 * if (returnType.equals(Date.class.getName())) { value =
				 * DateUtils.parseToDefaultDateTimeString((Date) value); }
				 */
				map.put(fieldName, value + "");
			}
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return map;
	}
}
