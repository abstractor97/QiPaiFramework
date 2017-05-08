/**
 * 
 */
package com.yaowan.framework.core.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;


/**
 * 单线程容器
 * 
 * @author zane
 *
 */
@Component
public class SingleThreadManager {
	/**
	 * 玩家逻辑线程池列表
	 */
	private ExecutorService[] executorServiceList;
	
	public SingleThreadManager() {
		int cpuCores = Runtime.getRuntime().availableProcessors();
		int size = cpuCores*4;
		executorServiceList = new ExecutorService[size];
		for (int i = 0; i < size; i++) {
			executorServiceList[i] = Executors.newSingleThreadExecutor();
		}
	}
	
	private ExecutorService getThread(Number id) {
		return executorServiceList[(int)(id.longValue() % executorServiceList.length)];
	}
	
	/**
	 * 新建一个单线程执行逻辑
	 * 
	 * @param singleThreadTask
	 */
	public void executeTask(SingleThreadTask singleThreadTask) {
		ExecutorService executorService = getThread(singleThreadTask.getISingleData().getSingleId());
		if(executorService == null) {
			throw new RuntimeException("Impossible exception");
		}
		executorService.execute(singleThreadTask);
	} 
}
