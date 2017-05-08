/**
 * 
 */
package com.yaowan.csv;

import java.util.List;

import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 *
 */
public abstract class SystemConfigCache<T extends ISystemBean> extends ConfigCache<T> {

	@Override
	public void loadCache(String[] configHead, List<String[]> configDatas) {
		super.loadCache(configHead, configDatas);
		checkSystemMethod();
	}
	
	public void checkSystemMethod() {
		Class<?> clazz = this.getClass();
		for(T t : this.getConfigList()) {
			String methodName = "get" + t.getKey().replaceAll("_", "");
			try {
				clazz.getDeclaredMethod(methodName, new Class[0]);
			} catch (NoSuchMethodException e) {
				LogUtil.warn(clazz.getSimpleName() + " missing method named " + methodName);
			} catch (SecurityException e) {
				//
			}
		}
	}
}
