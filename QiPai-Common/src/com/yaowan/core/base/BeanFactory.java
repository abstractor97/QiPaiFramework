/**
 * 
 */
package com.yaowan.core.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.ServerConfig;
import com.yaowan.framework.database.db.DatabaseFactory;
import com.yaowan.framework.util.FileUtil;
import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 *
 */
public abstract class BeanFactory {
	
	/**
	 * 名字索引Bean
	 */
	private static LinkedHashMap<String, Object> beanNameMap = new LinkedHashMap<String, Object>();
	/**
	 * 类型索引Bean
	 */
	private static LinkedHashMap<Class<?>, Object> beanMap = new LinkedHashMap<Class<?>, Object>();
	
	public static void load(String... beanPaths) {
		for(String beanPath : beanPaths) {
			initSingleton(beanPath);
		}
		
		doAutowired();
		
		for(Object obj : beanMap.values()) {
			LogUtil.debug(obj.toString());
		}
		LogUtil.info("Load bean size : " + beanMap.size());
		//dubbo 数据工厂
		//DataContext context = new DataContext();
		//context.initialize();
	}
	
	private static void initSingleton(String classPath) {
		try {
			Set<Class<?>> clazzs = FileUtil.getClasses(classPath); 
			for(Class<?> clz : clazzs) {
				Component component = clz.getAnnotation(Component.class);
				if(component != null) {
					if(beanMap.containsKey(clz) || beanNameMap.containsKey(clz.getSimpleName())) {
						LogUtil.error("Duplicate bean register " + clz.getSimpleName());
						throw new RuntimeException("Duplicate bean register " + clz.getSimpleName());
					}
					Object instance = clz.newInstance();
					beanMap.put(clz, instance);
					beanNameMap.put(clz.getSimpleName(), instance);
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<T> clz) {
		if(clz.isInterface()) {
			for(Object tempObject : beanMap.values()) {
				if(clz.isAssignableFrom(tempObject.getClass())) {
					return (T) tempObject;
				}
			}
			return null;
		} else {
			return (T)beanMap.get(clz);
		}
	}
	
	public static Object getBean(String name) {
		return beanNameMap.get(name);
	}
	
	public static void doAutowired() {
		for(Entry<Class<?>, Object> entry : beanMap.entrySet()) {
			Object object = entry.getValue();
			Field[] fields = entry.getKey().getDeclaredFields();
			
			wireFields(object, fields);
			
			Class<?> superClass = entry.getKey().getSuperclass();
			while(superClass != null) {
				wireFields(object, superClass.getDeclaredFields());
				
				superClass = superClass.getSuperclass();
			}
		}
	}
	
	private static void wireFields(Object object, Field[] fields) {
		for(Field field : fields) {
			Autowired autowired = field.getAnnotation(Autowired.class);
			if(autowired == null) {
				continue;
			}
			try {
				field.setAccessible(true);
				Object wireObject = null;
				
//				if("".equals(autowired.value())) {
					//按类型进行注入
					wireObject = getBean(field.getType());
//				} else {
//					//按名字进行注入
//					wireObject = getBean(autowired.value());
//				}
				if(wireObject == null) {
					LogUtil.error(field.getDeclaringClass().getName() + " wiring " + field.getName() + " with NullPointer");
				} else {
					field.set(object, wireObject);
				}
			} catch (Exception e) {
				LogUtil.error(field.getDeclaringClass().getName() + " wiring " + field.getName() + " exception");
			} finally {
				field.setAccessible(false);
			}
		}
	}
	
	public static void dataSource(String jdbcProperties) {
		ResourceBundle rb = getResourceBundle(jdbcProperties);
		
		String baseId = propertyValue(rb, String.class, "baseId");
		String host = propertyValue(rb, String.class, "host");
		String user = propertyValue(rb, String.class, "user");
		String password = propertyValue(rb, String.class, "password");
		String baseName = propertyValue(rb, String.class, "baseName");
		int minConnections = propertyValue(rb, Integer.class, "minConnections");
		int maxConnections = propertyValue(rb, Integer.class, "maxConnections");
		
		DataSource dataSource = DatabaseFactory.create(host, baseName, user,
				password, minConnections, maxConnections);
		
		if(beanNameMap.containsKey(baseId)) {
			LogUtil.error("Duplicate bean(dataSource) register " + baseId);
			throw new RuntimeException("Duplicate bean(dataSource) register " + baseId);
		} else {
			beanNameMap.put(baseId, dataSource);
		}
	}
	
	protected static ResourceBundle getResourceBundle(String jdbcProperties) {
		ResourceBundle.clearCache();
		try {
			String path = new StringBuilder().append(ServerConfig.configPath0)
					.append(File.separator).append(jdbcProperties)
					.append(".properties").toString();
			
			return new PropertyResourceBundle(new BufferedInputStream(
					new FileInputStream(path)));
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T propertyValue(ResourceBundle rb, Class<T> clz, String propertyName) {
		if(rb.containsKey(propertyName)) {
			String value = rb.getString(propertyName);
			if(Integer.class.equals(clz)) {
				return (T) Integer.valueOf(value);
			} else if(String.class.equals(clz)) {
				return (T) value;
			}
			return null;
		} else {
			throw new RuntimeException("Property file missing " + propertyName + " value");
		}
	}
}
