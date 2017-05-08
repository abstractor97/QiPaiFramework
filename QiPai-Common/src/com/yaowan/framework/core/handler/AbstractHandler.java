/**
 * 
 */
package com.yaowan.framework.core.handler;

import com.yaowan.framework.core.handler.client.ClientDispatcher;
import com.yaowan.framework.core.handler.client.IClientExecutor;
import com.yaowan.framework.core.handler.server.IServerExecutor;
import com.yaowan.framework.core.handler.server.ServerDispatcher;

/**
 * @author huangyuyuan
 *
 */
public abstract class AbstractHandler {
	
	public AbstractHandler() {
		register();
	}
	/**
	 * 传输方式
	 * @return
	 */
	public abstract int transmitType();
	/**
	 * 模块号
	 * @return
	 */
	public abstract int moduleId();
	/**
	 * 
	 */
	public abstract void register();
	
	/**
	 * 增加常规执行者
	 * 
	 * @param protocol
	 * @param reqClass
	 * @param resClass
	 * @param executoer
	 */
	public void addExecutor(int cmd, IServerExecutor executor) {
		addExecutor(cmd, 0, true, executor);
	}
	/**
	 * 全参数增加执行者
	 * 
	 * @param cmd
	 * @param interval 请求间隔
	 * @param checkLogin 是否检测登陆
	 * @param reqClass
	 * @param resClass
	 * @param executoer
	 */
	public void addExecutor(int cmd, int interval, boolean checkLogin, IServerExecutor executor) {
		//计算出除十万位（请求/响应位）的结果
		int protocol = transmitType() * 10000000;
		protocol = protocol + moduleId() * 1000 + cmd;
		//请求的协议号
		int reqProtocol = protocol +     1000000;
		//响应的协议号
		int resProtocol = protocol +     2000000;
		ServerDispatcher.register(reqProtocol, resProtocol, interval, checkLogin, executor);
	}
	
	/**
	 * 全参数增加执行者
	 * 
	 * @param cmd
	 * @param interval 请求间隔
	 * @param checkLogin 是否检测登陆
	 * @param reqClass
	 * @param resClass
	 * @param executoer
	 */
	public void addExecutor(int cmd, IClientExecutor executor) {
		//计算出除十万位（请求/响应位）的结果
		int protocol = transmitType() * 10000000;
		protocol = protocol + moduleId() * 1000 + cmd;
		//请求的协议号
		int reqProtocol = protocol +     1000000;
		//响应的协议号
		int resProtocol = protocol +     2000000;
		ClientDispatcher.register(reqProtocol, resProtocol, 0, false, executor);
	}
	/**
	 * 全参数增加执行者
	 * 
	 * @param cmd
	 * @param interval 请求间隔
	 * @param checkLogin 是否检测登陆
	 * @param reqClass
	 * @param resClass
	 * @param executoer
	 */
	public void addCenterExecutor(int cmd, IClientExecutor executor) {
		//计算出除十万位（请求/响应位）的结果
		int protocol = transmitType() * 20000000;
		protocol = protocol + moduleId() * 1000 + cmd;
		//请求的协议号
		int reqProtocol = protocol +     1000000;
		//响应的协议号
		int resProtocol = protocol +     2000000;
		ClientDispatcher.register(reqProtocol, resProtocol, 0, false, executor);
	}
}
