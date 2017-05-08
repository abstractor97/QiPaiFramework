/**
 * 
 */
package com.yaowan.framework.core.handler.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 * 
 * 客户端处理器分发器
 */
public class ClientDispatcher {
	/**
	 * 协议执行者
	 */
	private static Map<Integer, IClientExecutor> handlerMap = new HashMap<Integer, IClientExecutor>();
	/**
	 * 没有登陆都可以请求的协议
	 */
	private static Set<Integer> uncheckLoginSet = new HashSet<Integer>();
	/**
	 * 有请求间隔要求的协议
	 */
	private static Map<Integer, Integer> intervalMap = new HashMap<Integer, Integer>();
	/**
	 * 全参数注册方法
	 * 
	 * @param protocolId
	 * @param interval
	 * @param checkLogin
	 * @param reqClass
	 * @param resClass
	 * @param executoer
	 */
	public static void register(int reqProtocol, int resProtocol, int interval, boolean checkLogin,
			IClientExecutor executor) {
		
		if(handlerMap.containsKey(resProtocol)) {
			//重复的协议号注册
			LogUtil.error("Duplicate protocol executor register " + resProtocol);
			throw new RuntimeException("Duplicate protocol executor register " + resProtocol);
		}
		if(executor == null) {
			LogUtil.error("Executor can not be null " + resProtocol);
			throw new RuntimeException("Executor can not be null " + resProtocol);
		}
		handlerMap.put(resProtocol, executor);
		
		//请求的间隔时间
		if(interval > 0) {
			intervalMap.put(resProtocol, interval);
		}
		//检查不需要登陆的协议
		if(!checkLogin) {
			uncheckLoginSet.add(resProtocol);
		}
	}
	/**
	 * 获取协议执行者
	 * @param protocolId
	 * @return
	 */
	public static IClientExecutor getExecutor(int protocolId) {
		return handlerMap.get(protocolId);
	}
}
