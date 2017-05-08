/**
 * 
 */
package com.yaowan.framework.core.events.handler;

import com.yaowan.framework.core.events.Event;



/**
 * 事件处理器
 * 
 * @author Thomas Zheng
 * 
 */
public interface EventHandler {

	/**
	 * 执行处理器
	 * 
	 * @param event
	 */
	int execute(Event event);

	/**
	 * 处理器句柄
	 * 
	 * @return
	 */
	int getHandle();

}
