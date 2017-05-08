package com.yaowan.framework.core.events.handler;

import java.util.HashMap;
import java.util.Map;

import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.EventConstant;

/**
 * 事件分发
 * 
 * @author Thomas Zheng
 * 
 */
public class DispatchEvent {


	/**
	 * 事件处理器
	 */
	private static Map<Integer, Map<Integer, EventHandler>> handlers = new HashMap<Integer, Map<Integer, EventHandler>>();

	/**
	 * 注册事件处理器
	 * 
	 * @param eventType
	 * @param handler
	 */
	public static synchronized void addEventListener(int eventType,
			EventHandler handler) {
		Map<Integer, EventHandler> map = handlers.get(eventType);
		if (map == null) {
			map = new HashMap<Integer, EventHandler>();
			handlers.put(eventType, map);
		}
		map.put(handler.getHandle(), handler);
	}

	/**
	 * 发送事件
	 * 
	 * @param event
	 *            事件
	 * @return 成功发送返回true
	 */
	public static int dispacthEvent(Event event) {
		Map<Integer, EventHandler> map = handlers.get(event.getEventType());
		if (map == null) {
			//LogUtil.debug("不存事件处理器类型 [{}]"+event.getEventType());
			event.setFlag(EventConstant.FAIL);
			return EventConstant.FAIL;
		}
		EventHandler handler = map.get(event.getHandle());
		if (handler == null) {
			/*LogUtil.debug("事件处理器类型 [{}] 不存在处理器 [{}]"+event.getEventType()+ event
					.getHandle());*/
			event.setFlag(EventConstant.FAIL);
			return EventConstant.FAIL;
		}
		return handler.execute(event);
	}
}
