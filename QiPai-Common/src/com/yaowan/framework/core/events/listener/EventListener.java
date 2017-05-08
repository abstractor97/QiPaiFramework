package com.yaowan.framework.core.events.listener;

import com.yaowan.framework.core.events.Event;

public interface EventListener {
	/**
	 * 对事件进行监听
	 * @param event
	 * @param eventHandle
	 */
	public void listenIn(Event event,int eventHandle);
	
	//将监听添加至事件的监听列表中
	public void addToEventHandlerAddListenerAdapter();
}
