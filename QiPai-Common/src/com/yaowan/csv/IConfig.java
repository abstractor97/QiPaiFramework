/**
 * 
 */
package com.yaowan.csv;

import java.util.List;

/**
 * @author huangyuyuan
 *
 */
public interface IConfig<T> {
	/**
	 * 配置文件的文件名
	 * 
	 * @return
	 */
	public String getFileName();
	/**
	 * 加载索引映射表
	 */
	public void loadIndexsMap();
	/**
	 * 排序方法数组
	 * 
	 * @return
	 */
	public String[] getSorts();
	/**
	 * 加载数据在缓存中
	 * 
	 * @param configDatas
	 */
	public void loadCache(String[] configHead, List<String[]> configDatas);
	/**
	 * 配置数据类
	 * 
	 * @return
	 */
	public Class<T> getConfigClass();
	/**
	 * 在所有配置表都就绪的情况下加载
	 */
	public void loadAfterAllConfigReady();
}
