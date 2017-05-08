package com.yaowan.framework.core.events.handler;

import javax.annotation.PostConstruct;

/**
 * 抽象事件处理器
 * 
 * @author Thomas Zheng
 * 
 */
public abstract class EventHandlerAdapter implements EventHandler {

	/**
	 * 初始化
	 */
	@PostConstruct
	void initialize() {
		DispatchEvent.addEventListener(getHandle()/1000, this);
	}

	/**
	 * 此Handler所属类型
	 * 
	 * @return
	 */
	protected  int getEventType(){
		return 0;	
	}
}
