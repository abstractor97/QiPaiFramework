/**
 * 
 */
package com.yaowan.framework.core.handler.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.yaowan.framework.core.handler.AbstractLink;
import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 * 
 * 服务端处理器分发器
 */
public class ServerDispatcher {
	/**
	 * 协议执行者
	 */
	private static Map<Integer, IServerExecutor> handlerMap = new HashMap<Integer, IServerExecutor>();
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
			IServerExecutor executor) {
		
		if(handlerMap.containsKey(reqProtocol)) {
			//重复的协议号注册
			LogUtil.error("Duplicate protocol executor register " + reqProtocol);
			throw new RuntimeException("Duplicate protocol executor register " + reqProtocol);
		}
		if(executor == null) {
			LogUtil.error("Executor can not be null " + reqProtocol);
			throw new RuntimeException("Executor can not be null " + reqProtocol);
		}
		handlerMap.put(reqProtocol, executor);
		
		//请求的间隔时间
		if(interval > 0) {
			intervalMap.put(reqProtocol, interval);
		}
		//检查不需要登陆的协议
		if(!checkLogin) {
			uncheckLoginSet.add(reqProtocol);
		}
	}
	/**
	 * 协议的请求间隔时间
	 * 
	 * @param protocolId
	 * @return
	 */
	public static int getIntervalTime(int protocolId) {
		if(intervalMap.containsKey(protocolId)) {
			return intervalMap.get(protocolId);
		}
		return 0;
	}
	/**
	 * 获取协议执行者
	 * @param protocolId
	 * @return
	 */
	public static IServerExecutor getExecutor(int protocolId) {
		return handlerMap.get(protocolId);
	}
	/**
	 * 请求是否合法
	 * 
	 * @param player
	 * @param protocolId
	 * @return
	 */
	public static boolean isRequestValid(AbstractLink link, int protocolId) {
		//协议是否需要登录后才能请求
		if (uncheckLoginSet.contains(protocolId)) {
			return true;
		} else {
			return false;
		}
	}
}
