/**
 * 
 */
package com.yaowan.excel;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yaowan.core.base.GlobalConfig;
import com.yaowan.framework.excel.ExcelObject;
import com.yaowan.framework.excel.ExcelReader;
import com.yaowan.framework.util.CommonUtils;
import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 *
 */
public abstract class ExcelCache<T extends ExcelObject> {
	/**
	 * 配置数据列表
	 */
	private List<T> cacheList;
	/**
	 * 配置数据ID映射表
	 */
	private Map<Integer, T> cacheMap;
	
	private Map<String, Field> fieldMap = new HashMap<String, Field>();
	private Map<String, Method> methodMap = new HashMap<String, Method>();
	
	private Map<String, String> currRowData;
	
	public ExcelCache() {
		ExcelCacheLoader.register(getFileName(), this);
	}
	
	public abstract String getFileName();
	
	public final void load() {
		try {
			loadList();
			loadMap();
			loadOther();
			fieldMap.clear();
			methodMap.clear();
			LogUtil.info(getFileName() + " load data size " + cacheList.size());
		} catch(Exception e) {
			LogUtil.error(e);
		}
	}
	
	public T formObject() {
		try {
			if(this.currRowData == null) {
				return null;
			}
			Class<T> clazz = getExcelClass();
			T obj = clazz.newInstance();
			
			for(String fieldName : this.currRowData.keySet()) {
				
				String fieldValue = this.currRowData.get(fieldName);
				
				Field field = fieldMap.get(fieldName);
				try {
					if(field == null) {
						field = clazz.getDeclaredField(fieldName);
						fieldMap.put(fieldName, field);
					}
				} catch(Exception e) {
					//unnecessary do anything
				}
				
				Method setter = methodMap.get(fieldName);
				try {
					if(setter == null) {
						setter = clazz.getDeclaredMethod(getSetterName(fieldName), field.getType());
						methodMap.put(fieldName, setter);
					}
				} catch(Exception e) {
					//unnecessary do anything
				}
				if(setter == null) {
					continue;
				}
				if (byte.class.equals(field.getType())) {
					setter.invoke(obj, CommonUtils.parseByte(fieldValue));
				}else if (short.class.equals(field.getType())) {
					setter.invoke(obj, CommonUtils.parseShort(fieldValue));
				} else if (int.class.equals(field.getType())) {
					setter.invoke(obj, CommonUtils.parseInt(fieldValue));
				} else if (long.class.equals(field.getType())) {
					setter.invoke(obj, CommonUtils.parseLong(fieldValue));
				} else if (float.class.equals(field.getType())) {
					setter.invoke(obj, CommonUtils.parseFloat(fieldValue));
				} else {
					setter.invoke(obj, fieldValue);
				}
			}
			return obj;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private final void loadList() {
		File dataFile = new File(GlobalConfig.excelPath + File.separator + getFileName());
		
		List<Map<String, String>> dataList = ExcelReader.excelData(dataFile);
		if(dataList == null) {
			LogUtil.error(getFileName() + " can not read");
		}
		
		List<T> tempCacheList = new ArrayList<T>();
		
		for(Map<String, String> rowData : dataList) {
			this.currRowData = rowData;
			T t = formObject();
			if(t == null) {
				continue;
			}
			tempCacheList.add(t);
		}
		cacheList = tempCacheList;
	}
	
	private final void loadMap() {
		Map<Integer, T> tempCacheMap = new HashMap<Integer, T>();
		for(T t : this.getAllList()) {
			tempCacheMap.put(t.getId(), t);
		}
		this.cacheMap = tempCacheMap;
	}
	
	public void loadOther() {
		//default do nothing
	}
	
	@SuppressWarnings("unchecked")
	private final Class<T> getExcelClass() {
		return (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}
	private String getSetterName(String fieldName) {
		return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}
	
	public final List<T> getAllList() {
		return cacheList;
	}
	
	protected byte getByte(String colName) {
		return CommonUtils.parseByte(getString(colName));
	}
	
	protected short getShort(String colName) {
		return CommonUtils.parseShort(getString(colName));
	}
	
	protected int getInt(String colName) {
		return CommonUtils.parseInt(getString(colName));
	}
	
	protected long getLong(String colName) {
		return CommonUtils.parseLong(getString(colName));
	}
	
	protected float getFloat(String colName) {
		return CommonUtils.parseFloat(getString(colName));
	}
	
	protected String getString(String colName) {
		return this.currRowData.get(colName);
	}
	
	public T get(int id) {
		return this.cacheMap.get(id);
	}
	
	public void doAfterAllCacheReady() {
		
	}
}
