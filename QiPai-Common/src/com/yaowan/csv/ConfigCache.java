/**
 * 
 */
package com.yaowan.csv;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.SortUtil;
import com.yaowan.framework.util.StringUtil;

/**
 * @author huangyuyuan
 *
 */
public abstract class ConfigCache<T> implements IConfig<T> {

	private List<T> configList;
	
	private List<T> sortConfigList;
	
	private Map<String, Method> methodMap = new HashMap<>();
	
	@Autowired
	ConfigLoader configLoader;
	
	@PostConstruct
	public void init() {
		configLoader.register(getFileName(), this);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected final void loadMap(String[] indexs, Map map) {
		if(indexs == null || indexs.length <= 0) {
			return;
		}
		map.clear();
		try {
			for(T t : configList) {
				
				Map lastMap = map;
				
				for(int i = 0; i < indexs.length; i++) {
					String index = indexs[i];
					
					Field field = t.getClass().getDeclaredField(index);
					
					field.setAccessible(true);
					
					Object keyValue = field.get(t);
					//最后一层
					if(i + 1 >= indexs.length) {
						lastMap.put(keyValue, t);
					} else {
						//逐层组装
						Object mapValue = lastMap.get(keyValue);
						if(mapValue == null) {
							mapValue = new HashMap();
						}
						lastMap.put(keyValue, mapValue);
						lastMap = (Map)mapValue;
					}
					
					field.setAccessible(false);
				}
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private void loadList(Class<T> clazz, String[] configHead, List<String[]> configDatas) {
		
		List<T> configList = new ArrayList<T>();
		for(int i = 0; i < configDatas.size(); i++) {
			String[] configData = configDatas.get(i);
			if(configData.length != configHead.length) {
				LogUtil.warn(clazz.getSimpleName() + " has warning on row " + (i + 2));
				continue;
			}
			configList.add(configToObject(clazz, configHead, configData));
		}
		this.configList = configList;
	}
	
	private void loadSortList() {
		if(this.getSorts() != null && this.getSorts().length > 0) {
			List<T> sortConfigList = new ArrayList<T>();
			sortConfigList.addAll(configList);
			SortUtil.sort(sortConfigList, this.getSorts());
			this.sortConfigList = sortConfigList;
		}
	}
	
	/**
	 * 获取配置表所有数据的列表
	 * 
	 * PS：不保证顺序
	 * 
	 * @return
	 */
	public final List<T> getConfigList() {
		return configList;
	}

	/**
	 * 获取配置表的有序列表
	 * 
	 * PS：仅对只有一层的配置表有业务含义，两层或以上的配置表结构需要自定义存取的缓存
	 * 
	 * @return
	 */
	public final List<T> getSortConfigList() {
		return sortConfigList;
	}

	private T configToObject(Class<T> clazz, String[] configHead, String[] configData) {
		T obj = null;
		try {
			obj = clazz.newInstance();
			for(int i = 0; i < configHead.length; i++) {
				String colHead = configHead[i];
				//表头的检测
				String[] nameType = colHead.trim().split(":");
				if(nameType.length < 2) {
					LogUtil.error("Config loading error: "
							+ clazz.getSimpleName()
							+ " config head does not define with format name:type");
					continue;
				}
				//表体的检测
				if(i >= configData.length) {
					break;
				}
				String colValue = configData[i];
				
				String fieldName = nameType[0];
				String fieldType = nameType[1];
				
				
				Method setter = methodMap.get(fieldName);
				try {
					if(setter == null) {
						setter = clazz.getDeclaredMethod("set" + fieldName, getFieldType(fieldType));
						methodMap.put(fieldName, setter);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				if(setter == null) {
					continue;
				}
				if ("byte".equals(fieldType)) {
					setter.invoke(obj, StringUtil.getValueByStr(colValue,Byte.class));
				}else if ("short".equals(fieldType)) {
					setter.invoke(obj, StringUtil.getValueByStr(colValue,Short.class));
				} else if ("int".equals(fieldType)) {
					setter.invoke(obj, StringUtil.getValueByStr(colValue,Integer.class));
				} else if ("long".equals(fieldType)) {
					setter.invoke(obj, StringUtil.getValueByStr(colValue,Long.class));
				}  else if ("double".equals(fieldType)) {
					setter.invoke(obj, StringUtil.getValueByStr(colValue,Double.class));
				} else if ("float".equals(fieldType)) {
					setter.invoke(obj, StringUtil.getValueByStr(colValue,Float.class));
				} else if ("boolean".equals(fieldType)) {
					boolean b = StringUtil.getValueByStr(colValue,Boolean.class);
					if (!b) {
						int boolValue = StringUtil.getValueByStr(colValue,Integer.class);
						if (boolValue != 0) {
							b = true;
						}
					}
					setter.invoke(obj, b);
				} else {
					setter.invoke(obj, colValue);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	@Override
	public void loadCache(String[] configHead, List<String[]> configDatas) {
		loadList(getConfigClass(), configHead, configDatas);
		loadSortList();
		loadIndexsMap();
		loadOther();
		//释放反射缓存
		methodMap.clear();
		LogUtil.info("Load file " + this.getFileName() + " size : " + configList.size());
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Class<T> getConfigClass() {
		return (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	/**
	 * 加载完数据后的扩展处理
	 */
	protected void loadOther() {
		//default do nothing
	}

	@Override
	public void loadIndexsMap() {
		//default do nothing
	}

	@Override
	public String[] getSorts() {
		return null;
	}
	
	/**
	 * 获取字段类型
	 * 
	 * @param fieldType
	 * @return
	 */
	private static Class<?> getFieldType(String fieldType) {
		if ("byte".equalsIgnoreCase(fieldType)) {
			return byte.class;
		}else if ("short".equalsIgnoreCase(fieldType)) {
			return short.class;
		} else if ("int".equalsIgnoreCase(fieldType)) {
			return int.class;
		} else if ("long".equalsIgnoreCase(fieldType)) {
			return long.class;
		}  else if ("double".equalsIgnoreCase(fieldType)) {
			return double.class;
		} else if ("float".equalsIgnoreCase(fieldType)) {
			return float.class;
		} else if ("boolean".equalsIgnoreCase(fieldType)) {
			return boolean.class;
		} else if("String".equalsIgnoreCase(fieldType)){
			return String.class;
		} else if("Object".equalsIgnoreCase(fieldType)){
			return String.class;
		} else {
			//对于配置表而言，默认都是字符串
			return String.class;
		}
	}

	@Override
	public void loadAfterAllConfigReady() {
		//default do nothing
	}
}
