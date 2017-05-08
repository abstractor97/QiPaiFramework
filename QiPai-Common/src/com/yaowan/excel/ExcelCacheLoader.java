/**
 * 
 */
package com.yaowan.excel;

import java.util.HashMap;
import java.util.Map;

import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 *
 */
public class ExcelCacheLoader {
	
	private static final Map<String, ExcelCache<?>> cacheMap = new HashMap<String, ExcelCache<?>>();
	
	public static void load() {
		for(ExcelCache<?> excelCache : cacheMap.values()) {
			excelCache.load();
		}
		for(ExcelCache<?> excelCache : cacheMap.values()) {
			excelCache.doAfterAllCacheReady();
		}
	}
	
	public static void register(String cacheName, ExcelCache<?> cache) {
		if(cacheMap.containsKey(cacheName)) {
			LogUtil.error("Duplicate excel cache register " + cacheName);
			throw new RuntimeException("Duplicate excel cache register " + cacheName);
		}
		cacheMap.put(cacheName, cache);
	}
	/**
	 * 加载指定的配置文件
	 * @param fileNames
	 */
	public static void load(String ... fileNames){
		for (String fileName : fileNames) {
			ExcelCache<?> excelCache = cacheMap.get(fileName);
			if(excelCache!=null){
				excelCache.load();
			}
		}
	}
}
