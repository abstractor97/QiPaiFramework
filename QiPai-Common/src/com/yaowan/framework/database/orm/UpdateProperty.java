package com.yaowan.framework.database.orm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.util.CommonUtils;

/**
 * 更新属性时记录变动属性
 * 
 * @author zane 2016年8月25日 上午10:30:37
 *
 */
public abstract class UpdateProperty {
	
	/**
	 * set 方法集合
	 */
	protected static Map<Class<?>,Map<String,Method>> methodMap = new HashMap<Class<?>, Map<String,Method>>();
	protected Map<String,String> property = new ConcurrentHashMap<String,String>();
	public UpdateProperty() {
		Map<String,Method> map = methodMap.get(getClass());
		if (map == null) {
			map = new ConcurrentHashMap<String, Method>();
			methodMap.put(getClass(),map);
			for (Field field : getClass().getDeclaredFields()) {
				Column column = field.getAnnotation(Column.class);
				if (column != null) {
					try {					
						map.put(field.getName(), methodByField(getClass(), field));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
     * setter 名称
     * @param name
     * @return
     */
    public static Method methodByField(Class<?> clz,Field field) {
    	String name = field.getName();
		StringBuffer st = new StringBuffer("set");
		st.append(Character.toUpperCase(name.charAt(0)));
		// 分辨大寫為下劃綫
		for (int i = 1; i < name.length(); i++) {
			char c = name.charAt(i);
			st.append(c);
		}
		Method method = null;
		try {
			method = clz.getMethod(st.toString(),
					field.getType());
			method.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return method;
	}
	

	
	/**
	 * 设置 变动值到属性
	 * 
	 * @param name
	 * @param value
	 */
	public void addProperty(String name,Object value){
		Map<String,Method> map = methodMap.get(getClass());
		property.put(name, name);
		if(map.containsKey(name)){
			Method method = map.get(name);
			try {
				if(method.getParameterTypes()[0] !=value.getClass()){
					value = CommonUtils.conver(value, method.getParameterTypes()[0]);
				}
				method.invoke(this, value);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
	public void clear(){
		property.clear();
	}
	
	public Set<String> propertys(){
		return property.keySet();
	}
	
	public void markToUpdate(String columnName) {
		property.put(columnName, columnName);
	}
}