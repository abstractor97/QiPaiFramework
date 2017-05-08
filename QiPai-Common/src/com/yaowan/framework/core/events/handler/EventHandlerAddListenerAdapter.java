package com.yaowan.framework.core.events.handler;

import java.util.ArrayList;
import java.util.List;

import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.listener.EventListener;
import com.yaowan.framework.util.LogUtil;
/**
 * 增加对事件的监听者
 * @author flyingfan
 *
 */
public abstract class EventHandlerAddListenerAdapter extends EventHandlerAdapter {

	List<EventListener> listeners = new ArrayList<EventListener>();
	
	@Override
	public int execute(Event event) {
		int result = process(event);
		for (EventListener eventListener : listeners) {
			try {
				eventListener.listenIn(event,getHandle());
			} catch (Exception e) {
				LogUtil.error("事件监听出错: EventType:"+getEventType(), e);
			}
		}
		return result;
	}
	
	public abstract int process(Event event);

	public void addListener(EventListener listener){
		listeners.add(listener);
	}
	
}
