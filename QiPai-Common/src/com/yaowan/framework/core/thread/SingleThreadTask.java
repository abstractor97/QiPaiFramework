/**
 * 
 */
package com.yaowan.framework.core.thread;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.yaowan.framework.util.LogUtil;



/**
 * @author huangyuyuan
 *
 */
public abstract class SingleThreadTask implements Runnable {

	private ISingleData singleData;
	
	public SingleThreadTask(ISingleData singleData) {
		if(singleData == null) {
			throw new RuntimeException("Match can not be null");
		}
		this.singleData = singleData;
	}
	
	@Override
	public void run() {
		    long time =System.currentTimeMillis();
		    try {
		    	doTask(this.singleData);
				long dif =System.currentTimeMillis()-time;
				if(dif>1000){
					
					LogUtil.info(singleData.getSingleId()+"任务消耗缓慢！！！"+dif);
				}
			} catch (Exception e) {
				LogUtil.error(ExceptionUtils.getStackFrames(e));
			}
			
			//LogUtil.info(singleData.getSingleId()+"任务消耗"+(System.currentTimeMillis()-time));
//			LogUtil.debug(match.getId() + " run in " + Thread.currentThread().getName());
		
	}
	
	public abstract void doTask(ISingleData singleData);

	public ISingleData getISingleData() {
		return singleData;
	}
}
